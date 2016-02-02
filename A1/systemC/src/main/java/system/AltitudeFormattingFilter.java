package system;

import framework.SimpleFilter;
import framework.MeasurementConfig;
import util.ConversionUtil;

import java.text.DecimalFormat;
import java.util.Arrays;

/**
 * Simple filter to format the altitude data into string.
 *
 * @since 1.0.0
 */
public class AltitudeFormattingFilter extends SimpleFilter {

    /**
     * String format of the altitude measurement
     */
    private String numberFormat = "#000000.00000";

    public AltitudeFormattingFilter(String filterId, MeasurementConfig context) {
        super(context, filterId);
    }

    /**
     * Read altitude data and perform string format.
     *
     * @param id the id of the measurement data
     * @param measurement data
     * @return
     */
    @Override
    protected byte[] doTransform(int id, byte[] measurement) {
        // return measurement as is if it is not altitude
        if (id != MeasurementConfig.ID_ALTITUDE)
            return measurement;

        // read measurement to double
        double altitudeInFeet = ConversionUtil.convertToDouble(measurement);

        // format to string
        String formatted = new DecimalFormat(numberFormat).format(altitudeInFeet);

        // return padded bytes
        return Arrays.copyOf(formatted.getBytes(), 13);
    }

    public String getNumberFormat() {
        return numberFormat;
    }

    public void setNumberFormat(String numberFormat) {
        this.numberFormat = numberFormat;
    }
}
