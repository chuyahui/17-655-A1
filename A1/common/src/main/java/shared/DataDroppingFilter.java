package shared;

import framework.SimpleFilter;
import framework.MeasurementConfig;

/**
 * A filter that discards certain measurement to boost performance. Some measurements are not needed in the trailing
 * filters. They should be dropped that the following filters don't have to waste time parse them and pass them on.
 *
 * @since 1.0.0
 */
public class DataDroppingFilter extends SimpleFilter {

    /**
     * Configuration options on whether a certain measurement should be dropped.
     */
    private boolean dropTime = false;
    private boolean dropVelocity = false;
    private boolean dropAltitude = false;
    private boolean dropPressure = false;
    private boolean dropTemperature = false;
    private boolean dropAttitude = false;

    public DataDroppingFilter(String filterId, MeasurementConfig context) {
        super(context, filterId);
    }

    /**
     * A a certain measurement is configured to be dropped, return a empty byte array so {@link SimpleFilter} will
     * not pass it on. Otherwise, return the data as is so {@link SimpleFilter} passes it on.
     *
     * @param id the id of the measurement data
     * @param measurement data
     * @return
     */
    @Override
    protected byte[] doTransform(int id, byte[] measurement) {
        if (id == MeasurementConfig.ID_TIME)
            return dropTime ? new byte[0] : measurement;

        if (id == MeasurementConfig.ID_VELOCITY)
            return dropVelocity ? new byte[0] : measurement;

        if (id == MeasurementConfig.ID_ALTITUDE)
            return dropAltitude ? new byte[0] : measurement;

        if (id == MeasurementConfig.ID_PRESSURE)
            return dropPressure ? new byte[0] : measurement;

        if (id == MeasurementConfig.ID_TEMPERATURE)
            return dropTemperature ? new byte[0] : measurement;

        if (id == MeasurementConfig.ID_ATTITUDE)
            return dropAttitude ? new byte[0] : measurement;

        return new byte[0];
    }

    public void setDropTime(boolean dropTime) {
        this.dropTime = dropTime;
    }

    public void setDropVelocity(boolean dropVelocity) {
        this.dropVelocity = dropVelocity;
    }

    public void setDropAltitude(boolean dropAltitude) {
        this.dropAltitude = dropAltitude;
    }

    public void setDropPressure(boolean dropPressure) {
        this.dropPressure = dropPressure;
    }

    public void setDropTemperature(boolean dropTemperature) {
        this.dropTemperature = dropTemperature;
    }

    public void setDropAttitude(boolean dropAttitude) {
        this.dropAttitude = dropAttitude;
    }
}
