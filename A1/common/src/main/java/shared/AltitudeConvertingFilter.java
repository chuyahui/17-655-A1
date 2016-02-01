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
public class AltitudeConvertingFilter extends SimpleFilter {

    private String numberFormat = "#000000.00000";

    public AltitudeConvertingFilter(String filterId, MeasurementConfig context) {
        super(context, filterId);
    }

    @Override
    protected byte[] doTransform(int id, byte[] measurement) {
        if (id != MeasurementConfig.ID_ALTITUDE)
            return measurement;

        double altitudeInFeet = ConversionUtil.convertToDouble(measurement);
        double altitudeInMeter = altitudeInFeet / 3.2808d;
        String formatted = new DecimalFormat(numberFormat).format(altitudeInMeter);

        return Arrays.copyOf(formatted.getBytes(), 13);
    }

    public String getNumberFormat() {
        return numberFormat;
    }

    public void setNumberFormat(String numberFormat) {
        this.numberFormat = numberFormat;
    }
}