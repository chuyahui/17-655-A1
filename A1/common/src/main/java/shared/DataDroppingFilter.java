package shared;

import framework.FilterTemplate;
import framework.MeasurementContext;

import java.nio.ByteBuffer;

/**
 * @author Weinan Qiu
 * @since 1.0.0
 */
public class DataDroppingFilter extends FilterTemplate {

    private boolean dropTime = false;
    private boolean dropVelocity = false;
    private boolean dropAltitude = false;
    private boolean dropPressure = false;
    private boolean dropTemperature = false;
    private boolean dropAttitude = false;

    public DataDroppingFilter(String filterId, MeasurementContext context) {
        super(context, filterId);
    }

    @Override
    protected byte[] doTransform(int id, byte[] measurement) {
        if (id == MeasurementContext.ID_TIME)
            return dropTime ? new byte[0] : measurement;

        if (id == MeasurementContext.ID_VELOCITY)
            return dropVelocity ? new byte[0] : measurement;

        if (id == MeasurementContext.ID_ALTITUDE)
            return dropAltitude ? new byte[0] : measurement;

        if (id == MeasurementContext.ID_PRESSURE)
            return dropPressure ? new byte[0] : measurement;

        if (id == MeasurementContext.ID_TEMPERATURE)
            return dropTemperature ? new byte[0] : measurement;

        if (id == MeasurementContext.ID_ATTITUDE)
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
