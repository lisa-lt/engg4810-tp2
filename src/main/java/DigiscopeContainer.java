import java.awt.*;

import javax.swing.*;
import java.text.DecimalFormat;

/**
 * This class encapsulates the entire application in a Graphical User Interface
 * @author Lisa Liu-Thorrold
 *
 */
public class DigiscopeContainer extends EventEmitter {

	/* Constants */
	private static final int WINDOW_WIDTH = 1450;
	private static final int WINDOW_HEIGHT = 820;

	private static final String APPLICATION_NAME = "Digiscope";

	/* The JFrame for the entire application */
	public JFrame frame;
	
	/* GUI Textfields */
	private JTextField deviceIpTextField;
	private JTextField portNumberTextField;
	private JTextField triggerThresholdTextField;
	private JTextField numberOfSamplesToAcquireTextField;
	private JTextField mathChannelEquationTextField;
	private JTextField functionGeneratorP2PTextField;
	private JTextField functionGeneratorOffsetTextField;
	private JTextField functionGeneratorFrequencyTextField;
	private JTextField channelOffsetsTextField;
	
	
	/* GUI Comboboxes */
	private JComboBox<String> channelCouplingComboBox;
	private JComboBox<String> horizontalRangeComboBox;
	private JComboBox<String> verticalRangeComboBox;
	private JComboBox<String> triggerModeComboBox;
	private JComboBox<String> triggerTypeComboBox;
	private JComboBox<String> samplingModeComboBox;
	private JComboBox<String> functionGeneratorOutputComboBox;
	private JComboBox<String> functionGeneratorWaveTypeComboBox;
	private JComboBox<String> verticalRangeDisplayComboBox;
	private JComboBox<String> horizontalRangeDisplayComboBox;
	private JComboBox<String> channelToTriggerComboBox;
	private JComboBox<String> bandpassSamplingComboBox;

	/* Combobox model */
	private DefaultComboBoxModel<String> filterInputChannelComboBoxModel;
	private DefaultComboBoxModel<String> horizontalRangeDisplayComboBoxModel;
	private DefaultComboBoxModel<String> verticalRangeDisplayComboBoxModel;
	
	/* GUI Checkboxes */
	private JCheckBox channelACheckBox;
	private JCheckBox channelBCheckBox;
	private JCheckBox mathChannelCheckBox;
	private JCheckBox filterChannelCheckBox;

	/* JButtons */
	private JButton forceTriggerButton;
	private JButton rearmTriggerButton;
	private JButton connectButton;
	
	public JPanel oscilloscopePanel;
	
	/* JLabels */
	private JLabel statusLabel;
	private JLabel filterTypeInfoLabel;
	private JLabel connectionStatusInfoLabel;
	private JLabel currentSamplingRateInfoLabel;
	private JLabel currentSelectedVoltageLabel;
	//Channel A
	private JLabel channelAMinLabel;
	private JLabel channelAMaxLabel;
	private JLabel channelAMaxP2PLabel;
	private JLabel channelAAverageLabel;
	private JLabel channelAStdDevLabel;
	private JLabel channelAFrequencyLabel;
	//Channel B
	private JLabel channelBMinLabel;
	private JLabel channelBMaxLabel;
	private JLabel channelBMaxP2PLabel;
	private JLabel channelBAverageLabel;
	private JLabel channelBStdDevLabel;
	private JLabel channelBFrequencyLabel;
	//Math channel
	private JLabel mathChannelMinLabel;
	private JLabel mathChannelMaxLabel;
	private JLabel mathChannelMaxP2PLabel;
	private JLabel mathChannelAverageLabel;
	private JLabel mathChannelStdDevLabel;
	private JLabel mathChannelFrequencyLabel;
	//Filter channel
	private JLabel filterChannelMinLabel;
	private JLabel filterChannelMaxLabel;
	private JLabel filterChannelMaxP2PLabel;
	private JLabel filterChannelAverageLabel;
	private JLabel filterChannelStdDevLabel;
	private JLabel filterChannelFrequencyLabel;


	public DigiscopeContainer() {
		initComponents();
	}

