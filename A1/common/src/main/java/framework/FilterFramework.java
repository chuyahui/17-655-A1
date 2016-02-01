package framework;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is the base class for all filters. It defines multiple input ports and output ports. Subclasses are
 * expected to constrain the number of ports available and encapsulate their behavior. It also defines how output ports
 * of one filter can be connect to input ports of another filter.
 *
 * @since 1.0.0
 */
public abstract class FilterFramework extends Thread {

    /**
     * The id of this filter. It will be used as the name of the thread and as they key to look up
     * input ports and output ports from the port registries.
     */
    protected final String filterId;

    /**
     * The input port registry of this filter. The registry is keyed by the filterId of the connecting filter.
     */
    protected Map<String, PipedInputStream> inputs = new ConcurrentHashMap<String, PipedInputStream>();

    /**
     * The output port registry of this filter. The registry is keyed by the filterId of the connecting filter.
     */
    protected Map<String, PipedOutputStream> outputs = new ConcurrentHashMap<String, PipedOutputStream>();

    /**
     * The input filter registry of this filter. The registry is keyed by the filterId of the connecting filter.
     */
    protected Map<String, FilterFramework> inputFilters = new HashMap<String, FilterFramework>();

    /**
     * Default constructor of the filter framework.
     *
     * @param filterId the id of this filter
     */
    public FilterFramework(String filterId) {
        this.filterId = filterId;
        this.setName("Thread-" + filterId);
    }

    /**
     * Connection event callback for the output filter. By default, it just updates the filter registry. Subclasses
     * are expected to extend its functionality.
     *
     * @param inputFilter the input filter connected
     */
    protected void inputConnected(FilterFramework inputFilter) {
        inputFilters.put(inputFilter.filterId, inputFilter);
    }

    /**
     * Connection event callback for the input filter. Subclasses are expected to extend its functionality.
     *
     * @param outputFilter the output filter connected
     */
    protected void outputConnected(FilterFramework outputFilter) {
        // do nothing
    }

    /**
     * Connect the filter represented by the {@code Filter} object as the input to the filter represented by
     * {@code this}. Upon connecting, a new {@link PipedInputStream} is constructed and entered into the input
     * registry with the {@link FilterFramework#filterId} of the {@code Filter} object. A new {@link PipedOutputStream}
     * is also constructed and entered into the output registry of {@code Filter} keyed by the {@link FilterFramework#filterId}
     * of {@code this} object. Then the {@link PipedInputStream} and {@link PipedOutputStream} are connected together.
     * Finally, event callbacks like {@link #inputConnected(FilterFramework)} and {@link #outputConnected(FilterFramework)}
     * are notified so the subclasses can react to the connection event.
     *
     * @param Filter the filter requesting connection to this filter as input
     */
    public void connect(FilterFramework Filter) {
        // update input registry
        if (!this.inputs.containsKey(Filter.filterId))
            this.inputs.put(Filter.filterId, new PipedInputStream());

        // update the connecting filter's output registry
        if (!Filter.outputs.containsKey(this.filterId))
            Filter.outputs.put(this.filterId, new PipedOutputStream());

        try {
            // connect the two piped streams
            PipedInputStream InputReadPort = inputForKey(Filter.filterId);
            PipedOutputStream OutputWritePort = Filter.outputForKey(this.filterId);
            InputReadPort.connect(OutputWritePort);

            // notify event callbacks
            this.inputConnected(Filter);
            Filter.outputConnected(this);
        } catch(Exception Error) {
            System.out.println( "\n" + this.getName() + " FilterFramework error connecting::"+ Error );
        }
    }

