package system;

import framework.FilterFramework;
import framework.MeasurementContext;
import shared.DataDroppingFilter;
import shared.FileSourceFilter;

/**
 * @author Weinan Qiu
 * @since 1.0.0
 */
public class SystemBPlumber {

    public static void main(String[] args) throws Exception {
        FilterFramework fileSourceFilter = new FileSourceFilter("0", "/Users/davidiamyou/Downloads/FlightData.dat");
        DataDroppingFilter droppingFilter = new DataDroppingFilter("1", MeasurementContext.defaultContext());
        droppingFilter.setDropAttitude(true);
        droppingFilter.setDropVelocity(true);
        PressureValidityFilter splitFilter = new PressureValidityFilter("2", MeasurementContext.defaultContext());
        DebugPrintToConsoleFilter invalidPrinter = new DebugPrintToConsoleFilter("3", MeasurementContext.defaultContext());
        DebugPrintToConsoleFilter validPrinter = new DebugPrintToConsoleFilter("4", MeasurementContext.defaultContext());

        invalidPrinter.connect(splitFilter);
        validPrinter.connect(splitFilter);
        splitFilter.connect(droppingFilter);
        droppingFilter.connect(fileSourceFilter);

        fileSourceFilter.start();
        droppingFilter.start();
        splitFilter.start();
        validPrinter.start();
        invalidPrinter.start();
    }
}
