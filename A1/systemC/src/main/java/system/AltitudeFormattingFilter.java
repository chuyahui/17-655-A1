package system;

import framework.FilterTemplate;
import framework.MeasurementContext;
import util.ConversionUtil;

import java.text.DecimalFormat;
import java.util.Arrays;

/**
 * @author Weinan Qiu
 * @since 1.0.0
 */
public class AltitudeFormattingFilter extends FilterTemplate {

    private String numberFormat = "#000000.00000";

    public AltitudeFormattingFilter(String filterId, MeasurementContext context) {
        super(context, filterId);
    }

    @Override
    protected byte[] doTransform(int id, byte[] measurement) {
        if (id != MeasurementContext.ID_ALTITUDE)
            return measurement;

        double altitudeInFeet = ConversionUtil.convertToDouble(measurement);
        String formatted = new DecimalFormat(numberFormat).format(altitudeInFeet);

        return Arrays.copyOf(formatted.getBytes(), 13);
    }

    public String getNumberFormat() {
        return numberFormat;
    }

    public void setNumberFormat(String numberFormat) {
        this.numberFormat = numberFormat;
    }
}
