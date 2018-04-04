import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This class represents the object that the firmware sends samples and commands
 * to (and also to send commands to the firmware). Two separate threads are 
 * run simultaneously, to handle the two way communication. Connection is
 * done using TCP.
 * @author Lisa Liu-Thorrold
 *
 */
public class DigiscopeServer {

	private final DigiscopeModel model;
	private Socket socket;
	private DataInputStream input;
	private OutputStream deviceOut;
	private InetAddress ipAddress;
	int portNumber;
	private Thread inboundThread;
	private Thread outboundThread;
	private LinkedBlockingQueue<byte []> outboundMessageQueue;
	final short padding = 0;

	DataOutputStream dos;

	// command constants
	final short TIME_PER_DIVISION_COMMAND = 0x3131;
	final short VOLTAGE_PER_DIVISION_COMMAND = 0x3232;
	final short TRIGGER_THRESHOLD_COMMAND = 0x3333;
	final short TRIGGER_MODE_COMMAND = 0x3434;
	final short TRIGGER_TYPE_COMMAND = 0x3535;
	final short SAMPLING_MODE_COMMAND = 0x3636;
	final short NUM_SAMPLES_COMMAND = 0x3737;
	final short CHANNEL_COUPLING_COMMAND = 0x3838;
	final short SEND_SAMPLE_START = 0x3939;
	final short SEND_SAMPLE_END = 0x4040;
	final short FORCE_TRIGGER_COMMAND = 0x4141;
	final short REARM_TRIGGER_COMMAND = 0x4242;
	final short FUNC_GEN_OUTPUT_COMMAND = 0x4343;
	final short FUNC_GEN_WAVE_TYPE_COMMAND = 0x4444;
	final short FUNC_GEN_P2P_VOLTAGE_COMMAND = 0x4545;
	final short FUNC_GEN_OFFSET_COMMAND = 0x4646;
	final short FUNC_GEN_FREQUENCY_COMMAND = 0x4747;
	final short SAMPLING_RATE_COMMAND = 0x4848;
	final short CHANNEL_TO_TRIGGER_COMMAND = 0x4949;
	final short CHANNEL_OFFSETS_COMMAND = 0x5050;
	final short DEVICE_STATUS = 0x5151;
	final short BANDPASS_SAMPLING = 0x5252;

	public DigiscopeServer(DigiscopeModel model) {
		this.model = model;
	}

	/**
	 * This method attempts to establish a connection to the device
	 * @param ipAddress - The ip address of the device
	 * @param portNumber - The port number of the device
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public void connect(String ipAddress, int portNumber)
			throws UnknownHostException, IOException {
		this.ipAddress = InetAddress.getByName(ipAddress);
		this.portNumber = portNumber;

		socket = new Socket(ipAddress, portNumber);
		input = new DataInputStream(socket.getInputStream());

		//flush at the end of the line.
		deviceOut = socket.getOutputStream();
		dos = new DataOutputStream(deviceOut);

		outboundMessageQueue = new LinkedBlockingQueue<>();

		inboundThread = new Thread(new DigiscopeServerIn());
		inboundThread.start();

		outboundThread = new Thread(new DigiscopeServerOut(outboundMessageQueue));
		outboundThread.start();
	}

	/**
	 * This method adds a messages to send to the firmware to a queue that is
	 * monitored by the thread that is responsible for sending these messages
	 * out through the socket
	 * @param message
	 */
	public void sendToDevice(byte[] message) {

		outboundMessageQueue.add(message);
	}

	/**
	 * Disconnection logic from the device.
	 */
	public void disconnect() {
		try {
			inboundThread.interrupt();
			outboundThread.interrupt();
			deviceOut.close();
			socket.close();
			input.close();
		} catch (IOException e) {
			System.out.println("Error disconnecting");
			e.printStackTrace();
		}
	}

	/**
	 * This class represents the thread that is responsible for sending messages
	 * to the firmware. This is done by constantly taking messages that are put
	 * into the queue and sending them out over the socket.
	 * @author Lisa
	 */
	private class DigiscopeServerOut implements Runnable {
		// private final LinkedBlockingQueue<String> outboundMessageQueue;
		private final LinkedBlockingQueue<byte []> outboundMessageQueue;

		DigiscopeServerOut(LinkedBlockingQueue<byte[]> outboundMessageQueue) {
			this.outboundMessageQueue = outboundMessageQueue;
		}

