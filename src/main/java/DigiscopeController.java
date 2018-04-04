import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.lang.reflect.Array;
import java.text.DecimalFormat;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * The application's controller using the MVC architecture
 * @author Lisa Liu-Thorrold
 *
 */
public class DigiscopeController {

	// The model from the MVC architecture
	private final DigiscopeModel model;

	// The view from the MVC architecture
	private final DigiscopeContainer view;

	// The main oscilloscope display
	private OscilloscopeDisplay display;

	// The digiscope client
	private DigiscopeServer digiscopeServer;


	/**
	 * This is the controller of the application in the MVC framework
	 * @param model - The application's model
	 * @param container - The view of the application
	 */
	public DigiscopeController(DigiscopeModel model, DigiscopeContainer container) {
		this.model = model;
		this.view = container;
		this.digiscopeServer = model.getDigiscopeServer();

		initOscilloscopeDisplay();

		// Add listeners for the GUI components
		view.addListener("connect", event -> connectToDevice());
		view.addListener("sendFunctionGeneratorConfig", event -> sendFunctionGeneratorConfig());
		view.addListener("parseFilterChannelInputFile", event -> parseFilterChannelInputFile());
		view.addListener("forceTriggerButtonPressed" , event -> forceTriggerButtonPressed());
		view.addListener("rearmTriggerButtonPressed", event -> rearmTriggerButtonPressed());
		view.addListener("sendConfigurationsToFirmware", event -> sendConfigurationsToFirmware());
		view.addListener("setMathEquation", event -> setMathEquation());
		view.addListener("channelACheckBoxChecked", event -> channelACheckBoxChecked());
		view.addListener("channelBCheckBoxChecked", event -> channelBCheckBoxChecked());
		view.addListener("mathChannelCheckBoxChecked", event -> mathChannelCheckBoxChecked());
		view.addListener("filterChannelCheckBoxChecked", event -> filterChannelCheckBoxChecked());
		view.addListener("verticalRangeResolutionChanged", event -> verticalRangeResolutionChanged());
		view.addListener("horizontalRangeResolutionChanged", event -> horizontalRangeResolutionChanged());
		view.addListener("filterInputChannelChanged", event -> updateFilterChannelInput());

		// Listeners for events from the touchscreen lcd display
		model.addListener("updateMeasurementLabels", event -> updateMeasurementLabels());
		model.addListener("setScalingComboBoxes", event -> setScalingComboBoxes());
		model.addListener("sampleVoltageSelected", event -> samplingVoltageSelected());
		model.addListener("changeChannelCoupling", event -> changeChannelCoupling());
		model.addListener("changeTriggerMode", event -> changeTriggerMode());
		model.addListener("changeTriggerType", event -> changeTriggerType());
		model.addListener("voltsPerDivisionChanged", event -> changeVoltsPerDivision());
		model.addListener("timePerDivisionChanged", event -> changeTimePerDivision());
		model.addListener("triggerThresholdChanged", event -> changeTriggerThreshold());
		model.addListener("functionOutputConfigChanged", event -> functionOutputConfigChanged());
		model.addListener("funcGenWaveTypeChanged", event -> funcGenWaveTypeChanged());
		model.addListener("funcGenP2PVoltageChanged", event -> funcGenP2PVoltageChanged());
		model.addListener("funcGenOffsetChanged", event -> funcGenOffsetChanged());
		model.addListener("funcGenFrequencyChanged", event -> funcGenFrequencyChanged());
		model.addListener("samplingRateChanged", event -> samplingRateChanged());
		model.addListener("incorrectSamplesSent", event -> incorrectSamplesSent());
		model.addListener("deviceStatusChanged", event -> deviceStatusChanged());

	}

	/*************************************************
	 * Control event listeners
	 *************************************************/

