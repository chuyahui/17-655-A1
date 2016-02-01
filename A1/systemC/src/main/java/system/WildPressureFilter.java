package system;

import framework.MeasurementConfig;
import framework.SplittingFilterTemplate;
import util.ConversionUtil;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @author Weinan Qiu
 * @since 1.0.0
 */
public class WildPressureFilter extends SplittingFilterTemplate {

    private DataFrame lastFrameWithValidPressure = null;
    private DataFrame currentFrame = null;
    private Queue<DataFrame> framesWithInvalidPressure = new LinkedList<DataFrame>();

    public WildPressureFilter(String filterId, MeasurementConfig context) {
        super(filterId, context);
    }

    @Override
    protected void routeMeasurement(byte[] idBytes, byte[] measurement) {
        if (currentFrame == null)
            currentFrame = new DataFrame();

        int id = ConversionUtil.convertToInt(idBytes);
        if (id == MeasurementConfig.ID_TIME) {
            currentFrame.time = measurement;
        } else if (id == MeasurementConfig.ID_PRESSURE) {
            currentFrame.pressure = measurement;
        } else {
            throw new RuntimeException("Invalid id at this filter: " + id);
        }

        if (!currentFrame.hasCollectedAll())
            return;

        double currentPressure = ConversionUtil.convertToDouble(currentFrame.pressure);
        boolean currentPressureValid = isPressureValid(currentPressure);
        if (!currentPressureValid) {
            framesWithInvalidPressure.add(currentFrame);
            sendFrameToOutputPortOne(currentFrame);
            return;
        }

        // extrapolate and flush
        while (framesWithInvalidPressure.size() > 0) {
            DataFrame correctedFrame = framesWithInvalidPressure.poll();
            correctedFrame.pressure = extrapolatePressure(currentPressure, true);
            sendFrameToOutputPortTwo(correctedFrame);
        }

        // replace last valid
        lastFrameWithValidPressure = currentFrame;
        currentFrame = null;
    }

    @Override
    protected void reachedEndOfStream() {
        while (framesWithInvalidPressure.size() > 0) {
            DataFrame correctedFrame = framesWithInvalidPressure.poll();
            correctedFrame.pressure = extrapolatePressure(null, false);
            sendFrameToOutputPortTwo(correctedFrame);
        }
    }

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

    private boolean isPressureValid(double pressure) {
        if (pressure < 0.0d)
            return false;               // negative values are invalid

        if (lastFrameWithValidPressure == null || lastFrameWithValidPressure.pressure == null)
            return true;                // no record of last valid pressure, meaning first record in stream, must be valid.

        return Math.abs(pressure - ConversionUtil.convertToDouble(lastFrameWithValidPressure.pressure)) <= 10.0d; // invalid if variation is greater than 10 PSI.
    }

    private byte[] extrapolatePressure(Double currentPressure, boolean currentPressureValid) {
        double value;
        if ((lastFrameWithValidPressure != null && lastFrameWithValidPressure.pressure != null) && currentPressureValid)
            value = (currentPressure + ConversionUtil.convertToDouble(lastFrameWithValidPressure.pressure)) / 2.0d;
        else if ((lastFrameWithValidPressure == null || lastFrameWithValidPressure.pressure == null) && currentPressureValid)
            value = currentPressure;
        else if ((lastFrameWithValidPressure != null && lastFrameWithValidPressure.pressure != null) && currentPressure == null)
            value = ConversionUtil.convertToDouble(lastFrameWithValidPressure.pressure);
        else
            throw new IllegalStateException("Impossible state");

        return ConversionUtil.convertToByteArray(value);
    }

    class DataFrame {
        public byte[] time;
        public byte[] pressure;

        public boolean hasCollectedAll() {
            return time != null &&
                    pressure != null;
        }
    }
}