	private void initComponents() {
		frame = new JFrame(APPLICATION_NAME);
		frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
		frame.setResizable(false);

		frame.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		/*************************************************
		 * Add all the JLabels onto the GUI
		 *************************************************/

		JLabel digiscopeLabel = new JLabel("Digiscope");
		digiscopeLabel.setBounds(1012, 16, 192, 49);
		digiscopeLabel.setFont(new Font("Helvetica Neue", Font.PLAIN, 40));
		frame.getContentPane().add(digiscopeLabel);
		
		JLabel deviceIpAddressLabel = new JLabel("Device IP Address:");
		deviceIpAddressLabel.setBounds(1012, 74, 126, 24);
		frame.getContentPane().add(deviceIpAddressLabel);
		
		JLabel portNumberLabel = new JLabel("Port Number:");
		portNumberLabel.setBounds(1195, 77, 87, 16);
		frame.getContentPane().add(portNumberLabel);
		
		JLabel channelsToPlotLabel = new JLabel("Channels To Plot:");
		channelsToPlotLabel.setBounds(1012, 132, 126, 16);
		frame.getContentPane().add(channelsToPlotLabel);
		
		JLabel minVoltageLabel = new JLabel("Min. Voltage");
		minVoltageLabel.setBounds(108, 662, 61, 16);
		minVoltageLabel.setFont(new Font("Helvetica Neue", Font.PLAIN, 10));
		frame.getContentPane().add(minVoltageLabel);
		
		JLabel maxVoltageLabel = new JLabel("Max Voltage");
		maxVoltageLabel.setBounds(194, 662, 71, 16);
		maxVoltageLabel.setFont(new Font("Helvetica Neue", Font.PLAIN, 10));
		frame.getContentPane().add(maxVoltageLabel);
		
		JLabel maxPeakToPeakVoltageLabel = new JLabel("Max P2P voltage");
		maxPeakToPeakVoltageLabel.setBounds(293, 662, 87, 16);
		maxPeakToPeakVoltageLabel.setFont(new Font("Helvetica Neue", Font.PLAIN, 10));
		frame.getContentPane().add(maxPeakToPeakVoltageLabel);
		
		JLabel averageVoltageLabel = new JLabel("Ave Voltage");
		averageVoltageLabel.setBounds(413, 662, 61, 16);
		averageVoltageLabel.setFont(new Font("Helvetica Neue", Font.PLAIN, 10));
		frame.getContentPane().add(averageVoltageLabel);
		
		JLabel stdDeviationLabel = new JLabel("Voltage Std. Deviation");
		stdDeviationLabel.setBounds(520, 662, 113, 16);
		stdDeviationLabel.setFont(new Font("Helvetica Neue", Font.PLAIN, 10));
		frame.getContentPane().add(stdDeviationLabel);
		
		JLabel frequencyLabel = new JLabel("Frequency");
		frequencyLabel.setBounds(658, 662, 52, 16);
		frequencyLabel.setFont(new Font("Helvetica Neue", Font.PLAIN, 10));
		frame.getContentPane().add(frequencyLabel);
		
		JLabel channelALabel = new JLabel("Ch A");
		channelALabel.setBounds(6, 690, 37, 16);
		frame.getContentPane().add(channelALabel);
		
		JLabel channelBLabel = new JLabel("Ch B");
		channelBLabel.setBounds(6, 715, 37, 16);
		frame.getContentPane().add(channelBLabel);
		
		JLabel mathChannelLabel = new JLabel("Math Ch");
		mathChannelLabel.setBounds(6, 740, 61, 16);
		frame.getContentPane().add(mathChannelLabel);
		
		JLabel filterChannelLabel = new JLabel("Filter Ch");
		filterChannelLabel.setBounds(6, 765, 61, 16);
		frame.getContentPane().add(filterChannelLabel);
		
		// Display details: Channel A
		channelAMinLabel = new JLabel("");
		channelAMinLabel.setFont(new Font("Helvetica Neue", Font.PLAIN, 10));
		channelAMinLabel.setBounds(108, 688, 61, 16);
		frame.getContentPane().add(channelAMinLabel);
		
		channelAMaxLabel = new JLabel("");
		channelAMaxLabel.setFont(new Font("Helvetica Neue", Font.PLAIN, 10));
		channelAMaxLabel.setBounds(194, 688, 61, 16);
		frame.getContentPane().add(channelAMaxLabel);
		
		channelAMaxP2PLabel = new JLabel("");
		channelAMaxP2PLabel.setFont(new Font("Helvetica Neue", Font.PLAIN, 10));
		channelAMaxP2PLabel.setBounds(293, 688, 61, 16);
		frame.getContentPane().add(channelAMaxP2PLabel);
		
		channelAAverageLabel = new JLabel("");
		channelAAverageLabel.setFont(new Font("Helvetica Neue", Font.PLAIN, 10));
		channelAAverageLabel.setBounds(413, 688, 61, 16);
		frame.getContentPane().add(channelAAverageLabel);
		
		channelAStdDevLabel = new JLabel("");
		channelAStdDevLabel.setFont(new Font("Helvetica Neue", Font.PLAIN, 10));
		channelAStdDevLabel.setBounds(520, 688, 61, 16);
		frame.getContentPane().add(channelAStdDevLabel);
		
		channelAFrequencyLabel = new JLabel("");
		channelAFrequencyLabel.setFont(new Font("Helvetica Neue", Font.PLAIN, 10));
		channelAFrequencyLabel.setBounds(656, 688, 61, 16);
		frame.getContentPane().add(channelAFrequencyLabel);
		
		// Display details: Channel B
		channelBMinLabel = new JLabel("");
		channelBMinLabel.setFont(new Font("Helvetica Neue", Font.PLAIN, 10));
		channelBMinLabel.setBounds(108, 715, 61, 16);
		frame.getContentPane().add(channelBMinLabel);

		channelBMaxLabel = new JLabel("");
		channelBMaxLabel.setFont(new Font("Helvetica Neue", Font.PLAIN, 10));
		channelBMaxLabel.setBounds(194, 715, 61, 16);
		frame.getContentPane().add(channelBMaxLabel);

		channelBMaxP2PLabel = new JLabel("");
		channelBMaxP2PLabel.setFont(new Font("Helvetica Neue", Font.PLAIN, 10));
		channelBMaxP2PLabel.setBounds(293, 715, 61, 16);
		frame.getContentPane().add(channelBMaxP2PLabel);

		channelBAverageLabel = new JLabel("");
		channelBAverageLabel.setFont(new Font("Helvetica Neue", Font.PLAIN, 10));
		channelBAverageLabel.setBounds(413, 715, 61, 16);
		frame.getContentPane().add(channelBAverageLabel);

		channelBStdDevLabel = new JLabel("");
		channelBStdDevLabel.setFont(new Font("Helvetica Neue", Font.PLAIN, 10));
		channelBStdDevLabel.setBounds(520, 715, 61, 16);
		frame.getContentPane().add(channelBStdDevLabel);

		channelBFrequencyLabel = new JLabel("");
		channelBFrequencyLabel.setFont(new Font("Helvetica Neue", Font.PLAIN, 10));
		channelBFrequencyLabel.setBounds(656, 715, 61, 16);
		frame.getContentPane().add(channelBFrequencyLabel);
		
		// Display details: Math Channel
		mathChannelMinLabel = new JLabel("");
		mathChannelMinLabel.setFont(new Font("Helvetica Neue", Font.PLAIN, 10));
		mathChannelMinLabel.setBounds(108, 738, 61, 16);
		frame.getContentPane().add(mathChannelMinLabel);

		mathChannelMaxLabel = new JLabel("");
		mathChannelMaxLabel.setFont(new Font("Helvetica Neue", Font.PLAIN, 10));
		mathChannelMaxLabel.setBounds(194, 738, 61, 16);
		frame.getContentPane().add(mathChannelMaxLabel);

		mathChannelMaxP2PLabel = new JLabel("");
		mathChannelMaxP2PLabel.setFont(new Font("Helvetica Neue", Font.PLAIN, 10));
		mathChannelMaxP2PLabel.setBounds(293, 738, 61, 16);
		frame.getContentPane().add(mathChannelMaxP2PLabel);

		mathChannelAverageLabel = new JLabel("");
		mathChannelAverageLabel.setFont(new Font("Helvetica Neue", Font.PLAIN, 10));
		mathChannelAverageLabel.setBounds(413, 738, 61, 16);
		frame.getContentPane().add(mathChannelAverageLabel);

		mathChannelStdDevLabel = new JLabel("");
		mathChannelStdDevLabel.setFont(new Font("Helvetica Neue", Font.PLAIN, 10));
		mathChannelStdDevLabel.setBounds(520, 738, 61, 16);
		frame.getContentPane().add(mathChannelStdDevLabel);

		mathChannelFrequencyLabel = new JLabel("");
		mathChannelFrequencyLabel.setFont(new Font("Helvetica Neue", Font.PLAIN, 10));
		mathChannelFrequencyLabel.setBounds(656, 738, 61, 16);
		frame.getContentPane().add(mathChannelFrequencyLabel);
		
		// Display details: Filter Channel
		filterChannelMinLabel = new JLabel("");
		filterChannelMinLabel.setFont(new Font("Helvetica Neue", Font.PLAIN, 10));
		filterChannelMinLabel.setBounds(108, 765, 61, 16);
		frame.getContentPane().add(filterChannelMinLabel);

		filterChannelMaxLabel = new JLabel("");
		filterChannelMaxLabel.setFont(new Font("Helvetica Neue", Font.PLAIN, 10));
		filterChannelMaxLabel.setBounds(194, 765, 61, 16);
		frame.getContentPane().add(filterChannelMaxLabel);

		filterChannelMaxP2PLabel = new JLabel("");
		filterChannelMaxP2PLabel.setFont(new Font("Helvetica Neue", Font.PLAIN, 10));
		filterChannelMaxP2PLabel.setBounds(293, 765, 61, 16);
		frame.getContentPane().add(filterChannelMaxP2PLabel);

		filterChannelAverageLabel = new JLabel("");
		filterChannelAverageLabel.setFont(new Font("Helvetica Neue", Font.PLAIN, 10));
		filterChannelAverageLabel.setBounds(413, 765, 61, 16);
		frame.getContentPane().add(filterChannelAverageLabel);

		filterChannelStdDevLabel = new JLabel("");
		filterChannelStdDevLabel.setFont(new Font("Helvetica Neue", Font.PLAIN, 10));
		filterChannelStdDevLabel.setBounds(520, 765, 61, 16);
		frame.getContentPane().add(filterChannelStdDevLabel);

		filterChannelFrequencyLabel = new JLabel("");
		filterChannelFrequencyLabel.setFont(new Font("Helvetica Neue", Font.PLAIN, 10));
		filterChannelFrequencyLabel.setBounds(656, 765, 61, 16);
		frame.getContentPane().add(filterChannelFrequencyLabel);

		JLabel deviceStatusLabel = new JLabel("Device Status:");
		deviceStatusLabel.setBounds(1270, 6, 93, 16);
		frame.getContentPane().add(deviceStatusLabel);
		
		statusLabel = new JLabel("");
		statusLabel.setBounds(1368, 6, 61, 16);
		frame.getContentPane().add(statusLabel);
		
		JLabel verticalRange = new JLabel("Volts Per Division:");
		verticalRange.setBounds(1160, 185, 122, 16);
		frame.getContentPane().add(verticalRange);
		
		JLabel horizontalRangeLabel = new JLabel("Time Per Division:");
		horizontalRangeLabel.setBounds(1296, 185, 117, 16);
		frame.getContentPane().add(horizontalRangeLabel);
		
		JLabel connectionStatusLabel = new JLabel("Connected:");
		connectionStatusLabel.setBounds(1270, 28, 133, 16);
		frame.getContentPane().add(connectionStatusLabel);
		
		connectionStatusInfoLabel = new JLabel("No");
		connectionStatusInfoLabel.setBounds(1368, 28, 61, 16);
		frame.getContentPane().add(connectionStatusInfoLabel);
		
		JLabel triggerModeLabel = new JLabel("Trigger Mode:");
		triggerModeLabel.setBounds(1012, 242, 87, 16);
		frame.getContentPane().add(triggerModeLabel);
		
		JLabel triggerTypeLabel = new JLabel("Trigger Type:");
		triggerTypeLabel.setBounds(1160, 242, 87, 16);
		frame.getContentPane().add(triggerTypeLabel);
		
		JLabel triggerThresholdLabel = new JLabel("Trigger Threshold:");
		triggerThresholdLabel.setBounds(1306, 242, 123, 16);
		frame.getContentPane().add(triggerThresholdLabel);
		
		JLabel samplingModeLabel = new JLabel("Sampling Mode:");
		samplingModeLabel.setBounds(1012, 299, 121, 16);
		frame.getContentPane().add(samplingModeLabel);
		
		JLabel numberOfSamplesLabel = new JLabel("Num Samples to Acquire:");
		numberOfSamplesLabel.setBounds(1275, 299, 165, 16);
		frame.getContentPane().add(numberOfSamplesLabel);

		JLabel channelCouplingLabel = new JLabel("Channel Coupling:");
		channelCouplingLabel.setBounds(1012, 185, 117, 16);
		frame.getContentPane().add(channelCouplingLabel);
		
		JLabel lblChannelToTrigger = new JLabel("Channel To Trigger:");
		lblChannelToTrigger.setBounds(1130, 299, 133, 16);
		frame.getContentPane().add(lblChannelToTrigger);
		
		JLabel channelOffsetConfigLabel = new JLabel("Channel Offset: ");
		channelOffsetConfigLabel.setBounds(1012, 356, 106, 16);
		frame.getContentPane().add(channelOffsetConfigLabel);
		
		JLabel bandpassSamplingLabel = new JLabel("Bandpass Sampling?");
		bandpassSamplingLabel.setBounds(1160, 356, 133, 16);
		frame.getContentPane().add(bandpassSamplingLabel);
		
		JLabel changeTimeResolution = new JLabel("Change Time Display Resolution:");
		changeTimeResolution.setFont(new Font("Helvetica Neue", Font.PLAIN, 10));
		changeTimeResolution.setBounds(733, 716, 160, 16);
		frame.getContentPane().add(changeTimeResolution);

		JLabel changeVoltageResolution = new JLabel("Change Voltage Display Resolution:");
		changeVoltageResolution.setFont(new Font("Helvetica Neue", Font.PLAIN, 10));
		changeVoltageResolution.setBounds(733, 697, 160, 16);
		frame.getContentPane().add(changeVoltageResolution);

		JLabel currentSamplingRateLabel = new JLabel("Current Sampling Rate:");
		currentSamplingRateLabel.setFont(new Font("Helvetica Neue", Font.PLAIN, 10));
		currentSamplingRateLabel.setBounds(733, 735, 117, 16);
		frame.getContentPane().add(currentSamplingRateLabel);

		JLabel currentVoltageLabel = new JLabel("Selected Pt. Voltage:");
		currentVoltageLabel.setFont(new Font("Helvetica Neue", Font.PLAIN, 10));
		currentVoltageLabel.setBounds(733, 754, 133, 16);
		frame.getContentPane().add(currentVoltageLabel);

		/*************************************************
		 * Textboxes
		 *************************************************/
		
		deviceIpTextField = new JTextField();
		deviceIpTextField.setBounds(1012, 97, 173, 28);
		frame.getContentPane().add(deviceIpTextField);
		deviceIpTextField.setColumns(10);
		
		portNumberTextField = new JTextField();
		portNumberTextField.setBounds(1195, 97, 93, 28);
		frame.getContentPane().add(portNumberTextField);
		portNumberTextField.setColumns(10);
		
		triggerThresholdTextField = new JTextField();
		triggerThresholdTextField.setBounds(1305, 258, 113, 28);
		frame.getContentPane().add(triggerThresholdTextField);
		triggerThresholdTextField.setColumns(10);
		
		numberOfSamplesToAcquireTextField = new JTextField();
		numberOfSamplesToAcquireTextField.setBounds(1270, 315, 134, 28);
		frame.getContentPane().add(numberOfSamplesToAcquireTextField);
		numberOfSamplesToAcquireTextField.setColumns(10);
		
		/*************************************************
		 * Miscellaneous Components
		 *************************************************/
		
		oscilloscopePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		oscilloscopePanel.setBounds(0, 0, 1000, 650);
		frame.getContentPane().add(oscilloscopePanel);
		
		JSeparator verticalSeparator = new JSeparator();
    	verticalSeparator.setOrientation(SwingConstants.VERTICAL);
    	verticalSeparator.setBounds(995, 662, 12, 119);
    	frame.getContentPane().add(verticalSeparator);
    	
		JSeparator dataVerticalSeparator = new JSeparator();
		dataVerticalSeparator.setOrientation(SwingConstants.VERTICAL);
		dataVerticalSeparator.setBounds(722, 662, 12, 117);
		frame.getContentPane().add(dataVerticalSeparator);
		
		/*************************************************
		 * Buttons
		 *************************************************/
		
		connectButton = new JButton("Connect");
		connectButton.addActionListener(event -> this.emit("connect"));
		connectButton.setBounds(1312, 98, 117, 29);
		frame.getContentPane().add(connectButton);
		
		/*************************************************
		 * Checkboxes
		 *************************************************/
		
		channelACheckBox = new JCheckBox("Channel A");
		channelACheckBox.setBounds(1005, 150, 113, 23);
		channelACheckBox.addActionListener(event ->
				this.emit("channelACheckBoxChecked"));
		frame.getContentPane().add(channelACheckBox);
		
		channelBCheckBox = new JCheckBox("Channel B");
		channelBCheckBox.setBounds(1124, 150, 95, 23);
		channelBCheckBox.addActionListener(event ->
				this.emit("channelBCheckBoxChecked"));
		frame.getContentPane().add(channelBCheckBox);
		
		mathChannelCheckBox = new JCheckBox("Math Channel");
		mathChannelCheckBox.setBounds(1221, 150, 102, 23);
		mathChannelCheckBox.addActionListener(event ->
				this.emit("mathChannelCheckBoxChecked"));
		frame.getContentPane().add(mathChannelCheckBox);
		
		filterChannelCheckBox = new JCheckBox("Filter Channel");
		filterChannelCheckBox.setBounds(1327, 150, 102, 23);
		filterChannelCheckBox.addActionListener(event ->
				this.emit("filterChannelCheckBoxChecked"));
		frame.getContentPane().add(filterChannelCheckBox);

		
		/*************************************************
		 * Comboboxes
		 *************************************************/
		
		channelCouplingComboBox = new JComboBox<String>();
		channelCouplingComboBox.setBounds(1005, 203, 94, 27);
		channelCouplingComboBox.addItem("AC");
		channelCouplingComboBox.addItem("DC");
		channelCouplingComboBox.setSelectedIndex(-1);
		frame.getContentPane().add(channelCouplingComboBox);
		
		verticalRangeComboBox = new JComboBox<String>();
		verticalRangeComboBox.setBounds(1155, 203, 102, 27);
		verticalRangeComboBox.addItem("20mV");
		verticalRangeComboBox.addItem("50mV");
		verticalRangeComboBox.addItem("100mV");
		verticalRangeComboBox.addItem("200mV");
		verticalRangeComboBox.addItem("500mV");
		verticalRangeComboBox.addItem("1V");
		verticalRangeComboBox.addItem("2V");
		verticalRangeComboBox.setSelectedIndex(-1);
		frame.getContentPane().add(verticalRangeComboBox);

		char c = '\u00B5';
		
		horizontalRangeComboBox = new JComboBox<String>();
		horizontalRangeComboBox.setBounds(1296, 203, 93, 27);
		horizontalRangeComboBox.addItem("1" + c + "s");
		horizontalRangeComboBox.addItem("2" + c + "s");
		horizontalRangeComboBox.addItem("5" + c + "s");
		horizontalRangeComboBox.addItem("10" + c + "s");
		horizontalRangeComboBox.addItem("20" + c + "s");
		horizontalRangeComboBox.addItem("50" + c + "s");
		horizontalRangeComboBox.addItem("100" + c + "s");
		horizontalRangeComboBox.addItem("200" + c + "s");
		horizontalRangeComboBox.addItem("500" + c + "s");
		horizontalRangeComboBox.addItem("1ms");
		horizontalRangeComboBox.addItem("2ms");
		horizontalRangeComboBox.addItem("5ms");
		horizontalRangeComboBox.addItem("10ms");
		horizontalRangeComboBox.addItem("20ms");
		horizontalRangeComboBox.addItem("50ms");
		horizontalRangeComboBox.addItem("100ms");
		horizontalRangeComboBox.addItem("200ms");
		horizontalRangeComboBox.addItem("500ms");
		horizontalRangeComboBox.addItem("1s");
		horizontalRangeComboBox.setSelectedIndex(-1);
		frame.getContentPane().add(horizontalRangeComboBox);
		
		triggerModeComboBox = new JComboBox<String>();
		triggerModeComboBox.setBounds(1005, 260, 131, 27);
		triggerModeComboBox.addItem("Auto");
		triggerModeComboBox.addItem("Normal");
		triggerModeComboBox.addItem("Single");
		triggerModeComboBox.setSelectedIndex(-1);
		frame.getContentPane().add(triggerModeComboBox);
		
		triggerTypeComboBox = new JComboBox<String>();
		triggerTypeComboBox.setBounds(1151, 260, 131, 27);
		triggerTypeComboBox.addItem("Rising");
		triggerTypeComboBox.addItem("Falling");
		triggerTypeComboBox.addItem("Level");
		triggerTypeComboBox.setSelectedIndex(-1);
		frame.getContentPane().add(triggerTypeComboBox);
		
		
		bandpassSamplingComboBox = new JComboBox<String>();
		bandpassSamplingComboBox.setBounds(1160, 378, 93, 27);
		bandpassSamplingComboBox.addItem("On");
		bandpassSamplingComboBox.addItem("Off");
		bandpassSamplingComboBox.setSelectedIndex(-1);
		frame.getContentPane().add(bandpassSamplingComboBox);
		
		/*************************************************
		 * Buttons
		 *************************************************/
		
		JButton sendConfigToFirmwareButton = new JButton(
				"Send Configurations to Firmware");
		sendConfigToFirmwareButton.setBounds(1090, 427, 250, 29);
		sendConfigToFirmwareButton.addActionListener(event -> 
				this.emit("sendConfigurationsToFirmware"));		
		frame.getContentPane().add(sendConfigToFirmwareButton);
		
		samplingModeComboBox = new JComboBox<String>();
		samplingModeComboBox.addItem("8 bit");
		samplingModeComboBox.addItem("12 bit");
		samplingModeComboBox.setSelectedIndex(-1);
		samplingModeComboBox.setBounds(1005, 317, 94, 27);
		frame.getContentPane().add(samplingModeComboBox);
		
		channelToTriggerComboBox = new JComboBox<String>();
		channelToTriggerComboBox.setBounds(1150, 317, 80, 27);
		channelToTriggerComboBox.addItem("A");
		channelToTriggerComboBox.addItem("B");
		channelToTriggerComboBox.setSelectedIndex(-1);
		frame.getContentPane().add(channelToTriggerComboBox);
		
		
		/*************************************************
		 * Math Channel
		 *************************************************/
		
		JSeparator separator = new JSeparator();
		separator.setOrientation(SwingConstants.HORIZONTAL);
		separator.setBounds(1046, 486, 367, 12);
		frame.getContentPane().add(separator);
		
		JLabel mathChannelLabel2 = new JLabel("Math Channel:");
		mathChannelLabel2.setBounds(1012, 544, 94, 16);
		frame.getContentPane().add(mathChannelLabel2);
		
		JLabel inputEquationChannelLabel = new JLabel("Input Equation:");
		inputEquationChannelLabel.setBounds(1134, 510, 129, 16);
		frame.getContentPane().add(inputEquationChannelLabel);
		
		mathChannelEquationTextField = new JTextField();
		mathChannelEquationTextField.setBounds(1118, 538, 192, 28);
		frame.getContentPane().add(mathChannelEquationTextField);
		mathChannelEquationTextField.setColumns(10);
		
		JButton setMathEquationButton = new JButton("Set Math Eq.");
		setMathEquationButton.setBounds(1327, 539, 117, 29);
		setMathEquationButton.addActionListener(event -> 
				this.emit("setMathEquation"));
		frame.getContentPane().add(setMathEquationButton);
		
		/*************************************************
		 * Filter Channel
		 *************************************************/

		JLabel filterChannelLabel2 = new JLabel("Filter Channel:");
		filterChannelLabel2.setBounds(1012, 584, 94, 16);
		frame.getContentPane().add(filterChannelLabel2);
		
		JLabel filterInputChannelLabel = new JLabel("Input Channel:");
		filterInputChannelLabel.setBounds(1120, 584, 95, 16);
		frame.getContentPane().add(filterInputChannelLabel);
		
		JLabel filterChannelInputFileLabel = new JLabel("Input File:");
		filterChannelInputFileLabel.setBounds(1238, 584, 129, 16);
		frame.getContentPane().add(filterChannelInputFileLabel);

		filterInputChannelComboBoxModel = new DefaultComboBoxModel<>();
		JComboBox<String> filterInputChannelComboBox = 
				new JComboBox<>(filterInputChannelComboBoxModel);
		filterInputChannelComboBoxModel.addElement("A");
		filterInputChannelComboBoxModel.addElement("B");
		filterInputChannelComboBox.setSelectedIndex(-1);
		filterInputChannelComboBox.addActionListener(event -> 
				this.emit("filterInputChannelChanged"));	
		filterInputChannelComboBox.setBounds(1124, 606, 93, 27);
		frame.getContentPane().add(filterInputChannelComboBox);
		
		JLabel channelTypeLabel = new JLabel("Channel Type:");
		channelTypeLabel.setBounds(1343, 584, 97, 16);
		frame.getContentPane().add(channelTypeLabel);
		
		JButton loadFilterFileButton = new JButton("Load");
		loadFilterFileButton.setBounds(1238, 605, 85, 29);
		loadFilterFileButton.addActionListener(event -> 
				this.emit("parseFilterChannelInputFile"));		
		frame.getContentPane().add(loadFilterFileButton);
		
		filterTypeInfoLabel = new JLabel("");
		filterTypeInfoLabel.setBounds(1327, 612, 97, 16);
		frame.getContentPane().add(filterTypeInfoLabel);
		
		/*************************************************
		 * User Interaction with
		 *************************************************/
		
		forceTriggerButton = new JButton("Force Trigger");
		forceTriggerButton.setBounds(737, 656, 117, 29);
		forceTriggerButton.addActionListener(event -> 
				this.emit("forceTriggerButtonPressed"));	
		forceTriggerButton.setEnabled(false);
		frame.getContentPane().add(forceTriggerButton);
		
		rearmTriggerButton = new JButton("Rearm Trigger");
		rearmTriggerButton.setBounds(866, 656, 117, 29);
		rearmTriggerButton.setEnabled(false);
		rearmTriggerButton.addActionListener(event -> 
				this.emit("rearmTriggerButtonPressed"));	
		frame.getContentPane().add(rearmTriggerButton);
		
		currentSamplingRateInfoLabel = new JLabel("");
		currentSamplingRateInfoLabel.setFont(new Font("Helvetica Neue", Font.PLAIN, 10));
		currentSamplingRateInfoLabel.setBounds(900, 735, 85, 16);
		frame.getContentPane().add(currentSamplingRateInfoLabel);
		
		currentSelectedVoltageLabel = new JLabel("");
		currentSelectedVoltageLabel.setFont(new Font("Helvetica Neue", Font.PLAIN, 10));
		currentSelectedVoltageLabel.setBounds(900, 754, 85, 16);
		frame.getContentPane().add(currentSelectedVoltageLabel);

		verticalRangeDisplayComboBoxModel = new DefaultComboBoxModel<>();
		verticalRangeDisplayComboBox =
				new JComboBox<>(verticalRangeDisplayComboBoxModel);
		verticalRangeDisplayComboBoxModel.addElement("20mV");
		verticalRangeDisplayComboBoxModel.addElement("50mV");
		verticalRangeDisplayComboBoxModel.addElement("100mV");
		verticalRangeDisplayComboBoxModel.addElement("200mV");
		verticalRangeDisplayComboBoxModel.addElement("500mV");
		verticalRangeDisplayComboBoxModel.addElement("1V");
		verticalRangeDisplayComboBoxModel.addElement("2V");
		verticalRangeDisplayComboBox.setBounds(895, 686, 102, 27);
		verticalRangeDisplayComboBox.setSelectedIndex(-1);
		verticalRangeDisplayComboBox.addActionListener(event -> 
				this.emit("verticalRangeResolutionChanged"));

		frame.getContentPane().add(verticalRangeDisplayComboBox);

		horizontalRangeDisplayComboBoxModel = new DefaultComboBoxModel<>();
		horizontalRangeDisplayComboBox =
				new JComboBox<String>(horizontalRangeDisplayComboBoxModel);
		horizontalRangeDisplayComboBoxModel.addElement("1" + c + "s");
		horizontalRangeDisplayComboBoxModel.addElement("2" + c + "s");
		horizontalRangeDisplayComboBoxModel.addElement("5" + c + "s");
		horizontalRangeDisplayComboBoxModel.addElement("10" + c + "s");
		horizontalRangeDisplayComboBoxModel.addElement("20" + c + "s");
		horizontalRangeDisplayComboBoxModel.addElement("50" + c + "s");
		horizontalRangeDisplayComboBoxModel.addElement("100" + c + "s");
		horizontalRangeDisplayComboBoxModel.addElement("200" + c + "s");
		horizontalRangeDisplayComboBoxModel.addElement("500" + c + "s");
		horizontalRangeDisplayComboBoxModel.addElement("1ms");
		horizontalRangeDisplayComboBoxModel.addElement("2ms");
		horizontalRangeDisplayComboBoxModel.addElement("5ms");
		horizontalRangeDisplayComboBoxModel.addElement("10ms");
		horizontalRangeDisplayComboBoxModel.addElement("20ms");
		horizontalRangeDisplayComboBoxModel.addElement("50ms");
		horizontalRangeDisplayComboBoxModel.addElement("100ms");
		horizontalRangeDisplayComboBoxModel.addElement("200ms");
		horizontalRangeDisplayComboBoxModel.addElement("500ms");
		horizontalRangeDisplayComboBoxModel.addElement("1s");
		horizontalRangeDisplayComboBox.setBounds(895, 711, 102, 27);
		horizontalRangeDisplayComboBox.setSelectedIndex(-1);
		horizontalRangeDisplayComboBox.addActionListener(event -> 
				this.emit("horizontalRangeResolutionChanged"));
		frame.getContentPane().add(horizontalRangeDisplayComboBox);
		
		
		/*************************************************
		 * Function Generator
		 *************************************************/
		
		JLabel functionGeneratorLabel = new JLabel("Function Generator");
		functionGeneratorLabel.setBounds(1136, 660, 207, 16);
		functionGeneratorLabel.setFont(new Font("Helvetica Neue", Font.PLAIN, 20));
		frame.getContentPane().add(functionGeneratorLabel);
		
		JLabel functionGeneratorOutputLabel = new JLabel("Output:");
		functionGeneratorOutputLabel.setBounds(1005, 690, 61, 16);
		frame.getContentPane().add(functionGeneratorOutputLabel);
		
		JLabel functionGeneratorWaveTypeLabel = new JLabel("Wave Type:");
		functionGeneratorWaveTypeLabel.setBounds(1090, 690, 81, 16);
		frame.getContentPane().add(functionGeneratorWaveTypeLabel);
		
		JLabel functionGeneratorP2PVoltageLabel = new JLabel("P2P Voltage:");
		functionGeneratorP2PVoltageLabel.setBounds(1195, 690, 81, 16);
		frame.getContentPane().add(functionGeneratorP2PVoltageLabel);
		
		JLabel functionGeneratorOffsetLabel = new JLabel("Offset:");
		functionGeneratorOffsetLabel.setBounds(1291, 690, 52, 16);
		frame.getContentPane().add(functionGeneratorOffsetLabel);
		
		JLabel functionGeneratorFrequencyLabel = new JLabel("Frequency:");
		functionGeneratorFrequencyLabel.setBounds(1358, 690, 71, 16);
		frame.getContentPane().add(functionGeneratorFrequencyLabel);
		
		JButton sendFunctionGenConfigToFirmwareButton = new JButton(
				"Send Function Generator Configuration to Firmware");
		sendFunctionGenConfigToFirmwareButton.setBounds(1046, 740, 355, 29);
		sendFunctionGenConfigToFirmwareButton.addActionListener(event -> 
				this.emit("sendFunctionGeneratorConfig"));
		frame.getContentPane().add(sendFunctionGenConfigToFirmwareButton);
		
		functionGeneratorOutputComboBox = new JComboBox<String>();
		functionGeneratorOutputComboBox.setBounds(1000, 711, 81, 27);
		functionGeneratorOutputComboBox.addItem("On");
		functionGeneratorOutputComboBox.addItem("Off");
		functionGeneratorOutputComboBox.setSelectedIndex(-1);
		frame.getContentPane().add(functionGeneratorOutputComboBox);
		
		functionGeneratorWaveTypeComboBox = new JComboBox<String>();
		functionGeneratorWaveTypeComboBox.addItem("Sine");
		functionGeneratorWaveTypeComboBox.addItem("Square");
		functionGeneratorWaveTypeComboBox.addItem("Triangle");
		functionGeneratorWaveTypeComboBox.addItem("Ramp");
		functionGeneratorWaveTypeComboBox.addItem("Noise");
		functionGeneratorWaveTypeComboBox.setSelectedIndex(-1);
		functionGeneratorWaveTypeComboBox.setBounds(1080, 711, 110, 27);
		frame.getContentPane().add(functionGeneratorWaveTypeComboBox);
		
		functionGeneratorP2PTextField = new JTextField();
		functionGeneratorP2PTextField.setBounds(1195, 709, 71, 28);
		frame.getContentPane().add(functionGeneratorP2PTextField);
		functionGeneratorP2PTextField.setColumns(10);
		
		functionGeneratorOffsetTextField = new JTextField();
		functionGeneratorOffsetTextField.setColumns(10);
		functionGeneratorOffsetTextField.setBounds(1278, 709, 71, 28);
		frame.getContentPane().add(functionGeneratorOffsetTextField);
		
		functionGeneratorFrequencyTextField = new JTextField();
		functionGeneratorFrequencyTextField.setColumns(10);
		functionGeneratorFrequencyTextField.setBounds(1358, 709, 71, 28);
		frame.getContentPane().add(functionGeneratorFrequencyTextField);
		
		JSeparator separator_1 = new JSeparator();
		separator_1.setOrientation(SwingConstants.HORIZONTAL);
		separator_1.setBounds(1046, 648, 367, 12);
		frame.getContentPane().add(separator_1);		
		
		channelOffsetsTextField = new JTextField();
		channelOffsetsTextField.setBounds(1008, 377, 130, 26);
		frame.getContentPane().add(channelOffsetsTextField);
		channelOffsetsTextField.setColumns(10);
			
	}