	/**
	 * This method is invoked when the a trigger has been received, and
	 * calculations have been performed. This method updates the measurement
	 * labels displayed under the oscilloscope display to reflect the new
	 * samples received.
	 */
	private void updateMeasurementLabels() {

		if (model.getChannelAisPlotted()) {
			view.setChannelAMeasurements(model.getChannelA().getMinVoltage(),
					model.getChannelA().getMaxVoltage(),
					model.getChannelA().getMaxP2Pvoltage(),
					model.getChannelA().getAverageVoltage(),
					model.getChannelA().getStandardVoltageDeviation(),
					model.getChannelA().getFrequency(), true,
					model.getChannelA().getVerticallyOffTheScreen());
		} else {
			view.setChannelAMeasurements(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, false,
					model.getChannelA().getVerticallyOffTheScreen());
		}


		if (model.getChannelBisPlotted()) {
			view.setChannelBMeasurements(model.getChannelB().getMinVoltage(),
					model.getChannelB().getMaxVoltage(), 
					model.getChannelB().getMaxP2Pvoltage(),
					model.getChannelB().getAverageVoltage(),
					model.getChannelB().getStandardVoltageDeviation(), 
					model.getChannelB().getFrequency(), true,
					model.getChannelB().getVerticallyOffTheScreen());
		} else {
			view.setChannelBMeasurements(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, false,
					model.getChannelB().getVerticallyOffTheScreen());
		}

		if (model.getMathChannelIsPlotted()) {
			view.setMathChannelMeasurements(model.getMathChannel().getMinVoltage(),
					model.getMathChannel().getMaxVoltage(), 
					model.getMathChannel().getMaxP2Pvoltage(),
					model.getMathChannel().getAverageVoltage(),
					model.getMathChannel().getStandardVoltageDeviation(), 
					model.getMathChannel().getFrequency(), true,
					model.getMathChannel().getVerticallyOffTheScreen());
		} else {
			view.setMathChannelMeasurements(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, false,
					model.getMathChannel().getVerticallyOffTheScreen());
		}

		if (model.getFilterChannelIsPlotted()) {
			view.setFilterChannelMeasurements(model.getFilterChannel().getMinVoltage(),
					model.getFilterChannel().getMaxVoltage(), 
					model.getFilterChannel().getMaxP2Pvoltage(),
					model.getFilterChannel().getAverageVoltage(),
					model.getFilterChannel().getStandardVoltageDeviation(), 
					model.getFilterChannel().getFrequency(), true,
					model.getFilterChannel().getVerticallyOffTheScreen());

		} else {
			view.setFilterChannelMeasurements(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, false,
					model.getFilterChannel().getVerticallyOffTheScreen());
		}

	}

	/**
	 * This method is invoked when the user has changed the vertical resolution.
	 */
	private void verticalRangeResolutionChanged() {

		// get the current displayed resolution
		String vertRange =
				view.getVerticalRangeDisplayComboBoxModel().getSelectedItem().toString();

		model.setVoltsPerDivisionDisplayed(vertRange);

		//replot the drawing
		model.getOscilloscopeDisplay().updateResolution(1);
		
		updateMeasurementLabels();
	}


	/**
	 * This method is invoked when the user has clicked on the screen to 
	 * display the value of the sample voltage clicked.
	 */
	private void samplingVoltageSelected() {
		double samplingVoltageSelected = model.getSelectedSampleVoltage();

		if (samplingVoltageSelected == Double.NEGATIVE_INFINITY) {
			view.setCurrentSelectedVoltageLabel("");
		} else {
			DecimalFormat df = new DecimalFormat("#.###");  			
			view.setCurrentSelectedVoltageLabel(
					df.format(samplingVoltageSelected) + "V");
		}
	}

	/**
	 * This method is invoked update the scaling comboboxes to display the 
	 * correct resolution when a sample is received.
	 */
	private void setScalingComboBoxes() {	
		String voltsPerDiv, timePerDiv;

		voltsPerDiv = view.getVoltsPerDivComboBox().getSelectedItem().toString();
		view.getVerticalRangeDisplayComboBoxModel().setSelectedItem(voltsPerDiv);

		timePerDiv = view.getTimePerDivComboBox().getSelectedItem().toString();
		view.getHorizontalRangeDisplayComboBoxModel().setSelectedItem(timePerDiv);
	}

	/**
	 * This method is invoked when the user has changed the source of the input
	 * into the filter channel.
	 */
	private void updateFilterChannelInput() {
		// Get the filter channel input
		String filterChannelInput =
				view.getFilterInputChannelComboBoxModel().getSelectedItem().toString();

		if (filterChannelInput.contains("Math")) {
			if (model.getMathChannel().getEquation().contains("F")) {
				view.showMessageDialog("Filter Channel Input not allowed: " +
						"deadlock dependency");
				view.getFilterInputChannelComboBoxModel().setSelectedItem(
						model.getFilterChannelInput());
				return;
			}
		}

		model.setFilterChannelInput(filterChannelInput);
		model.getFilterChannel().setFilterInputSet(true);

		if (model.getFilterChannel().getFilterChannelFilesSet()) {
			model.getFilterChannel().setAvailableForPlotting(true);
		}

	}

