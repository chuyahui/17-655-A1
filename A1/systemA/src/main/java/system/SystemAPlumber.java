package system;

import framework.FilterFramework;
import framework.MeasurementContext;
import shared.DataDroppingFilter;
import shared.FileSinkFilter;
import shared.FileSourceFilter;
import shared.FormattingFilter;

/**
 * @author Weinan Qiu
 * @since 1.0.0
 */
public class SystemAPlumber {

    public static void main(String[] args) throws Exception {
        FilterFramework fileSourceFilter = new FileSourceFilter("0", "/Users/davidiamyou/Downloads/FlightData.dat");
        DataDroppingFilter droppingFilter = new DataDroppingFilter("1", MeasurementContext.defaultContext());
        droppingFilter.setDropAttitude(true);
        droppingFilter.setDropPressure(true);
        droppingFilter.setDropVelocity(true);
        FilterFramework timeFilter = new TimeConvertingFilter("2",
                MeasurementContext.defaultContext());
        FilterFramework temperatureFilter = new TemperatureConvertingFilter("3",
                MeasurementContext.defaultContext()
                        .expectTimeWithLength(16));
        FilterFramework altitudeFilter = new AltitudeConvertingFilter("4",
                MeasurementContext.defaultContext()
                        .expectTimeWithLength(16)
                        .expectTemperatureWithLength(10));
        FormattingFilter formattingFilter = new FormattingFilter("5",
                MeasurementContext.defaultContext()
                        .expectTimeWithLength(16)
                        .expectTemperatureWithLength(10)
                        .expectAltitudeWithLength(13)
        );
        formattingFilter.setTimeRequired(true);
        formattingFilter.setAltitudeRequired(true);
        formattingFilter.setTemperatureRequired(true);
        FilterFramework fileSinkFilter = new FileSinkFilter("6", "/Users/davidiamyou/Downloads/SystemAResult.dat");

        fileSinkFilter.connect(formattingFilter);
        formattingFilter.connect(altitudeFilter);
        altitudeFilter.connect(temperatureFilter);
        temperatureFilter.connect(timeFilter);
        timeFilter.connect(droppingFilter);
        droppingFilter.connect(fileSourceFilter);

        fileSourceFilter.start();
        droppingFilter.start();
        timeFilter.start();
        temperatureFilter.start();
        altitudeFilter.start();
        formattingFilter.start();
        fileSinkFilter.start();
    }
}
