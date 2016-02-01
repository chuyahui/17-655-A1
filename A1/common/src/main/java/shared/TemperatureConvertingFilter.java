package shared;

import framework.FilterTemplate;
import framework.MeasurementConfig;
import util.ConversionUtil;

import java.text.DecimalFormat;
import java.util.Arrays;

/**
 * @author Weinan Qiu
 * @since 1.0.0
 */
public class TemperatureConvertingFilter extends FilterTemplate {

    private String numberFormat = "#000.00000";

    public TemperatureConvertingFilter(String filterId, MeasurementConfig context) {
        super(context, filterId);
    }

    @Override
    protected byte[] doTransform(int id, byte[] measurement) {
        if (id != MeasurementConfig.ID_TEMPERATURE) {
            return measurement;
        }


        double tempInFahrenheit = ConversionUtil.convertToDouble(measurement);
        double tempInCelsius = (tempInFahrenheit - 32d) / 1.8d;
        String formatted = new DecimalFormat(numberFormat).format(tempInCelsius);

        return Arrays.copyOf(formatted.getBytes(), 10);
    }

    public String getNumberFormat() {
        return numberFormat;
    }

    public void setNumberFormat(String numberFormat) {
        this.numberFormat = numberFormat;
    }
}