		public void run() {

			try {
				while (!Thread.currentThread().isInterrupted()) {

					byte[] message = outboundMessageQueue.take();
					dos.write(message);
				}
			} catch (Exception e) {
				System.out.println("Exiting thread1");
				e.printStackTrace();
			}
		}
	}

	/**
	 * This class represents the thread that is responsible for receiving
	 * messages from the firmware, and handling them appropriately.
	 * @author Lisa
	 */
	private class DigiscopeServerIn implements Runnable {

		double[] channelASamples;
		double[] channelBSamples;

		public void run() {
			try {
				while (!Thread.currentThread().isInterrupted()) {

					// use boolean indicate to break out of loop
					boolean eof = false;
					byte[] inputData = new byte[4];

					int numSamples = 0;

					while (!eof) {
						try {
							// read and use data - read in chunks of 4 bytes
							input.readFully(inputData);

							// flip them because tiva is little endian and java
							// is big endian
							byte[] command = {inputData[1], inputData[0]};
							byte[] value = {inputData[3], inputData[2]};

							processMessage(ByteBuffer.wrap(command).getShort(),
									ByteBuffer.wrap(value).getShort());
						} catch (EOFException e) {
							eof = true;
						}
					}

				}

			} catch (Exception e) {
				System.out.println("Exiting thread2");
				e.printStackTrace();
			}
		}

		/**
		 * This method process messages in chunks of 4 bytes.
		 * @param command - The first 2 bytes represents the command
         * @param value - Represents the value associated with a command. If
		 *              there is none, then this byte is padded with 0.
         */
		private void processMessage(short command, short value) {

			switch(command) {
			case CHANNEL_COUPLING_COMMAND:
				changeChannelCoupling(value);
				break;
			case VOLTAGE_PER_DIVISION_COMMAND:
				changeVoltagePerDiv(value);
				break;
			case TIME_PER_DIVISION_COMMAND:
				changeTimePerDiv(value);
				break;
			case TRIGGER_MODE_COMMAND:
				changeTriggerMode(value);
				break;
			case TRIGGER_THRESHOLD_COMMAND:
				changeTriggerThreshold(value);
				break;
			case TRIGGER_TYPE_COMMAND:
				changeTriggerType(value);
				break;
			case FUNC_GEN_OUTPUT_COMMAND:
				changeFuncGenOutput(value);
				break;
			case FUNC_GEN_WAVE_TYPE_COMMAND:
				changeFuncGenWaveType(value);
				break;
			case FUNC_GEN_P2P_VOLTAGE_COMMAND:
				changeFuncGenP2PVoltage(value);
				break;
			case FUNC_GEN_OFFSET_COMMAND:
				changeFuncGenOffset(value);
				break;
			case FUNC_GEN_FREQUENCY_COMMAND:
				changeFuncGenFrequency(value);
				break;
			case SEND_SAMPLE_START:
				readSamples(value);
				break;
			case SAMPLING_RATE_COMMAND:
				changeSamplingRate(value);
				break;
			case DEVICE_STATUS:
				changeDeviceStatus(value);
				break;

			}
		}

		/**
		 * Read the samples, and trigger the event if done successfully
		 * @param triggerIndex
         */
		private void readSamples(short triggerIndex) {
			try {
				processSamples(model.getNumSamplesToAcquire(),triggerIndex);
			} catch (Exception e) {
				e.printStackTrace();
			}

			model.emit("setScalingComboBoxes");
			System.out.println("Processing samples ok");
		}

		/*
		 * Convert raw adc reading to meaningful one.
		 */
		private double processSample(short sample) {
			return (3.3 * sample / 4095);
		}

