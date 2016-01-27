package framework;

import java.util.ArrayList;
import java.util.List;

/**
 * A splitting filter can only accept 1 input connection, but it post output to 2 filters.
 *
 * @author Weinan Qiu
 * @since 1.0.0
 */
public abstract class SplittingFilterTemplate extends FilterFramework {

    protected final MeasurementContext context;
    protected String inputFilterId = null;
    protected List<String> outputFilterIds = new ArrayList<String>();

    public SplittingFilterTemplate(String filterId, MeasurementContext context) {
        super(filterId);
        this.context = context;
    }

    @Override
    public void connect(FilterFramework Filter) {
        if (inputs.size() >= 1)
            throw new RuntimeException("A splitting filter can only accept 1 connection");
        super.connect(Filter);
    }

    @Override
    protected void inputConnected(FilterFramework inputFilter) {
        super.inputConnected(inputFilter);
        if (this.inputFilters.size() >= 1)
            throw new RuntimeException("A splitting filter can only accept 1 connection");
        this.inputFilterId = inputFilter.filterId;
    }

    @Override
    protected void outputConnected(FilterFramework outputFilter) {
        if (this.outputFilterIds.size() >= 2)
            throw new RuntimeException("A splitting filter can only accept 2 connection");
        this.outputFilterIds.add(outputFilter.filterId);
    }

    byte ReadFilterInputPort() throws EndOfStreamException {
        return readFromInput(inputFilterId);
    }

    void WriteFilterOutputPortOne(byte data) {
        writeToOutput(data, outputFilterIds.get(0));
    }

    void WriteFilterOutputPortTwo(byte data) {
        writeToOutput(data, outputFilterIds.get(1));
    }

    private int readId() throws EndOfStreamException {
        int id = 0;
        for (int i = 0; i < context.getIdLength(); i++) {
            id = id | (ReadFilterInputPort() & 0xFF);
            if (i != context.getIdLength() - 1)
                id = id << 8;
        }
        return id;
    }

    private byte[] readMeasurement(int length) throws EndOfStreamException {
        byte[] measurement = new byte[length];
        for (int i = 0; i < length; i++) {
            measurement[i] = ReadFilterInputPort();
        }
        return measurement;
    }

    protected abstract void routeMeasurement(int id, byte[] measurement);

    @Override
    public void run() {
        while (true) {
            try {
                int id = readId();
                byte[] measurement = readMeasurement(context.idForMeasurementLength(id));
                routeMeasurement(id, measurement);
            } catch (EndOfStreamException ex) {
                closeAllPorts();
                break;
            }
        }
    }
}
