package shared;

import framework.SimpleFilter;
import framework.MeasurementConfig;
import util.ConversionUtil;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * The filter that formats time into string.
 *
 * @author Weinan Qiu
 * @since 1.0.0
 */
public class TimeConvertingFilter extends SimpleFilter {

    /**
     * The string format of time
     */
    private String dateFormat = "yyyy:dd:hh:mm:ss";

    public TimeConvertingFilter(String filterId, MeasurementConfig context) {
        super(context, filterId);
    }

    /**
     * Perform formatting on time and pass it on.
     *
     * @param id the id of the measurement data
     * @param measurement data
     * @return
     */
    @Override
    protected byte[] doTransform(int id, byte[] measurement) {
        // return data as is if it is not time.
        if (id != MeasurementConfig.ID_TIME)
            return measurement;

        // read time in milliseconds
        long time = ConversionUtil.convertToLong(measurement);

        // format time
        String formattedTime = new SimpleDateFormat(dateFormat).format(new Date(time));

        // return padded bytes of the formatted time
        return Arrays.copyOf(formattedTime.getBytes(), 16);
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }
}