    /**
     * Read a byte of data from the {@link PipedInputStream} represented by the {@code key}. Subclasses are expected
     * to encapsulate this behavior and provide a function making more sense to its context.
     *
     * @param key the filterId key to the corresponding {@link PipedInputStream} in the input registry.
     * @return a byte of data
     * @throws EndOfStreamException when the stream is closed or no longer alive
     */
    byte readFromInput(String key) throws EndOfStreamException {
        PipedInputStream InputReadPort = inputForKey(key);
        byte datum = 0;

        try {
            while (InputReadPort.available() == 0) {
                if (endOfInputForKey(key)) {
                    throw new EndOfStreamException(key, "End of input stream reached");
                }
                sleep(250);
            }
        } catch (EndOfStreamException Error) {
            throw Error;
        } catch (Exception Error) {
            System.out.println( "\n" + this.getName() + " Error in read port wait loop::" + Error );
        }

        try {
            datum = (byte) InputReadPort.read();
            return datum;
        } catch (Exception Error) {
            System.out.println( "\n" + this.getName() + " Pipe read error::" + Error );
            return datum;
        }
    }

    /**
     * Write a byte to the {@link PipedOutputStream} represented by the {@code key}. Subclasses are expected
     * to encapsulate this behavior and provide a function making more sense to its context.
     *
     * @param datum the byte data to be written
     * @param key the filterId key to the corresponding {@link PipedOutputStream} in the output registry.
     */
    void writeToOutput(byte datum, String key) {
        PipedOutputStream OutputWritePort = outputForKey(key);
        try {
            OutputWritePort.write((int) datum);
            OutputWritePort.flush();
        } catch (Exception Error) {
            System.out.println("\n" + this.getName() + " Pipe write error::" + Error );
        }
    }

    /**
     * Whether the {@link PipedInputStream} represented by {@code key} has reached the end.
     *
     * @param key the filterId key to the corresponding {@link PipedInputStream} in the input registry.
     * @return whether the input stream has ended.
     */
    protected boolean endOfInputForKey(String key) {
        FilterFramework InputFilter = inputFilterForKey(key);
        return !InputFilter.isAlive();
    }

    /**
     * Close the {@link PipedInputStream} represented by {@code key}.
     *
     * @param key the filterId key to the corresponding {@link PipedInputStream} in the input registry.
     */
    protected void closeInputForKey(String key) {
        PipedInputStream InputReadPort = inputForKey(key);
        try {
            InputReadPort.close();
        } catch (Exception Error) {
            System.out.println( "\n" + this.getName() + " ClosePorts error::" + Error );
        }
    }

    /**
     * Close the {@link PipedOutputStream} represented by {@code key}.
     *
     * @param key the filterId key to the corresponding {@link PipedOutputStream} in the output registry.
     */
    void closeOutputForKey(String key) {
        PipedOutputStream OutputWritePort = outputForKey(key);
        try {
            OutputWritePort.close();
        } catch (Exception Error) {
            System.out.println( "\n" + this.getName() + " ClosePorts error::" + Error );
        }
    }

    /**
     * Close all registered {@link PipedInputStream} and {@link PipedOutputStream} connections registered in the
     * input registry and output registry.
     */
    void closeAllPorts() {
        for (String key : inputs.keySet())
            closeInputForKey(key);
        for (String key : outputs.keySet())
            closeOutputForKey(key);
    }

    /**
     * Utility method to retrieve the {@link PipedInputStream} from the input registry.
     *
     * @param key the filterId key to the corresponding {@link PipedInputStream} in the input registry.
     * @return the registered input stream
     */
    protected PipedInputStream inputForKey(String key) {
        return inputs.get(key);
    }

    /**
     * Utility method to retrieve the {@link PipedOutputStream} from the output registry.
     *
     * @param key the filterId key to the corresponding {@link PipedOutputStream} in the output registry.
     * @return the registered output stream
     */
    protected PipedOutputStream outputForKey(String key) {
        return outputs.get(key);
    }

    /**
     * Utility method to retrieve the {@link FilterFramework} from the input filter registry.
     *
     * @param key the filterId key to the corresponding {@link FilterFramework} in the input filter registry.
     * @return the registered input filter
     */
    protected FilterFramework inputFilterForKey(String key) {
        return inputFilters.get(key);
    }

    /**
     * The exception thrown when the input stream has reached to an end.
     */
    public class EndOfStreamException extends Exception {

        static final long serialVersionUID = 0; // the version for streaming

        private String key;

        EndOfStreamException () { super(); }

        EndOfStreamException(String s) { super(s); }

        EndOfStreamException(String key, String s) {
            super(s);
            this.key = key;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }
    }
}
