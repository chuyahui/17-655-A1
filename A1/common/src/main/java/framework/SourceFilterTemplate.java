package framework;

import java.io.EOFException;

/******************************************************************************************************************
* File:SourceFilterTemplate.java
* Course: 17655
* Project: Assignment 1
* Copyright: Copyright (c) 2003 Carnegie Mellon University
* Versions:
*	1.0 November 2008 - Initial rewrite of original assignment 1 (ajl).
*
* Description:
*
* This class serves as a template for creating source filters. The details of threading, connections writing output
* are contained in the FilterFramework super class. In order to use this template the program should rename the class.
* The template includes the run() method which is executed when the filter is started. The run() method is the guts
* of the filter and is where the programmer should put their filter specific code.The run() method is the main
* read-write loop for reading data from some source and writing to the output port of the filter. This template
* assumes that the filter is a source filter that reads data from a file, device (sensor),or generates the data
* interally, and then writes data to its output port. In this case, only the output port is used. In cases where the
* filter is a standard filter or a sink filter, you should use the SimpleFilter.java or SinkFilterTemplate.java as
* a starting point for creating standard or sink filters.
*
* Parameters: 		None
*
* Internal Methods:
*
*	public void run() - this method must be overridden by this class.
*
******************************************************************************************************************/

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
	 * Template method to read one byte of data from the input stream
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

	public void run()
    {

		byte databyte = 0;

/*************************************************************
*	This is the main processing loop for the filter. Since this
*   is a source filter, the programer will have to determine
* 	when the loop ends.
**************************************************************/

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
   } // run

} // SourceFilterTemplate