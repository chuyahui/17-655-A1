package system;

import framework.MeasurementConfig;
import shared.*;

import java.util.Arrays;

/**
 * @author Weinan Qiu
 * @since 1.0.0
 */
public class SystemCPlumber {

    public static void main(String[] args) throws Exception {
        FileSourceFilter fileSourceA = new FileSourceFilter("1", "/Users/davidiamyou/Downloads/SubSetA.dat");
        DataDroppingFilter streamADrop = new DataDroppingFilter("2", MeasurementConfig.defaultConfig());
        streamADrop.setDropAttitude(true);
        streamADrop.setDropTemperature(true);
        streamADrop.setDropVelocity(true);

        FileSourceFilter fileSourceB = new FileSourceFilter("3", "/Users/davidiamyou/Downloads/SubSetB.dat");
        DataDroppingFilter streamBDrop = new DataDroppingFilter("4", MeasurementConfig.defaultConfig());
        streamBDrop.setDropAttitude(true);
        streamBDrop.setDropTemperature(true);
        streamBDrop.setDropVelocity(true);

        TimeSortFilter timeSortFilter = new TimeSortFilter("5", MeasurementConfig.defaultConfig());
        AltitudeFilter altitudeFilter = new AltitudeFilter("6", MeasurementConfig.defaultConfig());


        DataDroppingFilter dropPressureFilter = new DataDroppingFilter("7", MeasurementConfig.defaultConfig());
        dropPressureFilter.setDropPressure(true);
        TimeConvertingFilter timeConvertingFilter1 = new TimeConvertingFilter("8", MeasurementConfig.defaultConfig());
        AltitudeFormattingFilter altitudeFormattingFilter = new AltitudeFormattingFilter("9",
                MeasurementConfig.defaultConfig().expectTimeWithLength(16));
        FormattingFilter formattingFilter1 = new FormattingFilter("10",
                MeasurementConfig.defaultConfig().expectTimeWithLength(16).expectAltitudeWithLength(13));
        formattingFilter1.setTimeRequired(true);
        formattingFilter1.setAltitudeRequired(true);
        FileSinkFilter lessThan10KSink = new FileSinkFilter("11", "/Users/davidiamyou/Downloads/LessThan10K.dat");

        DataDroppingFilter dropAltitudeFilter = new DataDroppingFilter("12", MeasurementConfig.defaultConfig());
        dropAltitudeFilter.setDropAltitude(true);
        WildPressureFilter wildPressureFilter = new WildPressureFilter("13", MeasurementConfig.defaultConfig());
        JunkSinkFilter junkSinkFilter = new JunkSinkFilter("14");
        TimeConvertingFilter timeConvertingFilter2 = new TimeConvertingFilter("15", MeasurementConfig.defaultConfig());
        PressureFormattingFilter pressureFormattingFilter = new PressureFormattingFilter("16",
                MeasurementConfig.defaultConfig().expectTimeWithLength(16));
        FormattingFilter formattingFilter2 = new FormattingFilter("17",
                MeasurementConfig.defaultConfig().expectTimeWithLength(16).expectPressureWithLength(9));
        formattingFilter2.setTimeRequired(true);
        formattingFilter2.setPressureRequired(true);
        FileSinkFilter pressureWildPointsSink = new FileSinkFilter("18", "/Users/davidiamyou/Downloads/PressureWildPoints.dat");

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
