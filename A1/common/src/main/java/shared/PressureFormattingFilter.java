package shared;

import framework.SimpleFilter;
import framework.MeasurementConfig;
import util.ConversionUtil;

import java.text.DecimalFormat;
import java.util.Arrays;

/**
 * A filter that converts the pressure data to a string format.
 *
 * @since 1.0.0
 */
public class PressureFormattingFilter extends SimpleFilter {

    /**
     * The format of the pressure data.
     */
    private String numberFormat = "#00.00000";

    private boolean treatNegativeValueAsExtrapolated = false;

    public PressureFormattingFilter(String filterId, MeasurementConfig context) {
        super(context, filterId);
    }

    /**
     * Format the pressure data and write the padded byte array out.
     *
     * @param id the id of the measurement data
     * @param measurement data
     *
     * @return the padded byte array of formatted pressure data or any other data as is.
     */
    @Override
    protected byte[] doTransform(int id, byte[] measurement) {
        // return the measurement as is if it is not pressure
        if (id != MeasurementConfig.ID_PRESSURE)
            return measurement;

        // do format
        double pressure = ConversionUtil.convertToDouble(measurement);
        boolean extrapolated = false;
        if (treatNegativeValueAsExtrapolated && pressure < 0.0d) {
            extrapolated = true;
            pressure = Math.abs(pressure);
        }

        String formatted = new DecimalFormat(numberFormat).format(pressure);
        formatted = formatted.replace(".", ":");
        if (extrapolated)
            formatted += "*";

        // pad the byte array and return the formatted data.
        return Arrays.copyOf(formatted.getBytes(), 9);
    }

    public String getNumberFormat() {
        return numberFormat;
    }

    public void setNumberFormat(String numberFormat) {
        this.numberFormat = numberFormat;
    }

    public boolean isTreatNegativeValueAsExtrapolated() {
        return treatNegativeValueAsExtrapolated;
    }

    public void setTreatNegativeValueAsExtrapolated(boolean treatNegativeValueAsExtrapolated) {
        this.treatNegativeValueAsExtrapolated = treatNegativeValueAsExtrapolated;
    }
}
