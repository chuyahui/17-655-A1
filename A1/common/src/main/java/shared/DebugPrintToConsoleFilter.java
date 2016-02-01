package shared;

import framework.SimpleFilter;
import framework.MeasurementConfig;
import util.ConversionUtil;

/**
 * @author Weinan Qiu
 * @since 1.0.0
 */
public class DebugPrintToConsoleFilter extends SimpleFilter {

    public DebugPrintToConsoleFilter(String filterId, MeasurementConfig context) {
        super(context, filterId);
    }

    @Override
    protected byte[] doTransform(int id, byte[] measurement) {
        if (id == MeasurementConfig.ID_TIME)
            System.out.println("[" + this.getName() + "] " + ConversionUtil.convertToLong(measurement));

        return new byte[0];
    }
}
