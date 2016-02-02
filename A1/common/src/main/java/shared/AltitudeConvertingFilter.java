package shared;

import framework.SimpleFilter;
import framework.MeasurementConfig;
import util.ConversionUtil;

import java.text.DecimalFormat;
import java.util.Arrays;

/**
 * A filter that converts altitude data from feet to meter and then formats it into string format.
 *
 * @since 1.0.0
 */
public class AltitudeConvertingFilter extends SimpleFilter {

    /**
     * Number format for the altitude data
     */
    private String numberFormat = "#000000.00000";

    public AltitudeConvertingFilter(String filterId, MeasurementConfig context) {
        super(context, filterId);
    }

    /**
     * Perform altitude data unit conversion and format.
     *
     * @param id the id of the measurement data
     * @param measurement data
     * @return
     */
    @Override
    protected byte[] doTransform(int id, byte[] measurement) {
        // Return data as is if it is not altitude data
        if (id != MeasurementConfig.ID_ALTITUDE)
            return measurement;

        // read altitude data in feet
        double altitudeInFeet = ConversionUtil.convertToDouble(measurement);

        // convert altitude data in feet to meter
        double altitudeInMeter = altitudeInFeet / 3.2808d;

        // format altitude data in meter to string format.
        String formatted = new DecimalFormat(numberFormat).format(altitudeInMeter);

        // load the data into a byte array. Note that we must specify the length of the byte array since
        // String#getBytes() returns variable length array depending on the presence of negative number.
        return Arrays.copyOf(formatted.getBytes(), 13);
    }

    public String getNumberFormat() {
        return numberFormat;
    }

    public void setNumberFormat(String numberFormat) {
        this.numberFormat = numberFormat;
    }
}