		/**
		 * This method performs bandpass sampling on the values received.
		 * 1. Generate a 1Mhz sine wave 20 times bigger than the number of samples
		 *    that we are expecting to receive (linear interpolation by a factor
		 *    of 20
		 * 2. Interpolate 20 samples in between each point - equally
		 * 3. Multiply them together
		 * 4. Put the samples through a filter
		 */
		private void processBandpassChannelA(double[] chanASamples) {
			// 1khz sine wave
			int frequency = 1000000;
			int numSamplesToAcquire = model.getNumSamplesToAcquire();
			// effective sampling rate
			int samplingRate = 20000000;

			//upsample by factor of 20
			double[] chanASamplesBandpassed = new double[numSamplesToAcquire * 20];

			// Coefficients from the filter was received from:
			// http://arc.id.au/FilterDesign.html
			double[] filter = new double[]{ -0.000230, -0.000347, -0.000411,
					-0.000369, -0.000179, 0.000165, 0.000624, 0.001106, 0.001482,
					0.001609, 0.001368, 0.000708, -0.000327, -0.001579, -0.002796,
					-0.003672, -0.003913, -0.003317, -0.001845, 0.000345,
					0.002888, 0.005273, 0.006934, 0.007378, 0.006311, 0.003737,
					0.000000, -0.004249, -0.008160, -0.010853, -0.011611,
					-0.010059, -0.006283, -0.000857, 0.005240, 0.010795,
					0.014616, 0.015782, 0.013868, 0.009064, 0.002178, -0.005504,
					-0.012466, -0.017270, -0.018863, -0.016814, -0.011422,
					-0.003679, 0.004923, 0.012690, 0.018076, 0.020000, 0.018076,
					0.012690, 0.004923, -0.003679, -0.011422, -0.016814,
					-0.018863, -0.017270, -0.012466, -0.005504, 0.002178,
					0.009064, 0.013868, 0.015782, 0.014616, 0.010795, 0.005240,
					-0.000857, -0.006283, -0.010059, -0.011611, -0.010853,
					-0.008160, -0.004249, 0.000000, 0.003737, 0.006311,
					0.007378, 0.006934, 0.005273, 0.002888, 0.000345, -0.001845,
					-0.003317, -0.003913, -0.003672, -0.002796, -0.001579,
					-0.000327, 0.000708, 0.001368, 0.001609, 0.001482, 0.001106,
					0.000624, 0.000165, -0.000179, -0.000369, -0.000411,
					-0.000347, -0.000230 };

			ArrayList<Double> tempBandpassValues = new ArrayList<Double>();

			for (int i=0; i<numSamplesToAcquire; i++) {
				// Multiply the sample by the 1Mhz frequency to cancel out the
				// lower frequency
				tempBandpassValues.add(chanASamples[i] *
						(Math.sin(2* Math.PI * 1000000 * i / 20000000)));

				// Do the linear interpolation for twenty samples in between
				// each one. Done by creating 20 evenly stepped points between
				// the two values. Then multiply these by the 1Mhz sine wave
				if (i != (chanASamples.length - 1)) {
					double start = chanASamples[i];
					double end = chanASamples[i + 1];
					double interval = (end - start) / 20.0;
					for (int j = 1; j < 20; j++) {
						double newVal = start + (interval * j);
						tempBandpassValues.add(newVal *
								(Math.sin(2 * Math.PI * 1000000 * i / 20000000)));
					}
				}
			}

			double[] arr = tempBandpassValues.stream().mapToDouble(
					Double::doubleValue).toArray();

			// put the samples through the filter
			chanASamplesBandpassed = computeFirFilterChannel(arr, filter);

			// set the result
			model.getChannelA().setBandpassedSamples(chanASamplesBandpassed);
		}

		/**
		 * Puts the samples for the Fir filter and returns the result
		 * @param samples - The samples to filter
		 * @param filter - The filter to apply
         * @return - The filtered samples
         */
		private double[] computeFirFilterChannel(double[] samples, double[] filter) {
			int size = samples.length;
			double[] filterChannelSamples = new double[size];

			for (int i=0; i<samples.length; i++) {

				double sum = 0;

				for (int j=0; j<filter.length; j++) {
					if (i-j < 0) {
						sum += 0;
					} else {
						sum += filter[j] *  samples[i-j];
					}
				}

				filterChannelSamples[i] = sum;

			}

			return filterChannelSamples;
		}

