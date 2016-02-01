package framework;

import util.ConversionUtil;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * A filter template than represents an aggregating filter. A aggregating filter is a filter that reads from two input
 * ports but writes to only one output port (In theory, it can have N input ports but our implementation will restrict it
 * to two input ports for simplicity and readability of API).
 *
 * @since 1.0.0
 */
public abstract class AggregatingFilterTemplate extends FilterFramework {

    /**
     * The configuration object containing the length information of byte data.
     */
    protected final MeasurementConfig context;

    /**
     * The ids of the input filters. The id of the input filter from the upper stream will be placed at index 0.
     * The id of the input filter from the lower stream will be placed at index 1.
     */
    protected List<String> inputFilterIds = new ArrayList<String>();

    /**
     * The id of the output filter. It is used to lookup {@link java.io.PipedOutputStream} by calling {@link #outputForKey(String)}.
     */
    protected String outputFilterId = null;

    /**
     * Default constructor
     *
     * @param filterId the filter id of this filter.
     * @param context the configuration object containing the length information for byte data.
     */
    public AggregatingFilterTemplate(String filterId, MeasurementConfig context) {
        super(filterId);
        this.context = context;
    }

    /**
     * Perform connection and integrity check
     *
     * @param Filter the filter requesting connection to this filter as input
     */
    @Override
    public void connect(FilterFramework Filter) {
        if (inputs.size() >= 2)
            throw new RuntimeException("An aggregating filter can only accept 2 connections");
        super.connect(Filter);
    }

    /**
     * Register input filter ids and do integrity check
     *
     * @param inputFilter the input filter connected
     */
    @Override
    protected void inputConnected(FilterFramework inputFilter) {
        super.inputConnected(inputFilter);
        if (this.inputFilters.size() > 2)
            throw new RuntimeException("An aggregating filter can only accept 2 connections");
        this.inputFilterIds.add(inputFilter.filterId);
    }

    /**
     * Register output fitler id and do integrity check
     *
     * @param outputFilter the output filter connected
     */
    @Override
    protected void outputConnected(FilterFramework outputFilter) {
        if (this.outputFilterId != null)
            throw new RuntimeException("An aggregating filter can only connect to 1 filter");
        this.outputFilterId = outputFilter.filterId;
    }

    /**
     * Utility method to read from the input port from the upper stream.
     *
     * @return byte data read
     * @throws EndOfStreamException
     */
    protected byte ReadFilterInputPortOne() throws EndOfStreamException {
        return readFromInput(inputFilterIds.get(0));
    }

    /**
     * Utility method to read from the input port from the lower stream.
     *
     * @return byte data read
     * @throws EndOfStreamException
     */
    protected byte ReadFilterInputPortTwo() throws EndOfStreamException {
        return readFromInput(inputFilterIds.get(1));
    }

    /**
     * Utility method to write to output port.
     *
     * @param data data to be written to output
     */
    protected void WriteFilterOutputPort(byte data) {
        writeToOutput(data, outputFilterId);
    }

