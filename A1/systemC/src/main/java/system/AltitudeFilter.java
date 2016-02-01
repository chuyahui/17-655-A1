package system;

import framework.MeasurementContext;
import framework.SplittingFilterTemplate;
import util.ConversionUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author Weinan Qiu
 * @since 1.0.0
 */
public class AltitudeFilter extends SplittingFilterTemplate {

    private double threshold = 10000d;

    private DataFrame currentFrame;

    public AltitudeFilter(String filterId, MeasurementContext context) {
        super(filterId, context);
    }

    @Override
    protected void routeMeasurement(byte[] idBytes, byte[] measurement) {
        int id = ConversionUtil.convertToInt(idBytes);

        if (currentFrame == null)
            currentFrame = new DataFrame();

        if (id == MeasurementContext.ID_TIME)
            currentFrame.time = measurement;
        else if (id == MeasurementContext.ID_PRESSURE)
            currentFrame.pressure = measurement;
        else if (id == MeasurementContext.ID_ALTITUDE)
            currentFrame.altitude = measurement;
        else
            throw new RuntimeException("measurement with id " + id + " should have been discarded already!");

        if (!currentFrame.hasCollectedAll())
            return;

        try {
            if (currentFrame.altitudeInFeet() < threshold) {
                for (byte eachByte : currentFrame.getAllBytes())
                    WriteFilterOutputPortOne(eachByte);
            } else {
                for (byte eachByte : currentFrame.getAllBytes())
                    WriteFilterOutputPortTwo(eachByte);
            }
        } catch (IOException ex) {
            throw new RuntimeException("Encountered error while routing measurement: " + ex.getMessage());
        } finally {
            currentFrame = null;
        }
    }

    @Override
    protected void reachedEndOfStream() {

    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    class DataFrame {
        public byte[] time;
        public byte[] altitude;
        public byte[] pressure;

        public double altitudeInFeet() {
            return ConversionUtil.convertToDouble(altitude);
        }

        public boolean hasCollectedAll() {
            return time != null &&
                    altitude != null &&
                    pressure != null;
        }

        public byte[] getAllBytes() throws IOException {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.write(ConversionUtil.convertToByteArray(MeasurementContext.ID_TIME));
            out.write(time);
            out.write(ConversionUtil.convertToByteArray(MeasurementContext.ID_ALTITUDE));
            out.write(altitude);
            out.write(ConversionUtil.convertToByteArray(MeasurementContext.ID_PRESSURE));
            out.write(pressure);
            return out.toByteArray();
        }
    }
}
