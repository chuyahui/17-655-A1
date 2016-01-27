package framework;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Weinan Qiu
 * @since 1.0.0
 */
public abstract class AggregatingFilterTemplate extends FilterFramework {

    protected final MeasurementContext context;
    protected List<String> inputFilterIds = new ArrayList<String>();
    protected String outputFilterId = null;

    public AggregatingFilterTemplate(String filterId, MeasurementContext context) {
        super(filterId);
        this.context = context;
    }

    @Override
    public void connect(FilterFramework Filter) {
        if (inputs.size() >= 2)
            throw new RuntimeException("An aggregating filter can only accept 2 connections");
        super.connect(Filter);
    }

    @Override
    protected void inputConnected(FilterFramework inputFilter) {
        super.inputConnected(inputFilter);
        if (this.inputFilters.size() >= 2)
            throw new RuntimeException("An aggregating filter can only accept 2 connections");
        this.inputFilterIds.add(inputFilter.filterId);
    }

    @Override
    protected void outputConnected(FilterFramework outputFilter) {
        if (this.outputFilterId != null)
            throw new RuntimeException("An aggregating filter can only connect to 1 filter");
        this.outputFilterId = outputFilter.filterId;
    }

    protected byte ReadFilterInputPortOne() throws EndOfStreamException {
        return readFromInput(inputFilterIds.get(0));
    }

    protected byte ReadFilterInputPortTwo() throws EndOfStreamException {
        return readFromInput(inputFilterIds.get(1));
    }

    protected void WriteFilterOutputPort(byte data) {
        writeToOutput(data, outputFilterId);
    }

    protected byte[] readIdFromInputPortOne() throws EndOfStreamException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (int i = 0; i < context.getIdLength(); i++) {
            byte databyte = ReadFilterInputPortOne();
            out.write(databyte);
        }
        return out.toByteArray();
    }

    protected byte[] readMeasurementFromInputPortOne(int length) throws EndOfStreamException {
        byte[] measurement = new byte[length];
        for (int i = 0; i < length; i++) {
            measurement[i] = ReadFilterInputPortOne();
        }
        return measurement;
    }

    protected byte[] readIdFromInputPortTwo() throws EndOfStreamException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (int i = 0; i < context.getIdLength(); i++) {
            byte databyte = ReadFilterInputPortTwo();
            out.write(databyte);
        }
        return out.toByteArray();
    }

    protected byte[] readMeasurementFromInputPortTwo(int length) throws EndOfStreamException {
        byte[] measurement = new byte[length];
        for (int i = 0; i < length; i++) {
            measurement[i] = ReadFilterInputPortTwo();
        }
        return measurement;
    }

    protected abstract void attemptReadFromPorts() throws EndOfStreamException;

    protected abstract boolean hasCompletedAggregation();

    protected abstract byte[] aggregatedBytes();

    @Override
    public void run() {
        while (true) {
            try {
                attemptReadFromPorts();
                if (!hasCompletedAggregation())
                    sleep(250l);
                else {
                    byte[] bytesToFlush = aggregatedBytes();
                    for (byte eachByte : bytesToFlush)
                        WriteFilterOutputPort(eachByte);
                }
            } catch (Exception ex) {
                closeAllPorts();
                break;
            }
        }
    }
}