		/**
		 * This method handles reading through the samples when the command is
		 * received by firmware, sets them and initiates plotting
		 * @param numSamples - the number of samples to expect per channel
		 * @param triggerIndex - the index of the trigger point
         * @throws Exception
         */
		private void processSamples(int numSamples, short triggerIndex) throws Exception {

			channelASamples = new double[numSamples];
			channelBSamples = new double[numSamples];

			// To store the samples
			byte[] inputData = new byte[2];

			// To store the footer
			byte[] endData = new byte[4];

			try {
				// read channel a samples first
				for (int i = 0; i < numSamples; i++) {
					input.readFully(inputData);
					byte[] chanASample = {inputData[1], inputData[0]};
					channelASamples[i] = processSample(
							ByteBuffer.wrap(chanASample).getShort());
				}

				// read channel b samples second
				for (int i = 0; i < numSamples; i++) {
					input.readFully(inputData);
					byte[] chanBSample = {inputData[1], inputData[0]};
					channelBSamples[i] = processSample(
							ByteBuffer.wrap(chanBSample).getShort());
				}

				// read and use data
				input.readFully(endData);
				byte[] lastData = {endData[1], endData[0]};

				if (ByteBuffer.wrap(lastData).getShort() != SEND_SAMPLE_END) {
					model.emit("incorrectSamplesSent");
					// skip reading the samples/garbage after this.
					input.skip(input.available());
				}

				model.getOscilloscopeDisplay().clearGrid();
				model.getOscilloscopeDisplay().setTriggerIndex(triggerIndex);

				model.getChannelA().setChannelSamples(channelASamples, model.getSamplingRate());
				model.getChannelB().setChannelSamples(channelBSamples, model.getSamplingRate());

				if (model.getBandpassSampling().equals("On")) {
					processBandpassChannelA(channelASamples);
				}

				// Plot channel A if checkbox is checked by user
				if (model.getChannelAisPlotted()) {

					boolean bandpassOn =
							model.getBandpassSampling().equals("On") ? true : false;

					model.getOscilloscopeDisplay().initialPlotChannel(
							model.getVoltsPerDivisionInDouble(),
							model.getChannelA(), bandpassOn);
				}

				// Plot channel B if checkbox is checked by user
				if (model.getChannelBisPlotted()) {
					model.getOscilloscopeDisplay().initialPlotChannel(
							model.getVoltsPerDivisionInDouble(),
							model.getChannelB(), false);
				}

				// Compute filter channel if it has a valid file, and channel
				// input. If filter channel input is math, then computer
				// math channel first
				if (model.getFilterChannel().getAvailableForPlotting()) {
					// compute math channel first, if not already computed
					if (model.getFilterChannelInput().equals("Math") &&
							!model.getMathChannel().getEquation().contains("F")) {
						model.computeMathChannel();
					}

					model.computeFilterChannel();
				}

				// Compute math channel if it has a valid equation. If the math
				// equation contains F, the compute the filter channel first.
				if (model.getMathChannel().getAvailableForPlotting()) {
					// compute filter channel first, if not already computed
					if(model.getMathChannel().getEquation().contains("F")) {
						model.computeFilterChannel();
					}

					model.computeMathChannel();
				}

				// Plot filter if checkbox is checked by user
				if (model.getFilterChannelIsPlotted()) {
					model.getOscilloscopeDisplay().initialPlotChannel(
							model.getVoltsPerDivisionInDouble(),
							model.getFilterChannel(), false);

				}

				// Plot math channel if checkbox is checked by user
				if (model.getMathChannelIsPlotted()) {

					model.getOscilloscopeDisplay().initialPlotChannel(
							model.getVoltsPerDivisionInDouble(),
							model.getMathChannel(), false);
				}

				model.emit("updateMeasurementLabels");

			} catch (Exception e) {
				System.out.println("Error processing samples!!...");
				e.printStackTrace();
			}

		}

	}

	/*****************************************************
	 * Getter methods
	 *****************************************************/

	public int getPortNumber() {
		return portNumber;
	}

	public String getIpAddress() {
		return ipAddress.getHostAddress();
	}

	/*****************************************************
	 * Configuration Commands sent by Digiscope Controller
	 *****************************************************/

	public void sendFunctionGeneratorConfiguration(
			String output, String waveType, String p2pVoltage, String offset,
			String frequency) {

		// send function generator output
		short funcGenOutputValue = getFuncGenValue(output);
		sendBytesToDevice(FUNC_GEN_OUTPUT_COMMAND, funcGenOutputValue);

		// send function generator wave type
		short funcGenWaveTypeValue = getFuncGenWaveType(waveType);
		sendBytesToDevice(FUNC_GEN_WAVE_TYPE_COMMAND, funcGenWaveTypeValue);

		// send function gen p2p voltage
		short funcGenP2PVoltageValue = convertToMilliVolts(p2pVoltage);
		sendBytesToDevice(FUNC_GEN_P2P_VOLTAGE_COMMAND, funcGenP2PVoltageValue);

		// send function gen offset
		short funcGenOffsetValue = convertToMilliVolts(offset);
		sendBytesToDevice(FUNC_GEN_OFFSET_COMMAND, funcGenOffsetValue);

		// send function gen frequency
		short funcGenFrequencyValue = Short.parseShort(frequency);
		sendBytesToDevice(FUNC_GEN_FREQUENCY_COMMAND,funcGenFrequencyValue);

	}