	/**
	 * This method is invoked when the user has adjusted the horizontal (time)
	 * resolution
	 */
	private void horizontalRangeResolutionChanged() {

		System.out.println("Horizontal resolution changed");

		String horizontalRange =
				view.getHorizontalRangeDisplayComboBoxModel().getSelectedItem().toString();

		// Get the new displayed resolution
		double currentTimeUnit = model.getTimePerDivisionDisplayed();
		double newTimeUnit = getStandardTimeUnit(horizontalRange);

		double scalingFactor = currentTimeUnit/newTimeUnit;

		// Set the new resolution
		model.setTimePerDivisionDisplayed(horizontalRange);

		// Replot the drawing
		model.getOscilloscopeDisplay().updateResolution(scalingFactor);
		
		updateMeasurementLabels();

	}

	/**
	 * This method is invoked when the Channel A check box has been checked or
	 * unchecked. Model is updated accordingly.
	 */
	private void channelACheckBoxChecked() {
		model.setPlotChannelA(view.getChannelACheckBox().isSelected());
		if (model.getChannelA().getChannelSamples() != null) {
			model.getOscilloscopeDisplay().updateResolution(1);
			updateMeasurementLabels();
		}
		
	}

	/**
	 * This method is invoked when the Channel B check box has been checked or
	 * unchecked. Model is updated accordingly.
	 */
	private void channelBCheckBoxChecked() {
		model.setPlotChannelB(view.getChannelBCheckBox().isSelected());
		
		if (model.getChannelB().getChannelSamples() != null) {
		model.getOscilloscopeDisplay().updateResolution(1);
			updateMeasurementLabels();
		}
	}

	/**
	 * This method is invoked when the Math Channel check box has been checked or
	 * unchecked. Model is updated accordingly.
	 */
	private void mathChannelCheckBoxChecked() {

		if ((model.getMathChannel().getEquation().equals("")) &&
				view.getMathChannelCheckBox().isSelected()) {
			view.getMathChannelCheckBox().setSelected(false);
			view.showMessageDialog("Math channel equation is empty, " +
					"you need to set this first");
		}

		model.setPlotMathChannel(view.getMathChannelCheckBox().isSelected());
		
		if (model.getMathChannel().getChannelSamples() != null) {
			model.getOscilloscopeDisplay().updateResolution(1);
			updateMeasurementLabels();
		}
		
	}

	/**
	 * This method is invoked when the Filter Channel check box has been checked
	 * or unchecked. Model is updated accordingly.
	 */
	private void filterChannelCheckBoxChecked() {	

		if (((model.getFilterChannel().getFilterType() == null) ||
				(model.getFilterChannelInput() == null)) &&
				view.getFilterChannelCheckBox().isSelected()) {
			view.getFilterChannelCheckBox().setSelected(false);
			view.showMessageDialog("Filter channel file empty or input not " +
					"selected. Do this first");
		}
		model.setPlotFilterChannel(view.getFilterChannelCheckBox().isSelected());
		
		if (model.getFilterChannel().getChannelSamples() != null) {
			model.getOscilloscopeDisplay().updateResolution(1);
			updateMeasurementLabels();
		}
	}


	/**
	 * This method establishes a connection to the device. We can test this
	 * without using a device by running nc -l 10000 on the terminal.
	 */
	private void connectToDevice() {

		if (!model.isConnected()) {
			try {
				int portNumber =
						Integer.parseInt(view.getPortNumberTextField().getText());
				model.setPortNumber(portNumber);
				model.setIpAddress(view.getDeviceIpTextField().getText());
			} catch (Exception e) {
				view.showMessageDialog("Port Number must be an int");
				disconnect();
				return;
			}

			try {
				digiscopeServer.connect(model.getIpAddress(), model.getPortNumber());
			} catch (Exception e) {
				view.showMessageDialog("Error connected to device. " +
						"Enter a valid IP address and port number");
				disconnect();
				return;
			}

			view.showMessageDialog("Message connection successful");
			view.getConnectButton().setText("Disconnect");
			model.setConnected(true);
			view.setConnectionStatusLabel("Yes");
			view.setForceTriggerButtonEnabled(true);

		} else {
			digiscopeServer.disconnect();
			disconnect();
		}

	}

