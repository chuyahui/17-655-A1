package framework;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Weinan Qiu
 * @since 1.0.0
 */
public abstract class FilterFramework extends Thread {

    protected final String filterId;

    protected Map<String, PipedInputStream> inputs = new ConcurrentHashMap<String, PipedInputStream>();
    protected Map<String, PipedOutputStream> outputs = new ConcurrentHashMap<String, PipedOutputStream>();
    protected Map<String, FilterFramework> inputFilters = new HashMap<String, FilterFramework>();

    public FilterFramework(String filterId) {
        this.filterId = filterId;
    }

    protected void inputConnected(FilterFramework inputFilter) {
        inputFilters.put(inputFilter.filterId, inputFilter);
    }

    protected void outputConnected(FilterFramework outputFilter) {
        // do nothing
    }

    void connect(FilterFramework Filter) {
        if (!this.inputs.containsKey(Filter.filterId))
            this.inputs.put(Filter.filterId, new PipedInputStream());

        if (!Filter.outputs.containsKey(this.filterId))
            Filter.outputs.put(this.filterId, new PipedOutputStream());

        try {
            PipedInputStream InputReadPort = inputForKey(Filter.filterId);
            PipedOutputStream OutputWritePort = Filter.outputForKey(this.filterId);
            InputReadPort.connect(OutputWritePort);

            this.inputConnected(Filter);
            Filter.outputConnected(this);
        } catch(Exception Error) {
            System.out.println( "\n" + this.getName() + " FilterFramework error connecting::"+ Error );
        }
    }

    byte readFromInput(String key) throws EndOfStreamException {
        PipedInputStream InputReadPort = inputForKey(key);
        byte datum = 0;

        try {
            while (InputReadPort.available() == 0) {
                if (endOfInputForKey(key)) {
                    throw new EndOfStreamException("End of input stream reached");
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

    void writeToOutput(byte datum, String key) {
        PipedOutputStream OutputWritePort = outputForKey(key);
        try {
            OutputWritePort.write((int) datum);
            OutputWritePort.flush();
        } catch (Exception Error) {
            System.out.println("\n" + this.getName() + " Pipe write error::" + Error );
        }
    }

    private boolean endOfInputForKey(String key) {
        FilterFramework InputFilter = inputFilterForKey(key);
        return !InputFilter.isAlive();
    }

    void closeInputForKey(String key) {
        PipedInputStream InputReadPort = inputForKey(key);
        try {
            InputReadPort.close();
        } catch (Exception Error) {
            System.out.println( "\n" + this.getName() + " ClosePorts error::" + Error );
        }
    }

    void closeOutputForKey(String key) {
        PipedOutputStream OutputWritePort = outputForKey(key);
        try {
            OutputWritePort.close();
        } catch (Exception Error) {
            System.out.println( "\n" + this.getName() + " ClosePorts error::" + Error );
        }
    }

    void closeAllPorts() {
        for (String key : inputs.keySet())
            closeInputForKey(key);
        for (String key : outputs.keySet())
            closeOutputForKey(key);
    }

    protected PipedInputStream inputForKey(String key) {
        return inputs.get(key);
    }

    protected PipedOutputStream outputForKey(String key) {
        return outputs.get(key);
    }

    protected FilterFramework inputFilterForKey(String key) {
        return inputFilters.get(key);
    }

    class EndOfStreamException extends Exception {

        static final long serialVersionUID = 0; // the version for streaming

        EndOfStreamException () { super(); }

        EndOfStreamException(String s) { super(s); }

    }
}
