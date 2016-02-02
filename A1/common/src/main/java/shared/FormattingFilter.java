package shared;

import framework.SimpleFilter;
import framework.MeasurementConfig;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * A filter that formats several incoming measurements into a single line of string. It assumes the incoming measurements
 * are strings. It adds a tab between measurement data and also adds a new line character to the end of the line.
 *
 * @since 1.0.0
 */
public class FormattingFilter extends SimpleFilter {

    /**
     * Constant characters that will be used in formatting the line.
     */
    private static final String TAB = "\t";
    private static final String NEW_LINE = "\n";

    /**
     * Configuration options to specify what measurement to obtain and include in the line.
     */
    private boolean timeRequired;
    private boolean velocityRequired;
    private boolean altitudeRequired;
    private boolean pressureRequired;
    private boolean temperatureRequired;
    private boolean attitudeRequired;

    /**
     * Data cache for any incoming measurement. It will be cleared once we flush the formatted line out.
     */
    private byte[] time;
    private byte[] velocity;
    private byte[] altitude;
    private byte[] pressure;
    private byte[] temperature;
    private byte[] attitude;

    public FormattingFilter(String filterId, MeasurementConfig context) {
        super(context, filterId);
    }

    /**
     * Cache the incoming measurement. When we have all the required measurement, format it and flush them to the
     * output stream.
     *
     * @param id the id of the measurement data
     * @param measurement data
     *
     * @return an empty byte array since we will deliberately take control when to flush data, hence we don't need
     * the super class to do anything for us.
     */
    @Override
    protected byte[] doTransform(int id, byte[] measurement) {

        // cache data
        if (id == MeasurementConfig.ID_TIME)
            time = measurement;
        else if (id == MeasurementConfig.ID_VELOCITY)
            velocity = measurement;
        else if (id == MeasurementConfig.ID_ALTITUDE)
            altitude = measurement;
        else if (id == MeasurementConfig.ID_PRESSURE)
            pressure = measurement;
        else if (id == MeasurementConfig.ID_TEMPERATURE)
            temperature = measurement;
        else if (id == MeasurementConfig.ID_ATTITUDE)
            attitude = measurement;

        // format and flush if we have all required data
        formatAndFlushIfNecessary();

        // return nothing since we don't need super class to flush anything for us.
        return new byte[0];
    }

    /**
     * In the order of "time velocity temperature altitude pressure attitude", put a tab
     * between the required measurements and add a new line at the end. Write all the formatted
     * bytes to the output and clear the cache.
     */
    private void formatAndFlushIfNecessary() {
        if (hasCachedAllRequiredItems()) {
            try {
                // do format
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                if (timeRequired) {
                    buffer.write(trimBytes(time));
                    buffer.write(TAB.getBytes());
                }
                if (velocityRequired) {
                    buffer.write(trimBytes(velocity));
                    buffer.write(TAB.getBytes());
                }
                if (temperatureRequired) {
                    buffer.write(trimBytes(temperature));
                    buffer.write(TAB.getBytes());
                }
                if (altitudeRequired) {
                    buffer.write(trimBytes(altitude));
                    buffer.write(TAB.getBytes());
                }
                if (pressureRequired) {
                    buffer.write(trimBytes(pressure));
                    buffer.write(TAB.getBytes());
                }
                if (attitudeRequired) {
                    buffer.write(trimBytes(attitude));
                    buffer.write(TAB.getBytes());
                }
                buffer.write(NEW_LINE.getBytes());

                // write output
                for (byte eachByte : buffer.toByteArray())
                    WriteFilterOutputPort(eachByte);
            } catch (IOException ex) {
                throw new RuntimeException("Something went wrong: " + ex.getMessage());
            } finally {
                // reset cache
                resetCache();
            }
        }
    }

    /**
     * We need to trim the bytes since the byte array may have been padded in the previous filters. If we
     * directly write them out, we could see a little square at where should have been a plus sign.
     *
     * @param bytes the byte array to be converted to string and trimmed for any unrecognized characters.
     * @return the trimmed version of the byte array data
     */
    private byte[] trimBytes(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8).trim().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Test if we have gathered all required data measurements in a line.
     *
     * @return
     */
    private boolean hasCachedAllRequiredItems() {
        boolean timeCheck = true, velocityCheck = true, altitudeCheck = true, pressureCheck = true,
                temperatureCheck = true, attitudeCheck = true;
        if (timeRequired)
            timeCheck = time != null;
        if (velocityRequired)
            velocityCheck = velocity != null;
        if (altitudeRequired)
            altitudeCheck = altitude != null;
        if (pressureRequired)
            pressureCheck = pressure != null;
        if (temperatureRequired)
            temperatureCheck = temperature != null;
        if (attitudeRequired)
            attitudeCheck = attitude != null;

        return timeCheck && velocityCheck && altitudeCheck && pressureCheck && temperatureCheck && attitudeCheck;
    }

    /**
     * Reset the cache to prepare for another line.
     */
    private void resetCache() {
        this.time = null;
        this.velocity = null;
        this.altitude = null;
        this.pressure = null;
        this.temperature = null;
        this.attitude = null;
    }

    public void setAttitudeRequired(boolean attitudeRequired) {
        this.attitudeRequired = attitudeRequired;
    }

    public void setTemperatureRequired(boolean temperatureRequired) {
        this.temperatureRequired = temperatureRequired;
    }

    public void setPressureRequired(boolean pressureRequired) {
        this.pressureRequired = pressureRequired;
    }

    public void setAltitudeRequired(boolean altitudeRequired) {
        this.altitudeRequired = altitudeRequired;
    }

    public void setVelocityRequired(boolean velocityRequired) {
        this.velocityRequired = velocityRequired;
    }

    public void setTimeRequired(boolean timeRequired) {
        this.timeRequired = timeRequired;
    }
}
