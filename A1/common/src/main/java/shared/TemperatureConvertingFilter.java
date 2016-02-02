package shared;

import framework.SimpleFilter;
import framework.MeasurementConfig;
import util.ConversionUtil;

import java.text.DecimalFormat;
import java.util.Arrays;

/**
 * The filter that converts temperature data from fahrenheit to celsius and formats it to string.
 *
 * @since 1.0.0
 */
public class TemperatureConvertingFilter extends SimpleFilter {

    /**
     * String format of the temperature data in celsius
     */
    private String numberFormat = "#000.00000";

    public TemperatureConvertingFilter(String filterId, MeasurementConfig context) {
        super(context, filterId);
    }

    /**
     * Do conversion for temperature data and format it into string.
     *
     * @param id the id of the measurement data
     * @param measurement data
     * @return
     */
    @Override
    protected byte[] doTransform(int id, byte[] measurement) {
        // return data as is if it is not temperature
        if (id != MeasurementConfig.ID_TEMPERATURE) {
            return measurement;
        }

        // read data in fahrenheit
        double tempInFahrenheit = ConversionUtil.convertToDouble(measurement);

        // convert data to celsius
        double tempInCelsius = (tempInFahrenheit - 32d) / 1.8d;

        // format data
        String formatted = new DecimalFormat(numberFormat).format(tempInCelsius);

        // return padded data
        return Arrays.copyOf(formatted.getBytes(), 10);
    }

    public String getNumberFormat() {
        return numberFormat;
    }

    public void setNumberFormat(String numberFormat) {
        this.numberFormat = numberFormat;
    }
}
