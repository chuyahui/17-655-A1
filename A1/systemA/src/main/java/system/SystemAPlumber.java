package system;

import framework.FilterFramework;
import framework.MeasurementConfig;
import shared.*;

/**
 * Plumber for system A.
 *
 * @since 1.0.0
 */
public class SystemAPlumber {

    private static String getBaseFolder(String[] args) {
        assert args.length > 0;
        return args[0];
    }

    public static void main(String[] args) throws Exception {

        /**================================================================================
         * Section A: create filters
         *
         * The filters to be created are as follows (They are indexed by their filter id):
         * 0 - FileSourceFilter: reads input file
         * 1 - DataDroppingFilter: drop attitude, pressure and velocity measurements
         * 2 - TimeConvertingFilter: convert time to string format
         * 3 - TemperatureConvertingFilter: convert temperature from fahrenheit to celsius
         * 4 - AltitudeConvertingFilter: convert altitude from feet to meter
         * 5 - FormattingFilter: format time, temperature and altitude into one line
         * 6 - FileSinkFilter: write output to file
         * ================================================================================
         */
        // 0 - FileSourceFilter
        FilterFramework fileSourceFilter = new FileSourceFilter("0", getBaseFolder(args) + "/FlightData.dat");

        // 1 - DataDroppingFilter
        DataDroppingFilter droppingFilter = new DataDroppingFilter("1", MeasurementConfig.defaultConfig());
        droppingFilter.setDropAttitude(true);
        droppingFilter.setDropPressure(true);
        droppingFilter.setDropVelocity(true);

        // 2 - TimeConvertingFilter
        FilterFramework timeFilter = new TimeConvertingFilter("2",
                MeasurementConfig.defaultConfig());                 // from this point on, time will have length 16

        // 3 - TemperatureConvertingFilter
        FilterFramework temperatureFilter = new TemperatureConvertingFilter("3",
                MeasurementConfig.defaultConfig()
                        .expectTimeWithLength(16));                 // from this point on, temperature will have length 10

        // 4 - AltitudeConvertingFilter
        FilterFramework altitudeFilter = new AltitudeConvertingFilter("4",
                MeasurementConfig.defaultConfig()
                        .expectTimeWithLength(16)
                        .expectTemperatureWithLength(10));          // from this point on, altitude will have length 13

        // 5 - FormattingFilter
        FormattingFilter formattingFilter = new FormattingFilter("5",
                MeasurementConfig.defaultConfig()
                        .expectTimeWithLength(16)
                        .expectTemperatureWithLength(10)
                        .expectAltitudeWithLength(13)
        );
        formattingFilter.setTimeRequired(true);
        formattingFilter.setAltitudeRequired(true);
        formattingFilter.setTemperatureRequired(true);

        // 6 - FileSinkFilter
        FilterFramework fileSinkFilter = new FileSinkFilter("6", getBaseFolder(args) + "/OutputA.dat");

        /**==================================================================
         * Section B: connect filters
         *
         * Referencing the filters' id, the system will have a topology like:
         * 0 -> 1 -> 2 -> 3 -> 4 -> 5 -> 6
         * ==================================================================
         */
        fileSinkFilter.connect(formattingFilter);
        formattingFilter.connect(altitudeFilter);
        altitudeFilter.connect(temperatureFilter);
        temperatureFilter.connect(timeFilter);
        timeFilter.connect(droppingFilter);
        droppingFilter.connect(fileSourceFilter);

        /**========================
         * Section C: start filters
         * ========================
         */
        fileSourceFilter.start();
        droppingFilter.start();
        timeFilter.start();
        temperatureFilter.start();
        altitudeFilter.start();
        formattingFilter.start();
        fileSinkFilter.start();
    }
}