	public void sendForceTriggerCommand() {
		sendBytesToDevice(FORCE_TRIGGER_COMMAND, padding);
	}

	public void sendRearmTriggerCommand() {
		sendBytesToDevice(REARM_TRIGGER_COMMAND, padding);
	}

	// check if it displays properly on the lab computers.
	public void sendFirmwareConfig(String channelCoupling, String voltsPerDiv,
			String timePerDiv, String triggerMode,
			String triggerType, short triggerThreshold,
			String samplingMode, int numSamplesToAcquire,
			char channelToTrigger, short channelOffset,
			String bandpassSampling) {


		// send voltage per division command
		short voltageValue = getVoltageInMilliVolts(voltsPerDiv);
		sendBytesToDevice(VOLTAGE_PER_DIVISION_COMMAND, voltageValue);


		// send time per division command
		short timeValue = getTimeRes(timePerDiv);
		sendBytesToDevice(TIME_PER_DIVISION_COMMAND, timeValue);

		//send number of samples to acquire
		sendBytesToDevice(NUM_SAMPLES_COMMAND, (char) numSamplesToAcquire);

		// send sampling mode
		short samplingModeValue = getSamplingMode(samplingMode);
		sendBytesToDevice(SAMPLING_MODE_COMMAND, samplingModeValue);

		// send trigger threshold - should be in millivolts
		sendBytesToDevice(TRIGGER_THRESHOLD_COMMAND, triggerThreshold);

		// send channel offsets - should be in millivolts
		sendBytesToDevice(CHANNEL_OFFSETS_COMMAND, channelOffset);

		// send trigger type
		short triggerTypeValue = getTriggerType(triggerType);
		sendBytesToDevice(TRIGGER_TYPE_COMMAND, triggerTypeValue);

		// send trigger mode
		short triggerModeValue = getTriggerMode(triggerMode);
		sendBytesToDevice(TRIGGER_MODE_COMMAND, triggerModeValue);

		// send channel coupling
		short channelCouplingValue = getChannelCouplingMode(channelCoupling);
		sendBytesToDevice(CHANNEL_COUPLING_COMMAND, channelCouplingValue);

		// send channel to trigger
		short chanToTrigger = getChannelToTrigger(channelToTrigger);
		sendBytesToDevice(CHANNEL_TO_TRIGGER_COMMAND, chanToTrigger);

		// send bandpass sampling to device
		short bandpassSamplingValue = getBandpassSampling(bandpassSampling);
		sendBytesToDevice(BANDPASS_SAMPLING, bandpassSamplingValue);
	}



	private void sendBytesToDevice(short command, short data) {
		ByteBuffer commandBuffer = ByteBuffer.allocate(4);
		commandBuffer.putShort(command);
		commandBuffer.putShort(data);
		byte[] commandByteArray = commandBuffer.array();
		sendToDevice(commandByteArray);
	}

	private void sendBytesToDevice(short command, char data) {
		ByteBuffer commandBuffer = ByteBuffer.allocate(4);
		commandBuffer.putShort(command);
		commandBuffer.putChar(data);
		byte[] commandByteArray = commandBuffer.array();
		sendToDevice(commandByteArray);
	}

	/*************************************************
	 * Marshalling parameters into shorts to convert
	 * into bytes, for sending into firmware
	 *************************************************/

	private short getVoltageInMilliVolts(String voltagePerDivision) {
		switch(voltagePerDivision) {
		case "20mV":
			return 20;
		case "50mV":
			return 50;
		case "100mV":
			return 100;
		case "200mV":
			return 200;
		case "500mV":
			return 500;
		case "1V":
			return 1000;
		case "2V":
			return 2000;

		default:
			return 0;
		}
	}

	private short getTriggerMode(String triggerMode) {
		switch (triggerMode) {
		case "Auto":
			return 0x0000;
		case "Normal":
			return 0x0001;
		case "Single":
			return 0x0002;

		default:
			return 0;
		}
	}

