package system;

import framework.FilterFramework;
import framework.MeasurementConfig;
import shared.*;

import java.util.Arrays;

/**
 * @author Weinan Qiu
 * @since 1.0.0
 */
public class SystemBPlumber {

    public static void main(String[] args) throws Exception {
        FilterFramework fileSourceFilter = new FileSourceFilter("0", "/Users/davidiamyou/Downloads/FlightData.dat");
        DataDroppingFilter droppingFilter = new DataDroppingFilter("1", MeasurementConfig.defaultConfig());
        droppingFilter.setDropAttitude(true);
        droppingFilter.setDropVelocity(true);

        PressureValidityFilter splitFilter = new PressureValidityFilter("2", MeasurementConfig.defaultConfig());

        DataDroppingFilter invalidDroppingFilter = new DataDroppingFilter("3.1", MeasurementConfig.defaultConfig());
        invalidDroppingFilter.setDropAttitude(true);
        invalidDroppingFilter.setDropVelocity(true);
        invalidDroppingFilter.setDropAltitude(true);
        invalidDroppingFilter.setDropTemperature(true);
        TimeConvertingFilter invalidTimeConvertingFilter = new TimeConvertingFilter("4.1", MeasurementConfig.defaultConfig());
        PressureFormattingFilter invalidPressureFormattingFilter = new PressureFormattingFilter("5.1",
                MeasurementConfig.defaultConfig()
                        .expectTimeWithLength(16));
        FormattingFilter invalidFormattingFilter = new FormattingFilter("6.1",
                MeasurementConfig.defaultConfig()
                        .expectTimeWithLength(16)
                        .expectPressureWithLength(9));
        invalidFormattingFilter.setTimeRequired(true);
        invalidFormattingFilter.setPressureRequired(true);
        FileSinkFilter invalidFileSink = new FileSinkFilter("7.1", "/Users/davidiamyou/Downloads/WildPoints.dat");

        /********************************************************************
         * Create filters for the valid stream
         ********************************************************************/
        TimeConvertingFilter validTimeConvertingFilter = new TimeConvertingFilter("3.2", MeasurementConfig.defaultConfig());
        TemperatureConvertingFilter validTemperatureConvertingFilter = new TemperatureConvertingFilter("4.2",
                MeasurementConfig.defaultConfig()
                        .expectTimeWithLength(16));
        AltitudeConvertingFilter validAltitudeConvertingFilter = new AltitudeConvertingFilter("5.2",
                MeasurementConfig.defaultConfig()
                        .expectTimeWithLength(16)
                        .expectTemperatureWithLength(10));
        PressureFormattingFilter validPressureFormattingFilter = new PressureFormattingFilter("6.2",
                MeasurementConfig.defaultConfig()
                        .expectTimeWithLength(16)
                        .expectTemperatureWithLength(10)
                        .expectAltitudeWithLength(13));
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
        FileSinkFilter validFileSink = new FileSinkFilter("8.2", "/Users/davidiamyou/Downloads/OutputB.dat");

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
