package framework;

import util.ConversionUtil;

import java.io.ByteArrayOutputStream;

/**
 * Generic case of a filter where it reads from only one input port and writes to only one output port.
 */
public abstract class SimpleFilter extends FilterFramework {

	/**
	 * The configuration object containing the length information of byte data.
	 */
	protected final MeasurementConfig context;

	/**
	 * The id of the input filter. It is used to lookup {@link java.io.PipedInputStream} by calling {@link #inputForKey(String)}.
	 */
	protected String inputFilterId = null;

	/**
	 * The id of the output filter. It is used to lookup {@link java.io.PipedOutputStream} by calling {@link #outputForKey(String)}.
	 */
	protected String outputFilterId = null;

	/**
	 * Default constructor.
	 *
	 * @param context the configuration object containing the length information for byte data.
	 * @param filterId the filter id of this filter.
	 */
	protected SimpleFilter(final MeasurementConfig context, String filterId) {
		super(filterId);
		this.context = context;
	}

	/**
	 * Perform connection and do a integrity check to ensure it's only connected once.
	 *
	 * @param Filter the filter requesting connection to this filter as input
	 */
	@Override
	public void connect(FilterFramework Filter) {
		if (inputs.size() >= 1)
			throw new RuntimeException("A basic filter can only accept 1 connection");
		super.connect(Filter);
	}

	/**
	 * Register the {@link #inputFilterId} and do an integrity check.
	 *
	 * @param inputFilter the input filter connected
	 */
	@Override
	protected void inputConnected(FilterFramework inputFilter) {
		super.inputConnected(inputFilter);
		if (this.inputFilters.size() >= 1 && !this.inputFilters.keySet().contains(inputFilter.filterId))
			throw new RuntimeException("A basic filter can only accept 1 connection");
		this.inputFilterId = inputFilter.filterId;
	}

	/**
	 * Register the {@link #outputFilterId} and do an integrity check.
	 * @param outputFilter the output filter connected
	 */
	@Override
	protected void outputConnected(FilterFramework outputFilter) {
		if (this.outputFilterId != null)
			throw new RuntimeException("A basic filter can only be connected to 1 filter.");
		this.outputFilterId = outputFilter.filterId;
	}

	/**
	 * Template method left for subclasses to implement. Here, subclasses should transform the measurement
	 * data and return the transformed bytes. If the subclass is not responsible for transforming a specific
	 * measurement, it should return the measurement as is. If the subclass wishes to discard the measurement,
	 * it should return a 0-length byte array.
	 *
	 * @param id the id of the measurement data
	 * @param measurement data
	 * @return the transformed data
	 */
	protected abstract byte[] doTransform(int id, byte[] measurement);

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
	 * Utility method write to the {@link java.io.PipedOutputStream} represented by {@link #outputFilterId}.
	 *
	 * @param data a byte of data to be written to the output port.
	 */
	protected void WriteFilterOutputPort(byte data) {
		writeToOutput(data, outputFilterId);
	}

	/**
	 * Utility method to read id.
	 *
	 * @return byte array of id bytes.
	 * @throws EndOfStreamException
	 */
	private byte[] readId() throws EndOfStreamException {
		//System.out.println("[" + this.getName() + "]length to read: " + context.getIdLength());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		for (int i = 0; i < context.getIdLength(); i++) {
			byte databyte = ReadFilterInputPort();
			//System.out.println("[" + this.getName() + "]byte read: " + databyte);
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
		//System.out.println("[" + this.getName() + "]length to read: " + length);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		for (int i = 0; i < length; i++) {
			byte databyte = ReadFilterInputPort();
			//System.out.println("[" + this.getName() + "]byte read: " + databyte);
			out.write(databyte);
		}
		return out.toByteArray();
	}

	/**
	 * Main execution method for this filter. It tries to read id and measurement from the input port and pass it
	 * to {@link #doTransform(int, byte[])} for any transformation the subclasses provides. If the transformed bytes
	 * has a length greater than 0, the id bytes and measurement bytes will be passed onto the output port. Otherwise,
	 * the id and measurement is simply discarded.
	 *
	 * When input port goes down, it will attempt to close all ports.
	 */
	public void run() {
		while (true) {
			try {
				// read id
				byte[] idBytes = readId();
				int id = ConversionUtil.convertToInt(idBytes);

				// read measurement
				byte[] measurement = readMeasurement(context.idForMeasurementLength(id));

				// do transformation
				byte[] transformedMeasurement = doTransform(id, measurement);

				// pass data onto output port if length is > 0
				if (transformedMeasurement != null && transformedMeasurement.length > 0) {
					for (int i = 0; i < idBytes.length; i++)
						WriteFilterOutputPort(idBytes[i]);
					for (int i = 0; i < transformedMeasurement.length; i++) {
						WriteFilterOutputPort(transformedMeasurement[i]);
					}
				}
			} catch (EndOfStreamException e) {
				closeAllPorts();
				break;
			}
		}
	}
}