	private short getTriggerType(String triggerType) {
		switch (triggerType) {
		case "Rising":
			return 0x0000;
		case "Falling":
			return 0x0001;
		case "Level":
			return 0x0002;

		default:
			return 0;

		}
	}

	private short getSamplingMode(String samplingMode) {
		switch (samplingMode) {
		case "8 bit":
			return 0x0000;
		case "12 bit":
			return 0x0001;

		default:
			return 0;
		}
	}

	private short getChannelCouplingMode(String channelCouplingMode) {
		switch (channelCouplingMode) {
		case "AC":
			return 0x0000;
		case "DC":
			return 0x0001;

		default:
			return 0;
		}
	}

	private short getTimeRes(String timeRes) {

		final char c = '\u00B5';

		switch (timeRes) {
		case "1" + c + "s":
			return 0x3132;
		case "2" + c + "s":
			return 0x3133;
		case "5" + c + "s":
			return 0x3134;
		case "10" + c + "s":
			return 0x3135;
		case "20" + c + "s":
			return 0x3136;
		case "50" + c + "s":
			return 0x3137;
		case "100" + c + "s":
			return 0x3138;
		case "200" + c + "s":
			return 0x3139;
		case "500" + c + "s":
			return 0x3140;
		case "1ms":
			return 0x3141;
		case "2ms":
			return 0x3142;
		case "5ms":
			return 0x3143;
		case "10ms":
			return 0x3144;
		case "20ms":
			return 0x3145;
		case "50ms":
			return 0x3146;
		case "100ms":
			return 0x3147;
		case "200ms":
			return 0x3148;
		case "500ms":
			return 0x3149;
		case "1s":
			return 0x3150;

		}

		return 0;
	}

	private short getFuncGenValue(String output) {
		switch(output) {
		case "On":
			return 0x0000;
		case "Off":
			return 0x0001;

		default:
			return 0;
		}
	}

	private short getChannelToTrigger(char channelToTrigger) {
		switch(channelToTrigger) {
		case 'A':
			return 0x0000;
		case 'B':
			return 0x0001;

		default:
			return 0;
		}
	}

	private short getBandpassSampling(String bandpassSampling) {
		switch(bandpassSampling) {
		case "On":
			return 0x0000;
		case "Off":
			return 0x0001;

		default:
			return 0;
		}
	}

	private short getFuncGenWaveType(String waveType) {
		switch(waveType) {
		case "Sine":
			return 0x0000;
		case "Square":
			return 0x0001;
		case "Triangle":
			return 0x0002;
		case "Ramp":
			return 0x0003;
		case "Noise":
			return 0x0004;

		default:
			return 0;
		}
	}


	/*************************************************
	 * Marshalling parameters out of shorts to reflect
	 * in the user interface
	 *************************************************/

	private void changeChannelCoupling(short value) {
		String channelCouplingValue = "";

		switch(value) {
		case 0x0000:
			channelCouplingValue = "AC";
			break;
		case 0x0001:
			channelCouplingValue = "DC";
			break;
		}

		model.setChannelCoupling(channelCouplingValue);
		model.emit("changeChannelCoupling");
	}

	// update the configuration on the right panel
	private void changeVoltagePerDiv(short value) {
		String voltageValue = getVoltageStringFromMilliVolts(value);
		model.setVoltsPerDivision(voltageValue);
		model.emit("voltsPerDivisionChanged");
	}

	// update the configuration on the right panel
	private void changeTimePerDiv(short value) {
		String timePerDivValue = getTimeString(value);
		model.setTimePerDivision(timePerDivValue);
		model.emit("timePerDivisionChanged");
	}

	private void changeTriggerMode(short value) {

		String triggerModeValue = "";

		switch(value) {
		case 0x0000:
			triggerModeValue = "Auto";
			break;
		case 0x0001:
			triggerModeValue = "Normal";
			break;
		case 0x0002:
			triggerModeValue = "Single";
			break;
		}

		model.setTriggerMode(triggerModeValue);
		model.emit("changeTriggerMode");

	}


	private void changeTriggerThreshold(short value) {
		model.setTriggerThreshold(convertVoltageToString(value));
		model.emit("triggerThresholdChanged");
	}

