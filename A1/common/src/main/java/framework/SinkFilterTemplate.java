package framework;

/**
 * This class represents the sink, a special kind of filter which writes to an external resource. It provides basic
 * functionality to read from the input pipe and leave the write function to its implementations.
 *
 * @since 1.0.0
 */
public abstract class SinkFilterTemplate extends FilterFramework {

	/**
	 * The filter id for the input pipe, used to lookup the {@link java.io.PipedInputStream} from the input registry.
	 */
	private String inputKey;

	/**
	 * Template method for the subclasses to implement. Subclasses should decide how to write the data to the external
	 * resource.
	 *
	 * @param dataByte the data to be written to external resource.
	 */
	protected abstract void writeByteToSink(byte dataByte);

	/**
	 * Default constructor
	 *
	 * @param filterId
	 */
	public SinkFilterTemplate(String filterId) {
		super(filterId);
	}

	/**
	 * Register the input filter id.
	 *
	 * @param inputFilter the input filter connected
	 */
	@Override
	protected void inputConnected(FilterFramework inputFilter) {
		super.inputConnected(inputFilter);
		this.inputKey = inputFilter.filterId;
	}

	/**
	 * Main execution method for the sink. It reads a byte of data from the input and let the subclasses decide
	 * how to write it to the external resource.
	 */
	public void run() {
		byte databyte = 0;

		while (true) {
			try {
				// read data
				databyte = readFromInput(inputKey);

				// write data
				writeByteToSink(databyte);
			} catch (EndOfStreamException e) {
				// close ports if there's no more input data
				closeAllPorts();
				break;
			}
		}
   	}
}