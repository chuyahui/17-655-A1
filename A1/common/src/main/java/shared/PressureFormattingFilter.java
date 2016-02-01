package shared;

import framework.FilterTemplate;
import framework.MeasurementContext;
import util.ConversionUtil;

import java.text.DecimalFormat;
import java.util.Arrays;

/**
 * @author Weinan Qiu
 * @since 1.0.0
 */
public class PressureFormattingFilter extends FilterTemplate {

    private String numberFormat = "#00.00000";

    public PressureFormattingFilter(String filterId, MeasurementContext context) {
        super(context, filterId);
    }

    @Override
    protected byte[] doTransform(int id, byte[] measurement) {
        if (id != MeasurementContext.ID_PRESSURE)
            return measurement;

        double pressure = ConversionUtil.convertToDouble(measurement);
        String formatted = new DecimalFormat(numberFormat).format(pressure);

        return Arrays.copyOf(formatted.getBytes(), 9);
    }
}
