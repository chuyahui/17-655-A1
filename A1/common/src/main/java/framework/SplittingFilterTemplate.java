package framework;

import util.ConversionUtil;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * A filter template than represents a splitting filter. A Splitting filter is a filter that reads from only one input
 * port but writes to two output ports (In theory, it can have N output ports but our implementation will restrict it
 * to two output ports for simplicity and readability of API).
 *
 * @since 1.0.0
 */
public abstract class SplittingFilterTemplate extends FilterFramework {

    /**
     * The configuration object containing the length information of byte data.
     */
    protected final MeasurementConfig context;

    /**
     * The id of the input filter. It is used to lookup {@link java.io.PipedInputStream} by calling {@link #inputForKey(String)}.
     */
    protected String inputFilterId = null;

    /**
     * The ids of the output filters. The id of the output filter from the upper stream will be placed at index 0.
     * The id of the output filter from the lower stream will be placed at index 1.
     */
    protected List<String> outputFilterIds = new ArrayList<String>();

    /**
     * Default constructor.
     *
     * @param filterId the filter id of this filter.
     * @param context the configuration object containing the length information for byte data.
     */
    public SplittingFilterTemplate(String filterId, MeasurementConfig context) {
        super(filterId);
        this.context = context;
    }

    /**
     * Perform connection and do an integrity check.
     *
     * @param Filter the filter requesting connection to this filter as input
     */
    @Override
    public void connect(FilterFramework Filter) {
        if (inputs.size() >= 1)
            throw new RuntimeException("A splitting filter can only accept 1 connection");
        super.connect(Filter);
    }

    /**
     * Register the input filter id and do integrity check.
     *
     * @param inputFilter the input filter connected
     */
    @Override
    protected void inputConnected(FilterFramework inputFilter) {
        super.inputConnected(inputFilter);
        if (this.inputFilters.size() >= 1 && !this.inputFilters.keySet().contains(inputFilter.filterId))
            throw new RuntimeException("A splitting filter can only accept 1 connection");
        this.inputFilterId = inputFilter.filterId;
    }

    /**
     * Registry the output filter ids and do integrity check.
     *
     * @param outputFilter the output filter connected
     */
    @Override
    protected void outputConnected(FilterFramework outputFilter) {
        if (this.outputFilterIds.size() >= 2)
            throw new RuntimeException("A splitting filter can only accept 2 connection");
        this.outputFilterIds.add(outputFilter.filterId);
    }

    /**
     * Utility method to read from the {@link java.io.PipedInputStream} represented by {@link #inputFilterId}.
     *
     * @return a byte of data read from the input port
     * @throws EndOfStreamException
     */
    protected byte ReadFilterInputPort() throws EndOfStreamException {
        return readFromInput(inputFilterId);
    }

    /**
     * Utility method write to the {@link java.io.PipedOutputStream} connecting the output filter in the upper stream.
     *
     * @param data a byte of data to be written to the output port.
     */
    protected void WriteFilterOutputPortOne(byte data) {
        writeToOutput(data, outputFilterIds.get(0));
    }

    /**
     * Utility method write to the {@link java.io.PipedOutputStream} connecting the output filter in the lower stream.
     *
     * @param data a byte of data to be written to the output port.
     */
    protected void WriteFilterOutputPortTwo(byte data) {
        writeToOutput(data, outputFilterIds.get(1));
    }

    /**
     * Utility method to read id.
     *
     * @return byte array of id bytes.
     * @throws EndOfStreamException
     */
    private byte[] readId() throws EndOfStreamException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (int i = 0; i < context.getIdLength(); i++) {
            byte databyte = ReadFilterInputPort();
            out.write(databyte);
        }
        return out.toByteArray();
    }

    /**
     * Utility method to read measurement.
     *
     * @param length length of the measurement to read.
     * @return byte array of the measurement data.
     * @throws EndOfStreamException
     */
    private byte[] readMeasurement(int length) throws EndOfStreamException {
        byte[] measurement = new byte[length];
        for (int i = 0; i < length; i++) {
            measurement[i] = ReadFilterInputPort();
        }
        return measurement;
    }

    /**
     * Template method for the subclasses to implement. The subclasses are expected to perform routing logic here and
     * write data to output ports using {@link #WriteFilterOutputPortOne(byte)} and {@link #WriteFilterOutputPortTwo(byte)}.
     *
     * @param id id data bytes
     * @param measurement measurement data bytes
     */
    protected abstract void routeMeasurement(byte[] id, byte[] measurement);

    /**
     * Callback method for subclasses to implement. It provides an opportunity for subclasses to react to the event
     * of input port closing. Subclasses, for instance, may choose to flush any remaining data cache to the output
     * port.
     */
    protected abstract void reachedEndOfStream();

    /**
     * Main execution method for this filter. It attempts to read id and measurement and defer to subclasses for any
     * routing logic. In the case of input port closing, it also notifies the subclass of the event before closing all
     * ports.
     */
    @Override
    public void run() {
        while (true) {
            try {
                // read id
                byte[] idBytes = readId();
                int id = ConversionUtil.convertToInt(idBytes);

                // read measurement
                byte[] measurement = readMeasurement(context.idForMeasurementLength(id));

                // route data
                routeMeasurement(idBytes, measurement);
            } catch (EndOfStreamException ex) {
                // notify input port closing
                reachedEndOfStream();

                // close input and output ports.
                closeAllPorts();
                break;
            }
        }
    }
}