	/**
	 * This method gets and sets the following things to model, and firmware:
	 * 1. Channel Coupling (AC or DC)
	 * 2. Gets volts per division
	 * 3. Gets time per division
	 * 4. Gets trigger mode
	 * 5. Gets trigger type
	 * 6. Gets trigger threshold
	 * 7. Gets sampling mode
	 * 8. Gets number of samples to acquire.
	 */
	private void sendConfigurationsToFirmware() {

		String channelCoupling;
		String voltsPerDiv;
		String timePerDiv;
		String triggerMode;
		String triggerType;
		String bandpassSampling;
		short triggerThreshold;
		String samplingMode;
		int numSamplesToAcquire;
		char channelToTrigger;
		short channelOffset;

		view.setForceTriggerButtonEnabled(false);

		if (!model.isConnected()) {
			view.showMessageDialog(
					"Device is not connected, cannot send configuration.");
			return;
		}

		if (view.getChannelCouplingComboBox().getSelectedItem() != null) {
			channelCoupling =
					view.getChannelCouplingComboBox().getSelectedItem().toString();
		} else {
			view.showMessageDialog(
					"Channel Coupling needs to be selected. Configuration not sent.");
			return;
		}

		if (view.getVoltsPerDivComboBox().getSelectedItem() != null) {
			voltsPerDiv = view.getVoltsPerDivComboBox().getSelectedItem().toString();
			model.setVoltsPerDivision(voltsPerDiv);
		} else {
			view.showMessageDialog(
					"Volts per division needs to be selected. Configuration not sent.");
			return;
		}

		if (view.getTimePerDivComboBox().getSelectedItem() != null) {
			timePerDiv = view.getTimePerDivComboBox().getSelectedItem().toString();
			model.setTimePerDivision(timePerDiv);
		} else {
			view.showMessageDialog(
					"Time per division needs to be selected. Configuration not sent.");
			return;
		}

		if (view.getTriggerModeComboBox().getSelectedItem() != null) {
			triggerMode = view.getTriggerModeComboBox().getSelectedItem().toString();
		} else {
			view.showMessageDialog(
					"Trigger mode needs to be selected. Configuration not sent.");
			return;
		}

		
		if (view.getBandpassSamplingComboBox().getSelectedItem() != null) {
			bandpassSampling =
					view.getBandpassSamplingComboBox().getSelectedItem().toString();
		} else {
			view.showMessageDialog("Bandpass sampling mode needs to be " +
					"selected. Configuration not sent.");
			return;
		}

		if (view.getTriggerTypeComboBox().getSelectedItem() != null) {
			triggerType = view.getTriggerTypeComboBox().getSelectedItem().toString();
		} else {
			view.showMessageDialog(
					"Trigger type needs to be selected. Configuration not sent.");
			return;
		}


		try {
			Double tempDouble = Double.parseDouble(
					view.getTriggerThresholdTextBox().getText());
			tempDouble = tempDouble*(1000.00);
			triggerThreshold = tempDouble.shortValue();

		} catch (Exception e) {
			view.showMessageDialog(
					"Please enter a valid trigger threshold. Configuration not sent.");
			return;
		}
		
		try {
			Double tempDouble = Double.parseDouble(
					view.getChannelOffsetsTextField().getText());
			tempDouble = tempDouble*(1000.00);
			channelOffset = tempDouble.shortValue();

		} catch (Exception e) {
			view.showMessageDialog(
					"Please enter a valid channel offset. Configuration not sent.");
			return;
		}

		if (view.getSamplingModeComboBox().getSelectedItem() != null) {
			samplingMode = view.getSamplingModeComboBox().getSelectedItem().toString();
		} else {
			view.showMessageDialog(
					"Sample mode needs to be selected. Configuration not sent.");
			return;
		}

		try {
			numSamplesToAcquire = Integer.parseInt(
					view.getNumberOfSamplesToAcquireTextField().getText());
		} catch (Exception e) {
			view.showMessageDialog("Enter a valid number of samples to " +
					"acquire. Configuration not sent.");
			return;
		}
		
		if (view.getChannelToTriggerComboBox().getSelectedItem() != null) {
			String selectedItem =
					view.getChannelToTriggerComboBox().getSelectedItem().toString();
			channelToTrigger = selectedItem.charAt(0);
		} else {
			view.showMessageDialog("Channel to trigger needs to be selected."
					+ " Configuration not sent");
			return;
		}
		
		if(numSamplesToAcquire < 0) {
			view.showMessageDialog("Number of samples to acquire must be " +
					"positive. Configuration not sent");
			return;
		}
		

		if (samplingMode.equals("12 bit") && numSamplesToAcquire > 25000) {
			view.showMessageDialog("Can only acquire a maximum of 25,000 samples "
					+ "per channel in 12 bit mode. Configuration not sent.");
			return;
		}

		if (numSamplesToAcquire > 50000) {
			view.showMessageDialog("Can only acquire a maximum of 50,000 "
					+ "samples per channel in 8 bit mode. Configuration not sent.");
			return;
		}


		if (triggerMode.equals("Single")) {
			view.setRearmTriggerButtonEnabled(true);
		} else {
			view.setRearmTriggerButtonEnabled(false);
		}

		digiscopeServer.sendFirmwareConfig(channelCoupling, voltsPerDiv,
				timePerDiv, triggerMode, triggerType, triggerThreshold, 
				samplingMode, numSamplesToAcquire, channelToTrigger, 
				channelOffset, bandpassSampling);

		model.setInitialConfigSent(true);
		view.setForceTriggerButtonEnabled(true);
		model.setSamplingMode(samplingMode);
		model.setBandpassSampling(bandpassSampling);
		model.setNumSamplesToAcquire(numSamplesToAcquire);
	}

