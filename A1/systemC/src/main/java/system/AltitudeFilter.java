package system;

import framework.MeasurementConfig;
import framework.SplittingFilterTemplate;
import util.ConversionUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * A filter that routes the entire data frame whose altitude is below a certain threshold to the upper output port and
 * others to the lower output port.
 *
 * @since 1.0.0
 */
public class AltitudeFilter extends SplittingFilterTemplate {

    /**
     * Threshold used to identity whether data frame should be routed to upper or lower ports
     */
    private double threshold = 10000d;

    /**
     * Cache for the current data frame
     */
    private DataFrame currentFrame;

    public AltitudeFilter(String filterId, MeasurementConfig context) {
        super(filterId, context);
    }

    /**
     * Collects data for the data frame and routes the frame.
     *
     * @param idBytes
     * @param measurement measurement data bytes
     */
    @Override
    protected void routeMeasurement(byte[] idBytes, byte[] measurement) {
        int id = ConversionUtil.convertToInt(idBytes);

        // create frame if not exists
        if (currentFrame == null)
            currentFrame = new DataFrame();

        // cache frame data (collect only time, pressure and altitude and assume at this stage, all other measurements
        // have been dropped)
        if (id == MeasurementConfig.ID_TIME)
            currentFrame.time = measurement;
        else if (id == MeasurementConfig.ID_PRESSURE)
            currentFrame.pressure = measurement;
        else if (id == MeasurementConfig.ID_ALTITUDE)
            currentFrame.altitude = measurement;
        else
            throw new RuntimeException("measurement with id " + id + " should have been discarded already!");

        // if we haven't collected everything, return for now
        if (!currentFrame.hasCollectedAll())
            return;

        // from this point on, we have the current data frame containing time, pressure and altitude.

        try {
            // if less than threshold (by default, 10K), route to upper port
            if (currentFrame.altitudeInFeet() < threshold) {
                for (byte eachByte : currentFrame.getAllBytes())
                    WriteFilterOutputPortOne(eachByte);
            }
            // otherwise, route to lower port
            else {
                for (byte eachByte : currentFrame.getAllBytes())
                    WriteFilterOutputPortTwo(eachByte);
            }
        } catch (IOException ex) {
            throw new RuntimeException("Encountered error while routing measurement: " + ex.getMessage());
        } finally {
            // reset the current frame after routing to prepare for the next frame
            currentFrame = null;
        }
    }

    @Override
    protected void reachedEndOfStream() {
        // no need to do anything on end of stream since we don't cache more than 1 frame
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    /**
     * Structure to hold time, altitude and pressure data, represents a data frame
     */
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

        /**
         * Utility method to align all the bytes for time, altitude and pressure data (with id)
         * @return
         * @throws IOException
         */
        public byte[] getAllBytes() throws IOException {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.write(ConversionUtil.convertToByteArray(MeasurementConfig.ID_TIME));
            out.write(time);
            out.write(ConversionUtil.convertToByteArray(MeasurementConfig.ID_ALTITUDE));
            out.write(altitude);
            out.write(ConversionUtil.convertToByteArray(MeasurementConfig.ID_PRESSURE));
            out.write(pressure);
            return out.toByteArray();
        }
    }
}
