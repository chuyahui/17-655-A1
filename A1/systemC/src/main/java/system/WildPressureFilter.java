package system;

import framework.MeasurementConfig;
import framework.SplittingFilterTemplate;
import util.ConversionUtil;

import java.util.LinkedList;
import java.util.Queue;

/**
 * A splitting filter that identifies data frames that contains wild pressure points and extrapolates them.
 * Then it sends them down the lower output stream. Any other data frames will be send to upper output stream.
 *
 * This filter also assumes only time and pressure data is available at this stage.
 *
 * @since 1.0.0
 */
public class WildPressureFilter extends SplittingFilterTemplate {

    /**
     * Cache for the last valid frame of data.
     */
    private DataFrame lastFrameWithValidPressure = null;

    /**
     * Cache for the current frame of data under processing
     */
    private DataFrame currentFrame = null;

    /**
     * Queue for any data frames containing wild pressure points.
     */
    private Queue<DataFrame> framesWithInvalidPressure = new LinkedList<DataFrame>();

    public WildPressureFilter(String filterId, MeasurementConfig context) {
        super(filterId, context);
    }

    /**
     * Collect data into a data frame and judge if it contains a wild pressure point. If yes, take measures to
     * extrapolate proper values and flush it. If not, record it as last valid data frame and flush it.
     *
     * @param idBytes bytes for the id for the measurement
     * @param measurement measurement data bytes
     */
    @Override
    protected void routeMeasurement(byte[] idBytes, byte[] measurement) {
        // create frame structure if it is null
        if (currentFrame == null)
            currentFrame = new DataFrame();

        // cache data into the frame
        int id = ConversionUtil.convertToInt(idBytes);
        if (id == MeasurementConfig.ID_TIME) {
            currentFrame.time = measurement;
        } else if (id == MeasurementConfig.ID_PRESSURE) {
            currentFrame.pressure = measurement;
        } else {
            throw new RuntimeException("Invalid id at this filter: " + id);
        }

        // if we haven't collected all frame data, return for now. This function will be called again when new
        // measurement comes in.
        if (!currentFrame.hasCollectedAll())
            return;

        // get pressure data and test if it is valid, if invalid, add it to the invalid queue and return
        double currentPressure = ConversionUtil.convertToDouble(currentFrame.pressure);
        boolean currentPressureValid = isPressureValid(currentPressure);
        if (!currentPressureValid) {
            framesWithInvalidPressure.add(currentFrame);
            currentFrame = null;
            return;
        }

        // From this point on, the current frame is VALID!

        // if we have a backlog of invalid frames, extrapolate proper values for the frame and flush it to the
        // lower (extrapolated) output port, maintaining the original order.
        while (framesWithInvalidPressure.size() > 0) {
            DataFrame correctedFrame = framesWithInvalidPressure.poll();
            correctedFrame.pressure = extrapolatePressure(currentPressure, true);
            sendFrameToOutputPortTwo(correctedFrame);
        }

        // flush the current (valid) frame to the upper (valid) output port. Also record the current valid frame as
        // the last valid frame. Clear the cache for the current frame to prepare for the next frame.
        lastFrameWithValidPressure = currentFrame;
        sendFrameToOutputPortOne(currentFrame);
        currentFrame = null;
    }

    /**
     * In the event of end of stream, we must check if there are any remaining wild data frames. If so, they must be
     * treated as wild points occurring in the end of stream since any valid data frame before the end of stream will have
     * caused the wild point backlog to be cleared.
     */
    @Override
    protected void reachedEndOfStream() {
        while (framesWithInvalidPressure.size() > 0) {
            DataFrame correctedFrame = framesWithInvalidPressure.poll();
            correctedFrame.pressure = extrapolatePressure(null, false);
            sendFrameToOutputPortTwo(correctedFrame);
        }
    }

    /**
     * Utility method to flush all bytes inside the data frame structure to the upper port.
     *
     * @param frame data frame to be flushed
     */
    private void sendFrameToOutputPortOne(DataFrame frame) {
        for (byte aByte : ConversionUtil.convertToByteArray(MeasurementConfig.ID_TIME))
            WriteFilterOutputPortOne(aByte);
        for (byte aByte : frame.time)
            WriteFilterOutputPortOne(aByte);

        for (byte aByte : ConversionUtil.convertToByteArray(MeasurementConfig.ID_PRESSURE))
            WriteFilterOutputPortOne(aByte);
        for (byte aByte : frame.pressure)
            WriteFilterOutputPortOne(aByte);
    }

    /**
     * Utility method to flush all bytes inside the data frame structure to the lower port.
     *
     * @param frame data frame to be flushed
     */
    private void sendFrameToOutputPortTwo(DataFrame frame) {
        for (byte aByte : ConversionUtil.convertToByteArray(MeasurementConfig.ID_TIME))
            WriteFilterOutputPortTwo(aByte);
        for (byte aByte : frame.time)
            WriteFilterOutputPortTwo(aByte);

        for (byte aByte : ConversionUtil.convertToByteArray(MeasurementConfig.ID_PRESSURE))
            WriteFilterOutputPortTwo(aByte);
        for (byte aByte : frame.pressure)
            WriteFilterOutputPortTwo(aByte);
    }

    /**
     * Check if the pressure is a valid point
     *
     * @param pressure pressure data to be checked.
     * @return true if pressure is a valid point, false if pressure is a wild point.
     */
    private boolean isPressureValid(double pressure) {
        // negative values are invalid
        if (pressure < 0.0d)
            return false;

        // no record of last valid pressure, meaning first record in stream, must be valid.
        if (lastFrameWithValidPressure == null || lastFrameWithValidPressure.pressure == null)
            return true;

        // invalid if variation is greater than 10 PSI.
        return Math.abs(pressure - ConversionUtil.convertToDouble(lastFrameWithValidPressure.pressure)) <= 10.0d;
    }

    /**
     * Perform extrapolation.
     *
     * @param currentPressure current pressure data
     * @param currentPressureValid validity of the current pressure data.
     *
     * @return extrapolated result in bytes
     */
    private byte[] extrapolatePressure(Double currentPressure, boolean currentPressureValid) {
        double value;

        // we have both last valid pressure data and the current (next) valid pressure data (wild point at middle of stream)
        if ((lastFrameWithValidPressure != null && lastFrameWithValidPressure.pressure != null) && currentPressureValid)
            value = (currentPressure + ConversionUtil.convertToDouble(lastFrameWithValidPressure.pressure)) / 2.0d;

        // we have current (next) valid pressure data but no last valid data (wild points at start of stream)
        else if ((lastFrameWithValidPressure == null || lastFrameWithValidPressure.pressure == null) && currentPressureValid)
            value = currentPressure;

        // we have last valid data but not current (next) valid data (wild points at end of stream)
        else if ((lastFrameWithValidPressure != null && lastFrameWithValidPressure.pressure != null) && currentPressure == null)
            value = ConversionUtil.convertToDouble(lastFrameWithValidPressure.pressure);

        // impossible state
        else
            throw new IllegalStateException("Impossible state");

        return ConversionUtil.convertToByteArray(value);
    }

    /**
     * Structure for caching a frame of data containing time and pressure. We didn't bother
     * to encapsulate accessing and mutating behavior since this structure will only be used internally.
     */
    class DataFrame {
        public byte[] time;
        public byte[] pressure;

        public boolean hasCollectedAll() {
            return time != null &&
                    pressure != null;
        }
    }
}
