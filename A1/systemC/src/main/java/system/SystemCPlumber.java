package system;

import framework.MeasurementConfig;
import shared.*;

import java.util.Arrays;

/**
 * Plumber for system C
 *
 * @since 1.0.0
 */
public class SystemCPlumber {

    private static String getBaseFolder(String[] args) {
        assert args.length > 0;
        return args[0];
    }

    public static void main(String[] args) throws Exception {

        /**===============================================================================
         * Section A: Create filters
         * The filters to be created are as follows (They are indexed by their filter id):
         * 1 - FileSourceFilter: read data from file
         * 2 - DataDroppingFilter: drop attitude, temperature and velocity data
         * 3 - FileSourceFilter: read data from file
         * 4 - DataDroppingFilter: drop attitude, temperature and velocity data
         * 5 - TimeSortFilter: time align the incoming data
         * 6 - AltitudeFilter: filter out any data frame with altitude less than 10K
         * 7 - DataDroppingFilter: drop pressure data
         * 8 - TimeConvertingFilter: format time
         * 9 - AltitudeFormattingFilter: format altitude
         * 10 - FormattingFilter: format time and altitude into a single line
         * 11 - FileSinkFilter: write to file
         * 12 - DataDroppingFilter: drop altitude data
         * 13 - WildPressureFilter: identity and extrapolate wild pressure points
         * 14 - JunkSinkFilter: discard valid data frame with valid pressure points
         * 15 - TimeConvertingFilter: format time
         * 16 - PressureFormattingFilter: format pressure
         * 17 - FormattingFilter: format time, pressure into a single line
         * 18 - FileSinkFilter: write to file
         * ===============================================================================
         */

        // 1 - FileSourceFilter
        FileSourceFilter fileSourceA = new FileSourceFilter("1", getBaseFolder(args) + "/SubSetA.dat");

        // 2 - DataDroppingFilter
        DataDroppingFilter streamADrop = new DataDroppingFilter("2", MeasurementConfig.defaultConfig());
        streamADrop.setDropAttitude(true);
        streamADrop.setDropTemperature(true);
        streamADrop.setDropVelocity(true);

        // 3  - FileSourceFilter
        FileSourceFilter fileSourceB = new FileSourceFilter("3", getBaseFolder(args) + "/SubSetB.dat");

        // 4 - DataDroppingFilter
        DataDroppingFilter streamBDrop = new DataDroppingFilter("4", MeasurementConfig.defaultConfig());
        streamBDrop.setDropAttitude(true);
        streamBDrop.setDropTemperature(true);
        streamBDrop.setDropVelocity(true);

        // 5 - TimeSortFilter
        TimeSortFilter timeSortFilter = new TimeSortFilter("5", MeasurementConfig.defaultConfig());

        // 6 - AltitudeFilter
        AltitudeFilter altitudeFilter = new AltitudeFilter("6", MeasurementConfig.defaultConfig());

        // 7 - DataDroppingFilter
        DataDroppingFilter dropPressureFilter = new DataDroppingFilter("7", MeasurementConfig.defaultConfig());
        dropPressureFilter.setDropPressure(true);

        // 8 - TimeConvertingFilter
        TimeConvertingFilter timeConvertingFilter1 = new TimeConvertingFilter("8", MeasurementConfig.defaultConfig());

        // 9 - AltitudeFormattingFilter
        AltitudeFormattingFilter altitudeFormattingFilter = new AltitudeFormattingFilter("9",
                MeasurementConfig.defaultConfig().expectTimeWithLength(16));

        // 10 - FormattingFilter
        FormattingFilter formattingFilter1 = new FormattingFilter("10",
                MeasurementConfig.defaultConfig().expectTimeWithLength(16).expectAltitudeWithLength(13));
        formattingFilter1.setTimeRequired(true);
        formattingFilter1.setAltitudeRequired(true);

        // 11 - FileSinkFilter
        FileSinkFilter lessThan10KSink = new FileSinkFilter("11", getBaseFolder(args) + "/LessThan10K.dat");

        // 12 - DataDroppingFilter
        DataDroppingFilter dropAltitudeFilter = new DataDroppingFilter("12", MeasurementConfig.defaultConfig());
        dropAltitudeFilter.setDropAltitude(true);

        // 13 - WildPressureFilter
        WildPressureFilter wildPressureFilter = new WildPressureFilter("13", MeasurementConfig.defaultConfig());

        // 14 - JunkSinkFilter
        JunkSinkFilter junkSinkFilter = new JunkSinkFilter("14");

        // 15 - TimeConvertingFilter
        TimeConvertingFilter timeConvertingFilter2 = new TimeConvertingFilter("15", MeasurementConfig.defaultConfig());

        // 16 - PressureFormattingFilter
        PressureFormattingFilter pressureFormattingFilter = new PressureFormattingFilter("16",
                MeasurementConfig.defaultConfig().expectTimeWithLength(16));

        // 17 - FormattingFilter
        FormattingFilter formattingFilter2 = new FormattingFilter("17",
                MeasurementConfig.defaultConfig().expectTimeWithLength(16).expectPressureWithLength(9));
        formattingFilter2.setTimeRequired(true);
        formattingFilter2.setPressureRequired(true);

        // 18 - FileSinkFilter
        FileSinkFilter pressureWildPointsSink = new FileSinkFilter("18", getBaseFolder(args) + "/PressureWildPoints.dat");


        /**===================================================================
         * Section B: Connect filters.
         * Referencing the filters' id, the system will have a topology like:
         *
         * 1 -> 2 \           /-> 7 -> 8 -> 10 -> 11
         *          -> 5 -> 6
         * 3 -> 4 /           \             /-> 14
         *                     \-> 12 -> 13
         *                                  \-> 15 -> 16 -> 17 -> 18
         * ===================================================================
         */
        lessThan10KSink.connect(formattingFilter1);
        formattingFilter1.connect(altitudeFormattingFilter);
        altitudeFormattingFilter.connect(timeConvertingFilter1);
        timeConvertingFilter1.connect(dropPressureFilter);
        dropPressureFilter.connect(altitudeFilter);
        pressureWildPointsSink.connect(formattingFilter2);
        formattingFilter2.connect(pressureFormattingFilter);
        pressureFormattingFilter.connect(timeConvertingFilter2);
        junkSinkFilter.connect(wildPressureFilter);
        timeConvertingFilter2.connect(wildPressureFilter);
        wildPressureFilter.connect(dropAltitudeFilter);
        dropAltitudeFilter.connect(altitudeFilter);
        altitudeFilter.connect(timeSortFilter);
        timeSortFilter.connect(streamADrop);
        timeSortFilter.connect(streamBDrop);
        streamADrop.connect(fileSourceA);
        streamBDrop.connect(fileSourceB);

        /**========================
         * Section C: Start filters
         * ========================
         */
        for (Thread filter : Arrays.asList(
                fileSourceA,
                fileSourceB,
                streamADrop,
                streamBDrop,
                timeSortFilter,
                altitudeFilter,
                dropPressureFilter,
                timeConvertingFilter1,
                altitudeFormattingFilter,
                formattingFilter1,
                lessThan10KSink,
                dropAltitudeFilter,
                wildPressureFilter,
                junkSinkFilter,
                timeConvertingFilter2,
                pressureFormattingFilter,
                formattingFilter2,
                pressureWildPointsSink
        )) {
            filter.start();
        }
    }
}
