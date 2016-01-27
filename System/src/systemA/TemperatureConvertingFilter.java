package systemA;

import framework.FilterTemplate;
import framework.MeasurementContext;
import util.ConversionUtil;

import java.text.DecimalFormat;

/**
 * @author Weinan Qiu
 * @since 1.0.0
 */
public class TemperatureConvertingFilter extends FilterTemplate {

    private String numberFormat = "#000.0000";

    public TemperatureConvertingFilter(MeasurementContext context, String filterId) {
        super(context, filterId);
    }

    @Override
    protected byte[] doTransform(int id, byte[] measurement) {
        if (id != MeasurementContext.ID_TEMPERATURE)
            return measurement;

        double tempInFahrenheit = ConversionUtil.convertToDouble(measurement);
        double tempInCelsius = (tempInFahrenheit - 32d) / 1.8d;
        String formatted = new DecimalFormat(numberFormat).format(tempInCelsius);
        return formatted.getBytes();
    }

    public String getNumberFormat() {
        return numberFormat;
    }

    public void setNumberFormat(String numberFormat) {
        this.numberFormat = numberFormat;
    }
}