	private void changeFuncGenOutput(short value) {

		boolean funcGenOutput = false;

		switch(value) {
		case 0x0000:
			funcGenOutput = true;
			break;
		case 0x0001:
			funcGenOutput = false;
			break;
		}

		model.getCurrentFunctionGeneratorConfigurations().setOutputOn(funcGenOutput);
		model.emit("functionOutputConfigChanged");

	}

	private void changeFuncGenWaveType(short value) {

		String funcGenWaveType = "";

		switch (value) {
		case 0x0000:
			funcGenWaveType = "Sine";
			break;
		case 0x0001:
			funcGenWaveType = "Square";
			break;
		case 0x0002:
			funcGenWaveType = "Triangle";
			break;
		case 0x0003:
			funcGenWaveType = "Ramp";
			break;
		case 0x0004:
			funcGenWaveType = "Noise";
			break;
		}

		model.getCurrentFunctionGeneratorConfigurations().setWaveType(
				funcGenWaveType);
		model.emit("funcGenWaveTypeChanged");

	}


	private void changeFuncGenP2PVoltage(short value) {
		double valueInVolts = value/1000.0;
		model.getCurrentFunctionGeneratorConfigurations().setPeakToPeakVoltage(
				valueInVolts);
		model.emit("funcGenP2PVoltageChanged");
	}


	private void changeFuncGenOffset(short value) {
		double valueInVolts = value/1000.0;
		model.getCurrentFunctionGeneratorConfigurations().setOffset(valueInVolts);
		model.emit("funcGenOffsetChanged");
	}

	private void changeFuncGenFrequency(short value) {
		model.getCurrentFunctionGeneratorConfigurations().setFrequency(value);
		model.emit("funcGenFrequencyChanged");
	}

	// the value of the sampling rate will be in khz
	private void changeSamplingRate(short value) {
		model.setSamplingRate((int)value*1000);
		model.emit("samplingRateChanged");
	}

	private void changeDeviceStatus(short value) {
		//todo:
		String deviceStatus = "";

		switch(value) {
		case 0x0000:
			deviceStatus = "Armed";
			break;
		case 0x0001:
			deviceStatus = "Triggered";
			break;
		case 0x0002:
			deviceStatus = "Stopped";
			break;
		}

		model.setDeviceStatus(deviceStatus);
		model.emit("deviceStatusChanged");

	}

	private void changeTriggerType(short value) {
		String triggerTypeValue = "";

		switch(value) {
		case 0x0000:
			triggerTypeValue = "Rising";
			break;
		case 0x0001:
			triggerTypeValue = "Falling";
			break;
		case 0x0002:
			triggerTypeValue = "Level";
			break;
		}

		model.setTriggerType(triggerTypeValue);
		model.emit("changeTriggerType");

	}

	private short convertToMilliVolts(String value) {
		double doubleValue = Double.parseDouble(value);
		doubleValue = doubleValue * 1000; //convert to millivolts
		return (short)doubleValue;
	}

	private String convertVoltageToString(short value) {
		double valueInVolts = value/1000.0;
		DecimalFormat df = new DecimalFormat("#.###");
		return df.format(valueInVolts).toString();
	}

	private String getVoltageStringFromMilliVolts(short value) {
		switch(value) {
		case 20:
			return "20mV";
		case 50:
			return "50mV";
		case 100:
			return "100mV";
		case 200:
			return "200mV";
		case 500:
			return "500mV";
		case 1000:
			return "1V";
		case 2000:
			return "2V";

		default:
			return "";

		}
	}

	private String getTimeString(short value) {

		final char c = '\u00B5';
		switch(value) {
		case 0x3132:
			return "1" + c + "s";
		case 0x3133:
			return "2" + c + "s";
		case 0x3134:
			return "5" + c + "s";
		case 0x3135:
			return "10" + c + "s";
		case 0x3136:
			return "20" + c + "s";
		case 0x3137:
			return "50" + c + "s";
		case 0x3138:
			return "100" + c + "s";
		case 0x3139:
			return "200" + c + "s";
		case 0x3140:
			return "500" + c + "s";
		case 0x3141:
			return "1ms";
		case 0x3142:
			return "2ms";
		case 0x3143:
			return "5ms";
		case 0x3144:
			return "10ms";
		case 0x3145:
			return "20ms";
		case 0x3146:
			return "50ms";
		case 0x3147:
			return "100ms";
		case 0x3148:
			return "200ms";
		case 0x3149:
			return "500ms";
		case 0x3150:
			return "1s";

		default:
			return "";

		}

	}


}