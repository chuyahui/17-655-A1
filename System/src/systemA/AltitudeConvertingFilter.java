package systemA;

import framework.FilterTemplate;
import framework.MeasurementContext;
import util.ConversionUtil;

import java.text.DecimalFormat;

/**
 * @author Weinan Qiu
 * @since 1.0.0
 */
public class AltitudeConvertingFilter extends FilterTemplate {

    private String numberFormat = "#000000.00000";

    public AltitudeConvertingFilter(MeasurementContext context, String filterId) {
        super(context, filterId);
    }

    @Override
    protected byte[] doTransform(int id, byte[] measurement) {
        if (id != MeasurementContext.ID_ALTITUDE)
            return measurement;

        double altitudeInFeet = ConversionUtil.convertToDouble(measurement);
        double altitudeInMeter = altitudeInFeet / 3.2808d;
        String formatted = new DecimalFormat(numberFormat).format(altitudeInMeter);
        return formatted.getBytes();
    }
}
