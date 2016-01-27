package util;

/**
 * @author Weinan Qiu
 * @since 1.0.0
 */
public class ConversionUtil {

    public static int convertToInt(byte[] measurement) {
        int result = 0;
        for (int i=0; i< measurement.length; i++) {
            result = result | (measurement[i] & 0xFF);

            if (i != measurement.length - 1)
                result = result << 8;
        }
        return result;
    }

    public static long convertToLong(byte[] measurement) {
        long result = 0;
        for (int i=0; i< measurement.length; i++) {
            result = result | (measurement[i] & 0xFF);

            if (i != measurement.length - 1)
                result = result << 8;
        }
        return result;
    }

    public static double convertToDouble(byte[] measurement) {
        long longResult = convertToLong(measurement);
        return Double.longBitsToDouble(longResult);
    }

    public static double convertToDouble(long measurement) {
        return Double.longBitsToDouble(measurement);
    }
}
