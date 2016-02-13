package system;

import framework.FilterFramework;
import framework.MeasurementConfig;
import shared.*;

import java.util.Arrays;

/**
 * Plumber for system B
 *
 * @since 1.0.0
 */
public class SystemBPlumber {

    private static String getBaseFolder(String[] args) {
        if (args.length <= 0) {
            System.out.println("Please provide the absolute execution folder path as the first argument");
            System.exit(-1);
        }
        return args[0];
    }

    public static void main(String[] args) throws Exception {

        /**=========================================================================================================
         * Section A: Create filters
         *
         * The filters to be created are as follows (They are indexed by their filter id):
         * 0 - FileSourceFilter: read source from the file
         * 1 - DataDroppingFilter: drop attitude and velocity measurements
         * 2 - PressureValidityFilter: identity and extrapolate wild pressure points and route them accordingly
         * 3.1 - DataDroppingFilter: (wild stream) drop attitude, velocity, altitude and temperature measurements
         * 4.1 - TimeConvertingFilter: (wild stream) format time measurement
         * 5.1 - PressureFormattingFilter: (wild stream) format pressure measurement
         * 6.1 - FormattingFilter: (wild stream) format time and pressure into a single line
         * 7.1 - FileSinkFilter: (wild stream) read wild points to an output file
         * 3.2 - TimeConvertingFilter: (valid stream) format time measurement
         * 4.2 - TemperatureConvertingFilter: (valid stream) convert temperature from fahrenheit to celsius
         * 5.2 - AltitudeConvertingFilter: (valid stream) convert altitude from feet to meter
         * 6.2 - PressureFormattingFilter: (valid stream) format pressure
         * 7.2 - FormattingFilter: (valid stream) format time, temperature, altitude and pressure into a single line
         * 8.2 - FileSinkFilter: (valid stream) write result to file
         * ==========================================================================================================
         */

        // 0 - FileSourceFilter
        FilterFramework fileSourceFilter = new FileSourceFilter("0", getBaseFolder(args) + "/FlightData.dat");

        // 1 - DataDroppingFilter
        DataDroppingFilter droppingFilter = new DataDroppingFilter("1", MeasurementConfig.defaultConfig());
        droppingFilter.setDropAttitude(true);
        droppingFilter.setDropVelocity(true);

        // 2 - PressureValidityFilter
        PressureValidityFilter splitFilter = new PressureValidityFilter("2", MeasurementConfig.defaultConfig());

        // 3.1 - DataDroppingFilter (wild stream)
        DataDroppingFilter invalidDroppingFilter = new DataDroppingFilter("3.1", MeasurementConfig.defaultConfig());
        invalidDroppingFilter.setDropAttitude(true);
        invalidDroppingFilter.setDropVelocity(true);
        invalidDroppingFilter.setDropAltitude(true);
        invalidDroppingFilter.setDropTemperature(true);

        // 4.1 - TimeConvertingFilter (wild stream)
        TimeConvertingFilter invalidTimeConvertingFilter = new TimeConvertingFilter("4.1", MeasurementConfig.defaultConfig());

        // 5.1 - PressureFormattingFilter (wild stream)
        PressureFormattingFilter invalidPressureFormattingFilter = new PressureFormattingFilter("5.1",
                MeasurementConfig.defaultConfig()
                        .expectTimeWithLength(16));

        // 6.1 - FormattingFilter (wild stream)
        FormattingFilter invalidFormattingFilter = new FormattingFilter("6.1",
                MeasurementConfig.defaultConfig()
                        .expectTimeWithLength(16)
                        .expectPressureWithLength(9));
        invalidFormattingFilter.setTimeRequired(true);
        invalidFormattingFilter.setPressureRequired(true);

        // 7.1 - FileSinkFilter (wild stream)
        FileSinkFilter invalidFileSink = new FileSinkFilter("7.1", getBaseFolder(args) + "/WildPoints.dat");

        // 3.2 - TimeConvertingFilter (valid stream)
        TimeConvertingFilter validTimeConvertingFilter = new TimeConvertingFilter("3.2", MeasurementConfig.defaultConfig());

        // 4.2 - TemperatureConvertingFilter (valid stream)
        TemperatureConvertingFilter validTemperatureConvertingFilter = new TemperatureConvertingFilter("4.2",
                MeasurementConfig.defaultConfig()
                        .expectTimeWithLength(16));

        // 5.2 - AltitudeConvertingFilter (valid stream)
        AltitudeConvertingFilter validAltitudeConvertingFilter = new AltitudeConvertingFilter("5.2",
                MeasurementConfig.defaultConfig()
                        .expectTimeWithLength(16)
                        .expectTemperatureWithLength(10));

        // 6.2 - PressureFormattingFilter (valid stream)
        PressureFormattingFilter validPressureFormattingFilter = new PressureFormattingFilter("6.2",
                MeasurementConfig.defaultConfig()
                        .expectTimeWithLength(16)
                        .expectTemperatureWithLength(10)
                        .expectAltitudeWithLength(13));
        validPressureFormattingFilter.setTreatNegativeValueAsExtrapolated(true);

        // 7.2 - FormattingFilter (valid stream)
        FormattingFilter validFormattingFilter = new FormattingFilter("7.2",
                MeasurementConfig.defaultConfig()
                        .expectTimeWithLength(16)
                        .expectTemperatureWithLength(10)
                        .expectAltitudeWithLength(13)
                        .expectPressureWithLength(9));
        validFormattingFilter.setTimeRequired(true);
        validFormattingFilter.setAltitudeRequired(true);
        validFormattingFilter.setTemperatureRequired(true);
        validFormattingFilter.setPressureRequired(true);

        // 8.2 - FileSinkFilter (valid stream)
        FileSinkFilter validFileSink = new FileSinkFilter("8.2", getBaseFolder(args) + "/OutputB.dat");

        /**==================================================================
         * Section B: Connect the filters
         * Referencing the filters' id, the system will have a topology like:
         *              /-> 3.1 -> 4.1 -> 5.1 -> 6.1 -> 7.1
         * 0 -> 1 -> 2
         *              \-> 3.2 -> 4.2 -> 5.2 -> 6.2 -> 7.2 -> 8.2
         * ==================================================================
         */

        invalidFileSink.connect(invalidFormattingFilter);
        invalidFormattingFilter.connect(invalidPressureFormattingFilter);
        invalidPressureFormattingFilter.connect(invalidTimeConvertingFilter);
        invalidTimeConvertingFilter.connect(invalidDroppingFilter);
        validFileSink.connect(validFormattingFilter);
        validFormattingFilter.connect(validPressureFormattingFilter);
        validPressureFormattingFilter.connect(validAltitudeConvertingFilter);
        validAltitudeConvertingFilter.connect(validTemperatureConvertingFilter);
        validTemperatureConvertingFilter.connect(validTimeConvertingFilter);
        invalidDroppingFilter.connect(splitFilter);
        validTimeConvertingFilter.connect(splitFilter);
        splitFilter.connect(droppingFilter);
        droppingFilter.connect(fileSourceFilter);

        /**============================
         * Section C: Start the filters
         * ============================
         */
        for (Thread filter : Arrays.asList(
                fileSourceFilter,
                droppingFilter,
                splitFilter,
                validTimeConvertingFilter,
                validTemperatureConvertingFilter,
                validAltitudeConvertingFilter,
                validPressureFormattingFilter,
                validFormattingFilter,
                validFileSink,
                invalidDroppingFilter,
                invalidTimeConvertingFilter,
                invalidPressureFormattingFilter,
                invalidFormattingFilter,
                invalidFileSink)) {
            filter.start();
        }
    }
}
