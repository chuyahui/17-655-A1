package system;

import framework.AggregatingFilterTemplate;
import framework.MeasurementConfig;
import util.ConversionUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author Weinan Qiu
 * @since 1.0.0
 */
public class TimeSortFilter extends AggregatingFilterTemplate {

    private DataFrame portOneCandidate;
    private DataFrame portTwoCandidate;

    public TimeSortFilter(String filterId, MeasurementConfig context) {
        super(filterId, context);
    }

    @Override
    protected boolean shouldReadFromPortOne() {
        return portOneCandidate == null || !portOneCandidate.hasCollectedAllData();
    }

    @Override
    protected boolean shouldReadFromPortTwo() {
        return portTwoCandidate == null || !portTwoCandidate.hasCollectedAllData();
    }

    @Override
    protected void dataReadForPortOne(int id, byte[] measurement) {
        if (portOneCandidate == null)
            portOneCandidate = new DataFrame();

        if (id == MeasurementConfig.ID_TIME)
            portOneCandidate.time = measurement;
        else if (id == MeasurementConfig.ID_ALTITUDE)
            portOneCandidate.altitude = measurement;
        else if (id == MeasurementConfig.ID_PRESSURE)
            portOneCandidate.pressure = measurement;
        else
            throw new RuntimeException(String.format("[one] data point with id %d should have been discarded already.", id));
    }

    @Override
    protected void dataReadForPortTwo(int id, byte[] measurement) {
        if (portTwoCandidate == null)
            portTwoCandidate = new DataFrame();

        if (id == MeasurementConfig.ID_TIME)
            portTwoCandidate.time = measurement;
        else if (id == MeasurementConfig.ID_ALTITUDE)
            portTwoCandidate.altitude = measurement;
        else if (id == MeasurementConfig.ID_PRESSURE)
            portTwoCandidate.pressure = measurement;
        else
            throw new RuntimeException(String.format("[two] data point with id %d should have been discarded already.", id));
    }

    @Override
    protected boolean hasCompletedAggregation() {
        return portOneCandidate != null &&
                portOneCandidate.hasCollectedAllData() &&
                portTwoCandidate != null &&
                portTwoCandidate.hasCollectedAllData();
    }

    @Override
    protected byte[] aggregatedBytes() throws Exception {
        byte[] bytes;

        if (portOneCandidate.timeInMilliseconds() <= portTwoCandidate.timeInMilliseconds()) {
            bytes = portOneCandidate.getAllBytes();
            portOneCandidate = null;
        } else {
            bytes = portTwoCandidate.getAllBytes();
            portTwoCandidate = null;
        }

        return bytes;
    }

    private static class DataFrame {
        public byte[] time;
        public byte[] altitude;
        public byte[] pressure;

        public boolean hasCollectedAllData() {
            return time != null && altitude != null && pressure != null;
        }

        public long timeInMilliseconds() {
            return ConversionUtil.convertToLong(time);
        }

        public byte[] getAllBytes() throws IOException {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.write(ConversionUtil.convertToByteArray(MeasurementConfig.ID_TIME));
            out.write(time);
            out.write(ConversionUtil.convertToByteArray(MeasurementConfig.ID_ALTITUDE));
            out.write(altitude);
            out.write(ConversionUtil.convertToByteArray(MeasurementConfig.ID_PRESSURE));
            out.write(pressure);
            return out.toByteArray();
        }
    }
}
