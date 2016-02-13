package system;

import framework.MeasurementConfig;
import framework.SplittingFilterTemplate;
import util.ConversionUtil;

import java.util.LinkedList;
import java.util.Queue;

/**
 * A splitting filter that checks for any wild points in the pressure data. A wild point is any data point that varies
 * more than 10 PSI and/or is negative. A wild point will be extrapolated by taking an average of the last valid data
 * and the next valid data. In the case the wild points are the first in the stream, the first valid data is used. In
 * the case the wild points are the last in the stream, the last valid data is used.
 *
 * Any wild data points will be flushed through the upper output port. All valid data points, including the one that has
 * been extrapolated, will be flushed through the lower output port.
 *
 * This filter also caches a frame of data in the structure of {@link system.PressureValidityFilter.DataFrame}. This
 * provides convenience for calculating when referencing an entire frame of data.
 *
 * @since 1.0.0
 */
public class PressureValidityFilter extends SplittingFilterTemplate {


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

    public PressureValidityFilter(String filterId, MeasurementConfig context) {
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
        } else if (id == MeasurementConfig.ID_TEMPERATURE) {
            currentFrame.temperature = measurement;
        } else if (id == MeasurementConfig.ID_ALTITUDE) {
            currentFrame.altitude = measurement;
        } else if (id == MeasurementConfig.ID_PRESSURE) {
            currentFrame.pressure = measurement;
        } else {
            throw new RuntimeException("Invalid id at this filter: " + id);
        }

        // if we haven't collected all frame data, return for now. This function will be called again when new
        // measurement comes in.
        if (!currentFrame.hasCollectedAll())
            return;

//        receivedCount++;
//        System.out.println("[" + this.getName() + "] " + receivedCount);

        // get pressure data and test if it is valid
        double currentPressure = ConversionUtil.convertToDouble(currentFrame.pressure);
        boolean currentPressureValid = isPressureValid(currentPressure);

        // if pressure is a wild point, add the current data frame to the invalid queue and flush the current frame
        // to the upper (wild) output port. We don't have to worry about extrapolating for now since we must wait
        // until the next valid data frame to come in to do extrapolation.
        if (!currentPressureValid) {
            framesWithInvalidPressure.add(currentFrame);
            sendFrameToOutputPortOne(currentFrame);
            currentFrame = null;
            return;
        }

        // From this point on, the current frame is VALID!

        // if we have a backlog of invalid frames, extrapolate proper values for the frame and flush it to the
        // lower (valid) output port, maintaining the original order.
        while (framesWithInvalidPressure.size() > 0) {
            DataFrame correctedFrame = framesWithInvalidPressure.poll();
            correctedFrame.pressure = extrapolatePressure(currentPressure, true);
            correctedFrame.pressure = toNegative(correctedFrame.pressure);
            sendFrameToOutputPortTwo(correctedFrame);
        }

        // flush the current (valid) frame to the lower (valid) output port. Also record the current valid frame as
        // the last valid frame. Clear the cache for the current frame to prepare for the next frame.
        lastFrameWithValidPressure = currentFrame;
        sendFrameToOutputPortTwo(currentFrame);
        currentFrame = null;
    }

    /**
     * In the event of end of stream, we must check if there are any remaining wild data frames. If so, they must be
     * treated as wild points occurring in the end of stream since any valid data frame before the end of stream will have
     * caused the wild point backlog to be cleared.
     */
    @Override
    protected void reachedEndOfStream() {
        // extrapolate the wild backlogs and send the proper values to lower (valid) output port.
        while (framesWithInvalidPressure.size() > 0) {
            DataFrame correctedFrame = framesWithInvalidPressure.poll();
            correctedFrame.pressure = extrapolatePressure(null, false);
            correctedFrame.pressure = toNegative(correctedFrame.pressure);
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

        for (byte aByte : ConversionUtil.convertToByteArray(MeasurementConfig.ID_TEMPERATURE))
            WriteFilterOutputPortOne(aByte);
        for (byte aByte : frame.temperature)
            WriteFilterOutputPortOne(aByte);

        for (byte aByte : ConversionUtil.convertToByteArray(MeasurementConfig.ID_ALTITUDE))
            WriteFilterOutputPortOne(aByte);
        for (byte aByte : frame.altitude)
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

        for (byte aByte : ConversionUtil.convertToByteArray(MeasurementConfig.ID_TEMPERATURE))
            WriteFilterOutputPortTwo(aByte);
        for (byte aByte : frame.temperature)
            WriteFilterOutputPortTwo(aByte);

        for (byte aByte : ConversionUtil.convertToByteArray(MeasurementConfig.ID_ALTITUDE))
            WriteFilterOutputPortTwo(aByte);
        for (byte aByte : frame.altitude)
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

    private byte[] toNegative(byte[] doubleBytes) {
        double value = ConversionUtil.convertToDouble(doubleBytes);
        return ConversionUtil.convertToByteArray(0.0d - Math.abs(value));
    }

    /**
     * Structure for caching a frame of data containing time, temperature, altitude and pressure. We didn't bother
     * to encapsulate accessing and mutating behavior since this structure will only be used internally.
     */
    class DataFrame {
        public byte[] time;
        public byte[] temperature;
        public byte[] altitude;
        public byte[] pressure;

        public boolean hasCollectedAll() {
            return time != null &&
                    temperature != null &&
                    altitude != null &&
                    pressure != null;
        }
    }
}
