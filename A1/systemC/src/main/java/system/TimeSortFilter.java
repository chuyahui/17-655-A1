package system;

import framework.AggregatingFilterTemplate;
import framework.MeasurementConfig;
import util.ConversionUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * An aggregating filter that reads frames of data from two input ports and sort them based on their time. This
 * filter assumes only time, altitude and pressure data is passed in and all other measurements should have already been
 * dropped.
 *
 * The logic used in sorting the data frame are as follows:
 * - read data frame from upper port if we don't have a cache for data frame from upper port
 * - read data frame from lower port if we don't have a cache for data frame from lower port
 * - compare the time inside the two cached data frames
 * - send the data frame cache with smaller time value to the output port and clear that cache
 * - read data from the port whose corresponding cache is missing
 * - repeat the process
 *
 * When one input port has ended (it will only happen when the filter is actively reading a port and therefore the corresponding
 * cache must be empty), the filter will try to release the cache for the other port and also any remaining streams of
 * data from the other port.
 *
 * @since 1.0.0
 */
public class TimeSortFilter extends AggregatingFilterTemplate {

    /**
     * cache for data frame from the upper input port
     */
    private DataFrame portOneCandidate;

    /**
     * cache for data frame from the lower input port
     */
    private DataFrame portTwoCandidate;

    public TimeSortFilter(String filterId, MeasurementConfig context) {
        super(filterId, context);
    }

    /**
     * Instruct the super class to read from the upper input port. It should read if the cache is not filled up yet.
     *
     * @return whether to read from upper input port
     */
    @Override
    protected boolean shouldReadFromPortOne() {
        return portOneCandidate == null || !portOneCandidate.hasCollectedAllData();
    }

    /**
     * Instruct the super class to read from the lower input port. It should read if the cache is not filled up yet.
     *
     * @return whether to read from lower input port
     */
    @Override
    protected boolean shouldReadFromPortTwo() {
        return portTwoCandidate == null || !portTwoCandidate.hasCollectedAllData();
    }

    /**
     * Fill up the data frame with data read from upper port
     *
     * @param id id of data
     * @param measurement measurement data
     */
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

    /**
     * Fill up the data frame with data read from lower port
     *
     * @param id id of data
     * @param measurement measurement data
     */
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

    /**
     * Instruct the super class whether the aggregation process has finished. It is finished when we have both
     * data frame and are able to do a comparison based on time.
     *
     * @return whether we can processed to grab {@link #aggregatedBytes()}
     */
    @Override
    protected boolean hasCompletedAggregation() {
        return portOneCandidate != null &&
                portOneCandidate.hasCollectedAllData() &&
                portTwoCandidate != null &&
                portTwoCandidate.hasCollectedAllData();
    }

    /**
     * Do the comparison between the time of both data frames and return the cache of that data frame. Also clear that
     * cache.
     *
     * @return bytes of the data frame to be flushed to the output
     * @throws Exception
     */
    @Override
    protected byte[] aggregatedBytes() throws Exception {
        byte[] bytes;

        // do comparison
        if (portOneCandidate.timeInMilliseconds() <= portTwoCandidate.timeInMilliseconds()) {
            bytes = portOneCandidate.getAllBytes();
            portOneCandidate = null;        // clear cache
        } else {
            bytes = portTwoCandidate.getAllBytes();
            portTwoCandidate = null;        // clear cache
        }

        return bytes;
    }

    /**
     * When upper stream has ended, we should flush the lower cache immediately and also pass on any remaining data
     * from the lower stream until its end of stream.
     *
     * @throws EndOfStreamException
     */
    @Override
    protected void portOneStreamHasEnded() throws EndOfStreamException {
        try {
            if (portTwoCandidate != null) {
                for (byte eachByte : portTwoCandidate.getAllBytes())
                    WriteFilterOutputPort(eachByte);
                portTwoCandidate = null;
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        super.portOneStreamHasEnded();
    }

    /**
     * When lower stream has ended, we should flush the upper cache immediately and also pass on any remaining data
     * from the upper stream until its end of stream.
     *
     * @throws EndOfStreamException
     */
    @Override
    protected void portTwoStreamHasEnded() throws EndOfStreamException {
        try {
            if (portOneCandidate != null) {
                for (byte eachByte : portOneCandidate.getAllBytes())
                    WriteFilterOutputPort(eachByte);
                portOneCandidate = null;
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        super.portTwoStreamHasEnded();
    }

    /**
     * Structure for holding data frame of time, altitude and pressure.
     */
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