	/**
	 * This method sends a method to the device to get samples
	 */
	private void forceTriggerButtonPressed() {
		digiscopeServer.sendForceTriggerCommand();
	}

	/**
	 * This method sends a rearm command to the device.
	 */
	private void rearmTriggerButtonPressed() {
		digiscopeServer.sendRearmTriggerCommand();
	}

	/**
	 * This method parses the filter channel input file. 1 columns is
	 * FIR filter, 2 columns is IIR filter.
	 */
	private void parseFilterChannelInputFile() {

		String filterType = null;
		int numberOfLines = 0;
		int arrayIndexCount = 0;

		JFileChooser fileChooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				".csv files", "csv");
		fileChooser.setFileFilter(filter);
		fileChooser.setAcceptAllFileFilterUsed(false);
		int result = fileChooser.showOpenDialog(fileChooser);
		if (result == JFileChooser.APPROVE_OPTION) {

			File selectedFile = fileChooser.getSelectedFile();
			BufferedReader bufferedReader = null;
			String line;
			String cvsSplitBy = ",";

			// Open the file to get the number of lines first
			LineNumberReader lnr;
			try {
				lnr = new LineNumberReader(new FileReader(selectedFile));
				lnr.skip(Long.MAX_VALUE);

				numberOfLines = lnr.getLineNumber();
				// Closed to prevent resource leak
				lnr.close();
			} catch (Exception e) {
				// do nothing
			}

			double[] firFilter = new double[numberOfLines];
			double[] iirFilter1 = new double[numberOfLines];
			double[] iirFilter2 = new double[numberOfLines];


			try {
				bufferedReader = new BufferedReader(new FileReader(selectedFile));

				line = bufferedReader.readLine();
				String[] filterInputs = line.split(cvsSplitBy);	

				if (Array.getLength(filterInputs) == 1) {
					filterType = "FIR";
					firFilter[arrayIndexCount] =
							Double.parseDouble(filterInputs[0]);
				} else if (Array.getLength(filterInputs) == 2) {
					filterType = "IIR";
					iirFilter1[arrayIndexCount] =
							Double.parseDouble(filterInputs[0]);
					iirFilter2[arrayIndexCount] =
							Double.parseDouble(filterInputs[1]);
				}

				arrayIndexCount++;

				while ((line = bufferedReader.readLine()) != null) {
					// use comma as separator
					filterInputs = line.split(cvsSplitBy);	

					if (filterType.equals("FIR")) {
						firFilter[arrayIndexCount] =
								Double.parseDouble(filterInputs[0]);
					} else if (filterType.equals("IIR")) {
						iirFilter1[arrayIndexCount] =
								Double.parseDouble(filterInputs[0]);
						iirFilter2[arrayIndexCount] =
								Double.parseDouble(filterInputs[1]);
					}

					arrayIndexCount++;
				}

				if (filterType.equals("FIR")) {
					model.getFilterChannel().setFirFilter(firFilter);
				} else if (filterType.equals("IIR")) {
					model.getFilterChannel().setIirFilter(iirFilter1, iirFilter2);
				}

				model.getFilterChannel().setFilterType(filterType);
				view.setFilterChannelLabel(filterType);


			}	catch (Exception e) {
				JOptionPane.showMessageDialog(null, "Filter file "
						+ "format not correct");
				e.printStackTrace();
			} finally {
				try {
					assert bufferedReader != null;
					bufferedReader.close();
				} catch (IOException e) {
					JOptionPane.showMessageDialog(null, "Filter file"
							+ " format not correct");
				}
			}
		}

