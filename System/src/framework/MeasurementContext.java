package framework;

/**
 * @author Weinan Qiu
 * @since 1.0.0
 */
public class MeasurementContext {

    public static final int ID_TIME = 0;
    public static final int ID_VELOCITY = 1;
    public static final int ID_ALTITUDE = 2;
    public static final int ID_PRESSURE = 3;
    public static final int ID_TEMPERATURE = 4;
    public static final int ID_ATTITUDE = 5;

    private int idLength;
    private int timeLength;
    private int velocityLength;
    private int altitudeLength;
    private int pressureLength;
    private int temperatureLength;
    private int attitudeLength;

    private boolean shouldExpectId;
    private boolean shouldExpectTime;
    private boolean shouldExpectVelocity;
    private boolean shouldExpectAltitude;
    private boolean shouldExpectPressure;
    private boolean shouldExpectTemperature;
    private boolean shouldExpectAttitude;

    public static MeasurementContext newContext() {
        return new MeasurementContext();
    }

    public static MeasurementContext defaultContext() {
        return newContext()
                .expectIdWithLength(4)
                .expectTimeWithLength(8)
                .expectVelocityWithLength(8)
                .expectAltitudeWithLength(8)
                .expectPressureWithLength(8)
                .expectTemperatureWithLength(8)
                .expectAttitudeWithLength(8);
    }

    public int idForMeasurementLength(int id) {
        switch (id) {
            case ID_TIME:
                return timeLength;
            case ID_VELOCITY:
                return velocityLength;
            case ID_ALTITUDE:
                return altitudeLength;
            case ID_PRESSURE:
                return pressureLength;
            case ID_TEMPERATURE:
                return temperatureLength;
            case ID_ATTITUDE:
                return attitudeLength;
            default:
                throw new IllegalArgumentException("id: " + id + " does not have corresponding measurement");
        }
    }

    public MeasurementContext expectIdWithLength(int idLength) {
        this.idLength = idLength;
        this.shouldExpectId = true;
        return this;
    }

    public MeasurementContext expectTimeWithLength(int timeLength) {
        this.timeLength = timeLength;
        this.shouldExpectTime = true;
        return this;
    }

    public MeasurementContext expectVelocityWithLength(int velocityLength) {
        this.velocityLength = velocityLength;
        this.shouldExpectVelocity = true;
        return this;
    }

    public MeasurementContext expectAltitudeWithLength(int altitudeLength) {
        this.altitudeLength = altitudeLength;
        this.shouldExpectAltitude = true;
        return this;
    }

    public MeasurementContext expectPressureWithLength(int pressureLength) {
        this.pressureLength = pressureLength;
        this.shouldExpectPressure = true;
        return this;
    }

    public MeasurementContext expectTemperatureWithLength(int temperatureLength) {
        this.temperatureLength = temperatureLength;
        this.shouldExpectTemperature = true;
        return this;
    }

    public MeasurementContext expectAttitudeWithLength(int attitudeLength) {
        this.attitudeLength = attitudeLength;
        this.shouldExpectAttitude = true;
        return this;
    }

    public int getIdLength() {
        return idLength;
    }

    public int getTimeLength() {
        return timeLength;
    }

    public int getVelocityLength() {
        return velocityLength;
    }

    public int getAltitudeLength() {
        return altitudeLength;
    }

    public int getPressureLength() {
        return pressureLength;
    }

    public int getTemperatureLength() {
        return temperatureLength;
    }

    public int getAttitudeLength() {
        return attitudeLength;
    }

    public boolean isShouldExpectId() {
        return shouldExpectId;
    }

    public boolean isShouldExpectTime() {
        return shouldExpectTime;
    }

    public boolean isShouldExpectVelocity() {
        return shouldExpectVelocity;
    }

    public boolean isShouldExpectAltitude() {
        return shouldExpectAltitude;
    }

    public boolean isShouldExpectPressure() {
        return shouldExpectPressure;
    }

    public boolean isShouldExpectTemperature() {
        return shouldExpectTemperature;
    }

    public boolean isShouldExpectAttitude() {
        return shouldExpectAttitude;
    }
}