	public void run() {
		frame.setVisible(true);
	}

	/*************************************************
	 *  Getter/setter methods
	 *************************************************/

	public JPanel getOscilloscopePanel() { return oscilloscopePanel; }

	/* Connection related stuff */
	public JTextField getPortNumberTextField() { return portNumberTextField; }
	
	public JTextField getDeviceIpTextField() {return deviceIpTextField; }
	
	public JButton getConnectButton() { return connectButton; }
	
	public void setConnectionStatusLabel(String deviceIsConnected) {
		this.connectionStatusInfoLabel.setText(deviceIsConnected);
	}
	
	public JLabel getDeviceStatusLabel() {
		return statusLabel;
	}

	/* Function Generator stuff */
	public JComboBox<String> getFunctionGeneratorOutputComboBox() {
		return functionGeneratorOutputComboBox;
	}

	public JComboBox<String> getFunctionGeneratorWaveTypeComboBox() {
		return functionGeneratorWaveTypeComboBox;
	}

	public JTextField getFunctionGeneratorP2PTextField() {
		return functionGeneratorP2PTextField;
	}

	public JTextField getFunctionGeneratorOffsetTextField() {
		return functionGeneratorOffsetTextField;
	}

	public JTextField getFunctionGeneratorFrequencyTextField() {
		return functionGeneratorFrequencyTextField;
	}