		model.getFilterChannel().setFilterChannelFilesSet(true);

		if (model.getFilterChannel().getFilterInputSet()) {
			model.getFilterChannel().setAvailableForPlotting(true);
		}
	}

	/**
	 * This method sends function generator configuration to the firmware
	 */
	private void sendFunctionGeneratorConfig() {

		if (!model.isConnected()) {
			view.showMessageDialog("Device is not connected, " +
					"cannot send configuration");
			return;
		}

		boolean outputOn;
		String waveType;
		double p2pVoltage;
		double offset;
		short frequency;

		if (view.getFunctionGeneratorOutputComboBox().getSelectedIndex() != -1) {
			if (view.getFunctionGeneratorOutputComboBox().getSelectedItem().
					toString().contains("On")) {
				outputOn  = true;
			} else {
				outputOn = false;
			}
		} else {
			view.showMessageDialog("Do you want function generator on or off?");
			return;
		}

		if (view.getFunctionGeneratorWaveTypeComboBox().getSelectedIndex() != -1) {
			waveType = view.getFunctionGeneratorWaveTypeComboBox().
					getSelectedItem().toString();
		} else {
			view.showMessageDialog("What waveform do you want the " +
					"function generator to make?");
			return;
		}


		try {
			p2pVoltage = Double.parseDouble(
					view.getFunctionGeneratorP2PTextField().getText());
			offset = Double.parseDouble(
					view.getFunctionGeneratorOffsetTextField().getText());
			frequency = Short.parseShort(
					view.getFunctionGeneratorFrequencyTextField().getText());
		} catch (Exception e) {
			view.showMessageDialog("Invalid inputs");
			return;
		}

		
		FunctionGenerator fg = model.getCurrentFunctionGeneratorConfigurations();
		fg.setOutputOn(outputOn);
		fg.setWaveType(waveType);
		fg.setPeakToPeakVoltage(p2pVoltage);
		fg.setOffset(offset);
		fg.setFrequency(frequency);
		
		//Send function generator configuration to the hardware
		digiscopeServer.sendFunctionGeneratorConfiguration((outputOn ? "On" :
				"Off"), waveType, Double.toString(p2pVoltage),
				Double.toString(offset), Short.toString(frequency));

	}

	/**
	 * Sets the math equation as input by the user
	 */
	private void setMathEquation() {
		String mathEquation = view.getMathChannelEquationTextField().getText();

		String pattern    = "[()ABF+-/*^pie.1234567890\\s]*";

		if (!mathEquation.matches(pattern)) {
			view.showMessageDialog("Invalid Math Equation");
			return;
		}

		if (!model.getFilterChannel().getAvailableForPlotting()
				&& mathEquation.contains("F")) {
			view.showMessageDialog("Filter channel is not being calculated, " +
					"select plot Filter channel to calculate");
			view.getMathChannelEquationTextField().setText(
					model.getMathChannel().getEquation());
			return;
		}

		// check for deadlock dependency
		if (model.getFilterChannelInput() != null) {
			if (model.getFilterChannelInput().contains("Math") &&
					mathEquation.contains("F")) {
				view.showMessageDialog("Filter Channel Depends on Math. " +
						"Deadlock situation - math equation not permitted");
				view.getMathChannelEquationTextField().setText(
						model.getMathChannel().getEquation());
				return;
			}
		}

		// need to check if the channel previously had either a or b, and now has neither
		String previousEquation = model.getMathChannel().getEquation();

		boolean currentEquationContains = mathEquation.contains("A") ||
				mathEquation.contains("B");
		boolean previousEquationContains = previousEquation == null ? false :
				(previousEquation.contains("A") || previousEquation.contains("B"));

		boolean currFilterChannelNull = model.getFilterChannelInput() == null;

		//need to check whether filter channel input was math.
		if ((!currFilterChannelNull) &&
				(model.getFilterChannelInput().contains("Math"))) {
			if (!currentEquationContains && previousEquationContains) {
				// if it was, it needs to be changed & message displayed
				view.getFilterInputChannelComboBoxModel().setSelectedItem("A");
				view.showMessageDialog("Math channel no longer contains " +
						"physical channel inputs, setting filter channel " +
						"input to channel A.");
			}
		}

		model.getMathChannel().setEquation(mathEquation);

		int index = view.getFilterInputChannelComboBoxModel().getIndexOf("Math");

		// Math channel contains a physical channel, can add this as input into
		// the filter channel section.
		if (mathEquation.contains("A") || mathEquation.contains("B")) {

			if (index == -1 ) {
				view.getFilterInputChannelComboBoxModel().addElement("Math");
			}
		} else {
			if (index != -1) {
				view.getFilterInputChannelComboBoxModel().removeElement("Math");
			}
		}

		model.getMathChannel().setAvailableForPlotting(true);
		view.showMessageDialog("Math equation set");

	}

	private void changeChannelCoupling() {
		// get the channel coupling combobox
		JComboBox<String> channelComboBox = view.getChannelCouplingComboBox();

		// get the channel coupling value
		String channelCouplingValue = model.getChannelCoupling();

		// set it
		channelComboBox.setSelectedItem(channelCouplingValue);
	}

	private void changeTriggerMode() {
		// get the trigger mode combobox
		JComboBox<String> triggerModeComboBox = view.getTriggerModeComboBox();

		// get the trigger mode value
		String triggerMode = model.getTriggerMode();

		// set it
		triggerModeComboBox.setSelectedItem(triggerMode);
	}

	private void changeTriggerType() {
		// get the trigger type combobox
		JComboBox<String> triggerTypeComboBox = view.getTriggerTypeComboBox();

		// get the trigger type value
		String triggerType = model.getTriggerType();

		//set it
		triggerTypeComboBox.setSelectedItem(triggerType);
	}

	private void changeVoltsPerDivision() {
		// get the volts per division combobox
		JComboBox<String> voltsPerDivisionComboBox = view.getVoltsPerDivComboBox();

		// get the volts per division string value
		String voltsPerDivisionValue = model.getVoltsPerDivisionStringValue();

		// set it
		voltsPerDivisionComboBox.setSelectedItem(voltsPerDivisionValue);

	}

	private void changeTimePerDivision() {
		// get time per division combobox
		JComboBox<String > timePerDivisionComboBox = view.getTimePerDivComboBox();

		// get the time per division string value
		String timePerDivisionValue = model.getTimePerDivisionAsString();

		//set it
		timePerDivisionComboBox.setSelectedItem(timePerDivisionValue);
	}

	private void changeTriggerThreshold() {
		// get the trigger threshold text field
		JTextField triggerThresholdTextBox = view.getTriggerThresholdTextBox();

		// get the trigger threshold value
		String triggerThreshold = model.getTriggerThreshold();

		// set it
		triggerThresholdTextBox.setText(triggerThreshold);

	}

	private void functionOutputConfigChanged() {
		//get the function generator output combo box
		JComboBox<String> funcOutputComboBox =
				view.getFunctionGeneratorOutputComboBox();

		// get the function generator output value
		boolean outputOn =
				model.getCurrentFunctionGeneratorConfigurations().getOutputOn();

		if (outputOn) {
			funcOutputComboBox.setSelectedItem("On");
		} else {
			funcOutputComboBox.setSelectedItem("Off");
		}
	}

	private void funcGenWaveTypeChanged() {
		System.out.println("here");
		
		//get the func gen wave type combobox
		JComboBox<String > funcGenWaveTypeComboBox =
				view.getFunctionGeneratorWaveTypeComboBox();

		// get the function generator wave type value
		String waveType =
				model.getCurrentFunctionGeneratorConfigurations().getWaveType();

		// set the value in the user interface
		funcGenWaveTypeComboBox.setSelectedItem(waveType);

	}

	private void funcGenP2PVoltageChanged() {
		System.out.println("here");
		
		// get the func gen p2p voltage textbox
		JTextField p2pTextField = view.getFunctionGeneratorP2PTextField();

		// get the function generator p2p wave type value we want to use
		double p2pValue = model.getCurrentFunctionGeneratorConfigurations().
				getPeakToPeakVoltage();

		// set the value in the user interface
		DecimalFormat df = new DecimalFormat("#.###");
		p2pTextField.setText(df.format(p2pValue).toString());
	}

	private void funcGenOffsetChanged() {
		System.out.println("here");
		
		// get the func gen offset textbox
		JTextField offsetTextField = view.getFunctionGeneratorOffsetTextField();

		// get the function generator p2p wave type value we want to use
		double offsetValue =
				model.getCurrentFunctionGeneratorConfigurations().getOffset();

		// set the value in the user interface
		DecimalFormat df = new DecimalFormat("#.###");
		offsetTextField.setText(df.format(offsetValue).toString());
	}

	private void funcGenFrequencyChanged() {
		System.out.println("here");
		
		// get the func gen offset textbox
		JTextField frequencyTextField =
				view.getFunctionGeneratorFrequencyTextField();

		// get the function generator p2p wave type value we want to use
		short frequencyValue =
				model.getCurrentFunctionGeneratorConfigurations().getFrequency();

		frequencyTextField.setText(Short.toString(frequencyValue));

	}

	private void samplingRateChanged() {
		int samplingRate = model.getSamplingRate();

		// Get the sampling rate label
		view.setCurrentSamplingRateLabel(samplingRate + "kHz");
	}
	
	private void deviceStatusChanged() {
		String deviceStatus = model.getDeviceStatus();
		
		// set the device status label
		view.getDeviceStatusLabel().setText(deviceStatus);
	}

	/*************************************************
	 * Private auxillary functions
	 *************************************************/

	/**
	 * This method initiates the oscilloscope display
	 */
	private void initOscilloscopeDisplay() {
		display = new OscilloscopeDisplay(model);
		display.init();
		view.getOscilloscopePanel().add(display);
		model.setOscilloscopeDisplay(display);
	}
	
	private void incorrectSamplesSent() {
		view.showMessageDialog("Error - incorrect number of samples sent");
	}

	/**
	 * Handles disconnection from device.
	 */
	private void disconnect() {
		view.setConnectionStatusLabel("No");
		view.setForceTriggerButtonEnabled(false);
		view.setRearmTriggerButtonEnabled(false);
		model.setConnected(false);
		view.getConnectButton().setText("Connect");
	}

	/**
	 * Converts a string time unit to a standard time unit (1s) as a double
	 * @param timeUnit - The string to convert
	 * @return the SI unit of the time
	 */
	private double getStandardTimeUnit(String timeUnit) {
		
		final char c = '\u00B5';
		
		switch(timeUnit) {
		case "1" + c + "s":
			return 0.000001;
		case "2" + c + "s":
			return 0.000002;
		case "5" + c + "s":
			return 0.000005;
		case "10" + c + "s":
			return 0.00001;
		case "20" + c + "s":
			return 0.00002;
		case "50" + c + "s":
			return 0.00005;
		case "100" + c + "s":
			return 0.0001;
		case "200" + c + "s":
			return 0.0002;
		case "500" + c + "s":
			return 0.0005;
		case "1ms":
			return 0.001;
		case "2ms":
			return 0.002;
		case "5ms":
			return 0.005;
		case "10ms":
			return 0.01;
		case "20ms":
			return 0.02;
		case "50ms":
			return 0.05;
		case "100ms":
			return 0.1;
		case "200ms":
			return 0.2;
		case "500ms":
			return 0.5;
		case "1s":
			return 1;

		}

		// should never reach here
		return 0;
	}

}