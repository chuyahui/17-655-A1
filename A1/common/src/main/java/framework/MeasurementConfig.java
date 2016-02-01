package framework;

/**
 * A configuration object specifying the length of the incoming data. By default, id is 4 bytes and the rest are
 * 8 bytes. However, this may change as data are transformed, processed or filtered. Hence, the filter will be
 * configured with this object to prepare for the changed byte length.
 *
 * @since 1.0.0
 */
public class MeasurementConfig {

    /**
     * Constants for data id.
     */
    public static final int ID_TIME = 0;
    public static final int ID_VELOCITY = 1;
    public static final int ID_ALTITUDE = 2;
    public static final int ID_PRESSURE = 3;
    public static final int ID_TEMPERATURE = 4;
    public static final int ID_ATTITUDE = 5;

    /**
     * Length for data.
     */
    private int idLength;
    private int timeLength;
    private int velocityLength;
    private int altitudeLength;
    private int pressureLength;
    private int temperatureLength;
    private int attitudeLength;

    /**
     * Convenience method to construct a new configuration.
     *
     * @return a new configuration
     */
    public static MeasurementConfig newConfig() {
        return new MeasurementConfig();
    }

    /**
     * Convenience method to construct a new configuration with default length specified.
     *
     * @return a new configuration containing the default length
     */
    public static MeasurementConfig defaultConfig() {
        return newConfig()
                .expectIdWithLength(4)
                .expectTimeWithLength(8)
                .expectVelocityWithLength(8)
                .expectAltitudeWithLength(8)
                .expectPressureWithLength(8)
                .expectTemperatureWithLength(8)
                .expectAttitudeWithLength(8);
    }

    /**
     * Retrieve the length of the data by the corresponding id.
     *
     * @param id the id of the data.
     * @return the length of the data.
     */
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

    public MeasurementConfig expectIdWithLength(int idLength) {
        this.idLength = idLength;
        return this;
    }

    public MeasurementConfig expectTimeWithLength(int timeLength) {
        this.timeLength = timeLength;
        return this;
    }

    public MeasurementConfig expectVelocityWithLength(int velocityLength) {
        this.velocityLength = velocityLength;
        return this;
    }

    public MeasurementConfig expectAltitudeWithLength(int altitudeLength) {
        this.altitudeLength = altitudeLength;
        return this;
    }

    public MeasurementConfig expectPressureWithLength(int pressureLength) {
        this.pressureLength = pressureLength;
        return this;
    }

    public MeasurementConfig expectTemperatureWithLength(int temperatureLength) {
        this.temperatureLength = temperatureLength;
        return this;
    }

    public MeasurementConfig expectAttitudeWithLength(int attitudeLength) {
        this.attitudeLength = attitudeLength;
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
}