	/* User oscilloscope display interaction stuff */
	public void setForceTriggerButtonEnabled(Boolean enabled) {
		this.forceTriggerButton.setEnabled(enabled);
	}

	public void setRearmTriggerButtonEnabled(Boolean enabled) {
		this.rearmTriggerButton.setEnabled(enabled);
	}

	public void setCurrentSamplingRateLabel(String currentSamplingRate) {
		this.currentSamplingRateInfoLabel.setText(currentSamplingRate);
	}

	public void setCurrentSelectedVoltageLabel(String currentSelectedVoltage) {
		this.currentSelectedVoltageLabel.setText(currentSelectedVoltage);
	}
	
	/* Math channel */
	public JTextField getMathChannelEquationTextField() {
		return mathChannelEquationTextField;
	}
	
	/* Filter Channel */
	public DefaultComboBoxModel<String> getFilterInputChannelComboBoxModel() {
		return filterInputChannelComboBoxModel;
	}
	
	public void setFilterChannelLabel(String filterType) {
		this.filterTypeInfoLabel.setText(filterType);
	}
	
	/* Check boxes */
	public JCheckBox getChannelACheckBox() {
		return channelACheckBox;
	}

	public JCheckBox getChannelBCheckBox() {
		return channelBCheckBox;
	}
	public JCheckBox getMathChannelCheckBox() {
		return mathChannelCheckBox;
	}

