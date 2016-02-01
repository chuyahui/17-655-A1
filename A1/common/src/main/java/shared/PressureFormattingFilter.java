package shared;

import framework.SimpleFilter;
import framework.MeasurementConfig;
import util.ConversionUtil;

import java.text.DecimalFormat;
import java.util.Arrays;

/**
 * @author Weinan Qiu
 * @since 1.0.0
 */
public class PressureFormattingFilter extends SimpleFilter {

    private String numberFormat = "#00.00000";

    public PressureFormattingFilter(String filterId, MeasurementConfig context) {
        super(context, filterId);
    }

    @Override
    protected byte[] doTransform(int id, byte[] measurement) {
        if (id != MeasurementConfig.ID_PRESSURE)
            return measurement;

        double pressure = ConversionUtil.convertToDouble(measurement);
        String formatted = new DecimalFormat(numberFormat).format(pressure);

        return Arrays.copyOf(formatted.getBytes(), 9);
    }
}
