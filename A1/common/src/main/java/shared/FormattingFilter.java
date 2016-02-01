package shared;

import framework.FilterTemplate;
import framework.MeasurementContext;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Assumes string data and formats the output data with newline.
 *
 * @author Weinan Qiu
 * @since 1.0.0
 */
public class FormattingFilter extends FilterTemplate {

    private static final String TAB = "\t";
    private static final String NEW_LINE = "\n";

    private int recordsWritten = 0;

    private boolean timeRequired;
    private boolean velocityRequired;
    private boolean altitudeRequired;
    private boolean pressureRequired;
    private boolean temperatureRequired;
    private boolean attitudeRequired;

    private byte[] time;
    private byte[] velocity;
    private byte[] altitude;
    private byte[] pressure;
    private byte[] temperature;
    private byte[] attitude;

    public FormattingFilter(String filterId, MeasurementContext context) {
        super(context, filterId);
    }

    @Override
    protected byte[] doTransform(int id, byte[] measurement) {
        if (id == MeasurementContext.ID_TIME)
            time = measurement;
        else if (id == MeasurementContext.ID_VELOCITY)
            velocity = measurement;
        else if (id == MeasurementContext.ID_ALTITUDE)
            altitude = measurement;
        else if (id == MeasurementContext.ID_PRESSURE)
            pressure = measurement;
        else if (id == MeasurementContext.ID_TEMPERATURE)
            temperature = measurement;
        else if (id == MeasurementContext.ID_ATTITUDE)
            attitude = measurement;

        formatAndFlushIfNecessary();
        return new byte[0];
    }

    private void formatAndFlushIfNecessary() {
        if (hasCachedAllRequiredItems()) {
            try {
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

                for (byte eachByte : buffer.toByteArray())
                    WriteFilterOutputPort(eachByte);
            } catch (IOException ex) {
                throw new RuntimeException("Something went wrong: " + ex.getMessage());
            } finally {
                resetCache();
            }
        }
    }

    private byte[] trimBytes(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8).trim().getBytes(StandardCharsets.UTF_8);
    }

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