	public JCheckBox getFilterChannelCheckBox() {
		return filterChannelCheckBox;
	}
	
	/* Firmware configuration stuff */
	public JComboBox<String> getChannelCouplingComboBox() {
		return channelCouplingComboBox;
	}

	public JComboBox<String> getVoltsPerDivComboBox() {
		return verticalRangeComboBox;
	}

	public JComboBox<String> getTimePerDivComboBox() {
		return horizontalRangeComboBox;
	}

	public JComboBox<String> getTriggerModeComboBox() {
		return triggerModeComboBox;
	}

	public JComboBox<String> getTriggerTypeComboBox() {
		return triggerTypeComboBox;
	}

	public JTextField getTriggerThresholdTextBox() {
		return triggerThresholdTextField;
	}

	public JComboBox<String> getSamplingModeComboBox() {
		return samplingModeComboBox;
	}
	
	public JComboBox<String> getChannelToTriggerComboBox() {
		return channelToTriggerComboBox;
	}

	public JTextField getNumberOfSamplesToAcquireTextField() {
		return numberOfSamplesToAcquireTextField;
	}
	
	public JTextField getChannelOffsetsTextField() {
		return channelOffsetsTextField;
	}
	
	public JComboBox<String> getBandpassSamplingComboBox() {
		return bandpassSamplingComboBox;
	}
	

