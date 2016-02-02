package util;

import java.nio.ByteBuffer;

/**
 * Data type conversion utility.
 *
 * @since 1.0.0
 */
public class ConversionUtil {

    public static byte[] convertToByteArray(double value) {
        byte[] bytes = new byte[8];
        ByteBuffer.wrap(bytes).putDouble(value);
        return bytes;
    }

    public static byte[] convertToByteArray(int value) {
        byte[] bytes = new byte[4];
        ByteBuffer.wrap(bytes).putInt(value);
        return bytes;
    }

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
