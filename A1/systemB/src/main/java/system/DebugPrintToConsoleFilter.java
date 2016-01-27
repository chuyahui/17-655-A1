package system;

import framework.FilterTemplate;
import framework.MeasurementContext;
import util.ConversionUtil;

/**
 * @author Weinan Qiu
 * @since 1.0.0
 */
public class DebugPrintToConsoleFilter extends FilterTemplate {

    public DebugPrintToConsoleFilter(String filterId, MeasurementContext context) {
        super(context, filterId);
    }

    @Override
    protected byte[] doTransform(int id, byte[] measurement) {
        if (id == MeasurementContext.ID_PRESSURE)
            System.out.println("[" + this.getName() + "] " + ConversionUtil.convertToDouble(measurement));

        return new byte[0];
    }
}