	/* User adjustable resolution */
	public DefaultComboBoxModel<String> getVerticalRangeDisplayComboBoxModel() {
		return verticalRangeDisplayComboBoxModel;
	}

	public JComboBox<String> getVerticalRangeDisplayedComboBox() {
		return verticalRangeDisplayComboBox;
	}

	public DefaultComboBoxModel<String> getHorizontalRangeDisplayComboBoxModel() {
		return horizontalRangeDisplayComboBoxModel;
	}

	public JComboBox<String> getHorizontalRangeDisplayedComboBox() {
		return horizontalRangeDisplayComboBox;
	}


	/* Display channel measurements on the oscilloscope. Couldn't think of a
	 * way to refactor this code to make it look nicer, as MVC would have to
	 * be broken - labels are individual and managed by the controller */
	public void setChannelAMeasurements(Double min, Double max, Double p2p,
			Double ave, Double stdDev, Double freq, boolean displayed,
			boolean verticallyOffTheScreen) {
		if (displayed) {
			DecimalFormat df = new DecimalFormat("#.###");
			
			String chanAMinLabelText = (min == Double.NEGATIVE_INFINITY)
					? "N/A" : df.format(min) +"V";
			channelAMinLabel.setText(chanAMinLabelText);
			
			String chanAMaxLabelText = (max == Double.NEGATIVE_INFINITY)
					? "N/A" : df.format(max) + "V";
			channelAMaxLabel.setText(chanAMaxLabelText);
			
			String chanAMaxP2PLabelText = (p2p == Double.NEGATIVE_INFINITY)
					? "N/A" : df.format(p2p) + "V";
			channelAMaxP2PLabel.setText(chanAMaxP2PLabelText);
			
			String chanAAverageLabelText = (ave == Double.NEGATIVE_INFINITY)
					? "N/A" : df.format(ave) + "V";
			channelAAverageLabel.setText(chanAAverageLabelText);
			
			String chanAStdDevLabelText = (stdDev == Double.NEGATIVE_INFINITY)
					? "N/A" : df.format(stdDev) + "V";
			channelAStdDevLabel.setText(chanAStdDevLabelText);
			
			if (verticallyOffTheScreen) {
				channelAFrequencyLabel.setText("N/A");
			} else {
				channelAFrequencyLabel.setText(df.format(freq) + "Hz");
			}
		
		} else {
			channelAMinLabel.setText("");
			channelAMaxLabel.setText("");
			channelAMaxP2PLabel.setText("");
			channelAAverageLabel.setText("");
			channelAStdDevLabel.setText("");
			channelAFrequencyLabel.setText("");
		}
	}
	
