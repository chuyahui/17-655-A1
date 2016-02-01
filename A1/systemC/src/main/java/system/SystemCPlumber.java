package system;

import framework.MeasurementContext;
import shared.DataDroppingFilter;
import shared.DebugPrintToConsoleFilter;
import shared.FileSourceFilter;

/**
 * @author Weinan Qiu
 * @since 1.0.0
 */
public class SystemCPlumber {

    public static void main(String[] args) throws Exception {
        FileSourceFilter fileSourceA = new FileSourceFilter("1.1", "/Users/davidiamyou/Downloads/SubSetA.dat");
        DataDroppingFilter streamADrop = new DataDroppingFilter("2.1", MeasurementContext.defaultContext());
        streamADrop.setDropAttitude(true);
        streamADrop.setDropTemperature(true);
        streamADrop.setDropVelocity(true);

        FileSourceFilter fileSourceB = new FileSourceFilter("1.2", "/Users/davidiamyou/Downloads/SubSetB.dat");
        DataDroppingFilter streamBDrop = new DataDroppingFilter("2.2", MeasurementContext.defaultContext());
        streamBDrop.setDropAttitude(true);
        streamBDrop.setDropTemperature(true);
        streamBDrop.setDropVelocity(true);

        TimeSortFilter timeSortFilter = new TimeSortFilter("3", MeasurementContext.defaultContext());
        DebugPrintToConsoleFilter printToConsoleFilter = new DebugPrintToConsoleFilter("4", MeasurementContext.defaultContext());

        printToConsoleFilter.connect(timeSortFilter);
        timeSortFilter.connect(streamADrop);
        timeSortFilter.connect(streamBDrop);
        streamADrop.connect(fileSourceA);
        streamBDrop.connect(fileSourceB);

        fileSourceA.start();
        fileSourceB.start();
        streamADrop.start();
        streamBDrop.start();
        timeSortFilter.start();
        printToConsoleFilter.start();
    }
}
