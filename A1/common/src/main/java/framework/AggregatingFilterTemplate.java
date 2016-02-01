package framework;

import util.ConversionUtil;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Weinan Qiu
 * @since 1.0.0
 */
public abstract class AggregatingFilterTemplate extends FilterFramework {

    protected final MeasurementConfig context;
    protected List<String> inputFilterIds = new ArrayList<String>();
    protected String outputFilterId = null;

    public AggregatingFilterTemplate(String filterId, MeasurementConfig context) {
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
        if (this.inputFilters.size() > 2)
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

    protected abstract boolean shouldReadFromPortOne();

    protected abstract boolean shouldReadFromPortTwo();

    protected abstract void dataReadForPortOne(int id, byte[] measurement);

    protected abstract void dataReadForPortTwo(int id, byte[] measurement);

    protected abstract boolean hasCompletedAggregation();

    protected abstract byte[] aggregatedBytes() throws Exception;

    protected void portOneStreamHasEnded() throws EndOfStreamException {
        while (true) {
            WriteFilterOutputPort(ReadFilterInputPortTwo());
        }
    }

    protected void portTwoStreamHasEnded() throws EndOfStreamException {
        while (true) {
            WriteFilterOutputPort(ReadFilterInputPortOne());
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                if (shouldReadFromPortOne()) {
                    byte[] idBytes = readIdFromInputPortOne();
                    int id = ConversionUtil.convertToInt(idBytes);
                    byte[] measurement = readMeasurementFromInputPortOne(context.idForMeasurementLength(id));
                    dataReadForPortOne(id, measurement);
                }

                if (shouldReadFromPortTwo()) {
                    byte[] idBytes = readIdFromInputPortTwo();
                    int id = ConversionUtil.convertToInt(idBytes);
                    byte[] measurement = readMeasurementFromInputPortTwo(context.idForMeasurementLength(id));
                    dataReadForPortTwo(id, measurement);
                }

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
                        if (endOfInputForKey(inputOneKey) && !endOfInputForKey(inputTwoKey)) {
                            portOneStreamHasEnded();
                            closeInputForKey(inputOneKey);
                        } else if (endOfInputForKey(inputTwoKey) && !endOfInputForKey(inputOneKey)) {
                            portTwoStreamHasEnded();
                            closeInputForKey(inputTwoKey);
                        } else {
                            closeAllPorts();
                            break;
                        }
                    } catch (EndOfStreamException eos) {
                        closeAllPorts();
                        break;
                    }
                }
            }
        }
    }
}