	public void setChannelBMeasurements(Double min, Double max, Double p2p,
			Double ave, Double stdDev, Double freq, boolean displayed,
			boolean verticallyOffTheScreen) {

		if (displayed) {
			DecimalFormat df = new DecimalFormat("#.###");
		
			String chanBMinLabelText = (min == Double.NEGATIVE_INFINITY)
					? "N/A" : df.format(min) +"V";
			channelBMinLabel.setText(chanBMinLabelText);
			
			String chanBMaxLabelText = (max == Double.NEGATIVE_INFINITY)
					? "N/A" : df.format(max) + "V";
			channelBMaxLabel.setText(chanBMaxLabelText);
			
			String chanBMaxP2PLabelText = (p2p == Double.NEGATIVE_INFINITY)
					? "N/A" : df.format(p2p) + "V";
			channelBMaxP2PLabel.setText(chanBMaxP2PLabelText);
			
			String chanBAverageLabelText = (ave == Double.NEGATIVE_INFINITY)
					? "N/A" : df.format(ave) + "V";
			channelBAverageLabel.setText(chanBAverageLabelText);
			
			String chanBStdDevLabelText = (stdDev == Double.NEGATIVE_INFINITY)
					? "N/A" : df.format(stdDev) + "V";
			channelBStdDevLabel.setText(chanBStdDevLabelText);
			
			if (verticallyOffTheScreen) {
				channelBFrequencyLabel.setText("N/A");
			} else {
				channelBFrequencyLabel.setText(df.format(freq) + "Hz");
			}
			
		} else {
			channelBMinLabel.setText("");
			channelBMaxLabel.setText("");
			channelBMaxP2PLabel.setText("");
			channelBAverageLabel.setText("");
			channelBStdDevLabel.setText("");
			channelBFrequencyLabel.setText("");
		}
	}
	