    /**
     * Utility method to read id from upper port.
     *
     * @return id bytes
     * @throws EndOfStreamException
     */
    protected byte[] readIdFromInputPortOne() throws EndOfStreamException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (int i = 0; i < context.getIdLength(); i++) {
            byte databyte = ReadFilterInputPortOne();
            out.write(databyte);
        }
        return out.toByteArray();
    }

    /**
     * Utility method to read measurement from upper port.
     *
     * @param length length of the measurement to be read
     * @return measurement bytes
     * @throws EndOfStreamException
     */
    protected byte[] readMeasurementFromInputPortOne(int length) throws EndOfStreamException {
        byte[] measurement = new byte[length];
        for (int i = 0; i < length; i++) {
            measurement[i] = ReadFilterInputPortOne();
        }
        return measurement;
    }

    /**
     * Utility method to read id from lower port.
     *
     * @return id bytes
     * @throws EndOfStreamException
     */
    protected byte[] readIdFromInputPortTwo() throws EndOfStreamException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (int i = 0; i < context.getIdLength(); i++) {
            byte databyte = ReadFilterInputPortTwo();
            out.write(databyte);
        }
        return out.toByteArray();
    }

    /**
     * Utility method to read measurement from lower port.
     *
     * @param length length of the measurement to be read
     * @return measurement bytes
     * @throws EndOfStreamException
     */
    protected byte[] readMeasurementFromInputPortTwo(int length) throws EndOfStreamException {
        byte[] measurement = new byte[length];
        for (int i = 0; i < length; i++) {
            measurement[i] = ReadFilterInputPortTwo();
        }
        return measurement;
    }

    /**
     * Template method for subclasses to implement. This method instructs the filter to read from upper port.
     *
     * @return whether or not filter should read from upper port.
     */
    protected abstract boolean shouldReadFromPortOne();

    /**
     * Template method for subclasses to implement. This method instructs the filter to read from lower port.
     *
     * @return whether or not filter should read from lower port.
     */
    protected abstract boolean shouldReadFromPortTwo();

    /**
     * Template method for subclasses to implement. It passes the id and measurement read from upper port to the subclass
     * for any processing it provides.
     *
     * @param id id of data
     * @param measurement measurement data
     */
    protected abstract void dataReadForPortOne(int id, byte[] measurement);

    /**
     * Template method for subclasses to implement. It passes the id and measurement read from lower port to the subclass
     * for any processing it provides.
     *
     * @param id id of data
     * @param measurement measurement data
     */
    protected abstract void dataReadForPortTwo(int id, byte[] measurement);

    /**
     * Template method for subclasses to implement. This instructs the filter that subclasses have finished doing
     * aggregation and it can call {@link #aggregatedBytes()} to retrieve the aggregated result.
     *
     * @return whether or not the filter should call {@link #aggregatedBytes()}
     */
    protected abstract boolean hasCompletedAggregation();

    /**
     * Template method for subclasses to implement. This method should return the aggregated bytes corresponding to the last
     * {@link #hasCompletedAggregation()} call.
     *
     * @return aggregated bytes to be flushed to the output port.
     * @throws Exception
     */
    protected abstract byte[] aggregatedBytes() throws Exception;

    /**
     * Event call back method for the event that upper input port has closed. By default, it would read all data
     * from the lower input port and flushed it to the output port immediately. Subclasses may provide reasonable
     * override.
     *
     * @throws EndOfStreamException
     */
    protected void portOneStreamHasEnded() throws EndOfStreamException {
        while (true) {
            WriteFilterOutputPort(ReadFilterInputPortTwo());
        }
    }

    /**
     * Event call back method for the event that lower input port has closed. By default, it would read all data
     * from the upper input port and flushed it to the output port immediately. Subclasses may provide reasonable
     * override.
     *
     * @throws EndOfStreamException
     */
    protected void portTwoStreamHasEnded() throws EndOfStreamException {
        while (true) {
            WriteFilterOutputPort(ReadFilterInputPortOne());
        }
    }

    /**
     * Main execution method for this filter. It relies on the subclasses for instructions to read from the upper
     * port or the lower port. And it obtains the aggregated data from the subclass as it becomes available. THen
     * it flushes the aggregated data to its output port.
     *
     * In the case that one input port has closed, it will try to call {@link #portOneStreamHasEnded()} or
     * {@link #portTwoStreamHasEnded()}, which by default just flushes data from the other input port to the
     * output port immediately. When the other port has also depleted, it closes all ports.
     */
    @Override
    public void run() {
        while (true) {
            try {
                // read id and measurement from upper port if necessary and pass on to subclass for any processing
                if (shouldReadFromPortOne()) {
                    byte[] idBytes = readIdFromInputPortOne();
                    int id = ConversionUtil.convertToInt(idBytes);
                    byte[] measurement = readMeasurementFromInputPortOne(context.idForMeasurementLength(id));
                    dataReadForPortOne(id, measurement);
                }

                // read id and measurement from lower port if necessary and pass on to subclass for any processing
                if (shouldReadFromPortTwo()) {
                    byte[] idBytes = readIdFromInputPortTwo();
                    int id = ConversionUtil.convertToInt(idBytes);
                    byte[] measurement = readMeasurementFromInputPortTwo(context.idForMeasurementLength(id));
                    dataReadForPortTwo(id, measurement);
                }

                // pass the aggregated data onto the output port if it has finished aggregating.
                if (hasCompletedAggregation()) {
                    byte[] bytesToFlush = aggregatedBytes();
                    for (byte eachByte : bytesToFlush)
                        WriteFilterOutputPort(eachByte);
                }
            } catch (Exception ex) {
                if (ex instanceof EndOfStreamException) {
                    String inputOneKey = inputFilterIds.get(0);
                    String inputTwoKey = inputFilterIds.get(1);

                    try {
                        // if upper input port has closed, but lower input port is still open
                        if (endOfInputForKey(inputOneKey) && !endOfInputForKey(inputTwoKey)) {
                            portOneStreamHasEnded();
                            closeInputForKey(inputOneKey);
                        }
                        // if lower input port has closed, but upper input port is still open
                        else if (endOfInputForKey(inputTwoKey) && !endOfInputForKey(inputOneKey)) {
                            portTwoStreamHasEnded();
                            closeInputForKey(inputTwoKey);
                        }
                        // if both ports have closed
                        else {
                            closeAllPorts();
                            break;
                        }
                    } catch (EndOfStreamException eos) {
                        // close all ports and leave
                        closeAllPorts();
                        break;
                    }
                }
            }
        }
    }
}
