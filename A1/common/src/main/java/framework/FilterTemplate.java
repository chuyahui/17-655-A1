package framework;

import util.ConversionUtil;

import java.io.ByteArrayOutputStream;

/******************************************************************************************************************
* File:FilterTemplate.java
* Course: 17655
* Project: Assignment 1
* Copyright: Copyright (c) 2003 Carnegie Mellon University
* Versions:
*	1.0 November 2008 - Initial rewrite of original assignment 1 (ajl).
*
* Description:
*
* This class serves as a template for creating filters. The details of threading, filter connections, input, and output
* are contained in the FilterFramework super class. In order to use this template the program should rename the class.
* The template includes the run() method which is executed when the filter is started.
* The run() method is the guts of the filter and is where the programmer should put their filter specific code.
* In the template there is a main read-write loop for reading from the input port of the filter and writing to the
* output port of the filter. This template assumes that the filter is a "normal" that it both reads and writes data.
* That is both the input and output ports are used - its input port is connected to a pipe from an up-stream filter and
* its output port is connected to a pipe to a down-stream filter. In cases where the filter is a source or sink, you
* should use the SourceFilterTemplate.java or SinkFilterTemplate.java as a starting point for creating source or sink
* filters.
*
* Parameters: 		None
*
* Internal Methods:
*
*	public void run() - this method must be overridden by this class.
*
******************************************************************************************************************/

/**
 * A basic filter template can only accept 1 connection and be connected to 1 filter
 */
public abstract class FilterTemplate extends FilterFramework
{

	protected final MeasurementConfig context;
	protected String inputFilterId = null;
	protected String outputFilterId = null;

	protected FilterTemplate(final MeasurementConfig context, String filterId) {
		super(filterId);
		this.context = context;
	}

	@Override
	public void connect(FilterFramework Filter) {
		if (inputs.size() >= 1)
			throw new RuntimeException("A basic filter can only accept 1 connection");
		super.connect(Filter);
	}

	@Override
	protected void inputConnected(FilterFramework inputFilter) {
		super.inputConnected(inputFilter);
		if (this.inputFilters.size() >= 1 && !this.inputFilters.keySet().contains(inputFilter.filterId))
			throw new RuntimeException("A basic filter can only accept 1 connection");
		this.inputFilterId = inputFilter.filterId;
	}

	@Override
	protected void outputConnected(FilterFramework outputFilter) {
		if (this.outputFilterId != null)
			throw new RuntimeException("A basic filter can only be connected to 1 filter.");
		this.outputFilterId = outputFilter.filterId;
	}

	protected abstract byte[] doTransform(int id, byte[] measurement);

	protected byte ReadFilterInputPort() throws EndOfStreamException {
		return readFromInput(inputFilterId);
	}

	protected void WriteFilterOutputPort(byte data) {
		writeToOutput(data, outputFilterId);
	}

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

	public void run()
    {
		while (true)
		{

/***************************************************************
*	The program can insert code for the filter operations
* 	here. Note that data must be received and sent one
* 	byte at a time. This has been done to adhere to the
* 	pipe and filter paradigm and provide a high degree of
* 	portabilty between filters. However, you must reconstruct
* 	data on your own. First we read a byte from the input
* 	stream...
***************************************************************/

			try
			{
				byte[] idBytes = readId();
				int id = ConversionUtil.convertToInt(idBytes);
				//System.out.println("[" + this.getName() + "] id is: " + id);
				byte[] measurement = readMeasurement(context.idForMeasurementLength(id));
				byte[] transformedMeasurement = doTransform(id, measurement);
				if (transformedMeasurement != null && transformedMeasurement.length > 0) {
					for (int i = 0; i < idBytes.length; i++)
						WriteFilterOutputPort(idBytes[i]);
					for (int i = 0; i < transformedMeasurement.length; i++) {
						//System.out.println("[" + this.getName() + "] transformed measurement: " + transformedMeasurement[i]);
						WriteFilterOutputPort(transformedMeasurement[i]);
					}

				}
			} // try

/***************************************************************
*	When we reach the end of the input stream, an exception is
* 	thrown which is shown below. At this point, you should
* 	finish up any processing, close your ports and exit.
***************************************************************/

			catch (EndOfStreamException e)
			{
				closeAllPorts();
				break;

			} // catch

		} // while

   } // run

} // FilterTemplate