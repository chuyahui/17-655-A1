package framework;

import java.io.EOFException;

/**
 * The class represents the source, a special kind of filter that reads data from an external resource and pass it on.
 * It provides basic functionality of pass the data to the output port
 */
public abstract class SourceFilterTemplate extends FilterFramework
{

	private String outputKey;

	public SourceFilterTemplate(String filterId) {
		super(filterId);
	}

	@Override
	protected void outputConnected(FilterFramework outputFilter) {
		outputKey = outputFilter.filterId;
	}

	/**
	 * Template method to read one byte of data from the external resource
	 *
	 * @return the byte read from the input stream
	 */
	protected abstract byte readOneByte();

	/**
	 * Indicates the stream of data has reached to an end.
	 *
	 * @return
	 */
	protected abstract boolean hasReachedEndOfStream();

	/**
	 * Main execution method for the source filter. It reads a byte of data and pass it onto output port.
	 * If the external resource has ended, it throws {@link EOFException}
	 */
	public void run() {

		byte databyte = 0;

		try {
			while (true) {
				if (hasReachedEndOfStream())
					throw new EOFException("Has reached end of data stream.");

				databyte = readOneByte();
				writeToOutput(databyte, outputKey);
			}
		} catch (EOFException eof) {
			System.out.println("Reached end of data stream.");
			closeAllPorts();
		} catch (Exception ex) {
			System.out.println("Encountered exception: " + ex.getMessage());
		}
   	}
}