	public void setMathChannelMeasurements(Double min, Double max, Double p2p,
			Double ave, Double stdDev, Double freq, boolean displayed,
			boolean verticallyOffTheScreen) {

		if (displayed) {
			DecimalFormat df = new DecimalFormat("#.###");
			
			String mathChannelMinLabelText = (min == Double.NEGATIVE_INFINITY)
					? "N/A" : df.format(min) +"V";
			mathChannelMinLabel.setText(mathChannelMinLabelText);
			
			String mathChannelMaxLabelText = (max == Double.NEGATIVE_INFINITY)
					? "N/A" : df.format(max) + "V";
			mathChannelMaxLabel.setText(mathChannelMaxLabelText );
			
			String mathChannelMaxP2PLabelText = (p2p == Double.NEGATIVE_INFINITY)
					? "N/A" : df.format(p2p) + "V";
			mathChannelMaxP2PLabel.setText(mathChannelMaxP2PLabelText);
			
			String mathChannelAverageLabelText = (ave == Double.NEGATIVE_INFINITY)
					? "N/A" : df.format(ave) + "V";
			mathChannelAverageLabel.setText(mathChannelAverageLabelText);
			
			String mathChannelStdDevLabelText = (stdDev == Double.NEGATIVE_INFINITY)
					? "N/A" : df.format(stdDev) + "V";
			mathChannelStdDevLabel.setText(mathChannelStdDevLabelText);
			
			if (verticallyOffTheScreen) {
				mathChannelFrequencyLabel.setText("N/A");
			} else {
				mathChannelFrequencyLabel.setText(df.format(freq) + "Hz");
			}
			
		} else {
			mathChannelMinLabel.setText("");
			mathChannelMaxLabel.setText("");
			mathChannelMaxP2PLabel.setText("");
			mathChannelAverageLabel.setText("");
			mathChannelStdDevLabel.setText("");
			mathChannelFrequencyLabel.setText("");
		}
	}
	
	public void setFilterChannelMeasurements(Double min, Double max, Double p2p,
			Double ave, Double stdDev, Double freq, boolean displayed,
			boolean verticallyOffTheScreen) {
		if (displayed) {
			DecimalFormat df = new DecimalFormat("#.###");
			
			String filterChannelMinLabelText = (min == Double.NEGATIVE_INFINITY)
					? "N/A" : df.format(min) +"V";
			filterChannelMinLabel.setText(filterChannelMinLabelText);
			
			String filterChannelMaxLabelText = (max == Double.NEGATIVE_INFINITY)
					? "N/A" : df.format(max) + "V";
			filterChannelMaxLabel.setText(filterChannelMaxLabelText);
			
			String filterChannelMaxP2PLabelText = (p2p == Double.NEGATIVE_INFINITY)
					? "N/A" : df.format(p2p) + "V";
			filterChannelMaxP2PLabel.setText(filterChannelMaxP2PLabelText);
			
			String filterChannelAverageLabelText = (ave == Double.NEGATIVE_INFINITY)
					? "N/A" : df.format(ave) + "V";
			filterChannelAverageLabel.setText(filterChannelAverageLabelText);
			
			String filterChannelStdDevLabelText = (stdDev == Double.NEGATIVE_INFINITY)
					? "N/A" : df.format(stdDev) + "V";
			filterChannelStdDevLabel.setText(filterChannelStdDevLabelText);
			
			if (verticallyOffTheScreen) {
				filterChannelFrequencyLabel.setText("N/A");
			} else {
				filterChannelFrequencyLabel.setText(df.format(freq) + "Hz");
			}
			
		} else {
			filterChannelMinLabel.setText("");
			filterChannelMaxLabel.setText("");
			filterChannelMaxP2PLabel.setText("");
			filterChannelAverageLabel.setText("");
			filterChannelStdDevLabel.setText("");
			filterChannelFrequencyLabel.setText("");
		}
	}

	/*************************************************
	 *  Helper/ auxillary methods
	 *************************************************/

	public void showMessageDialog(String message) {
		JOptionPane.showMessageDialog(null, message);
	}
}
