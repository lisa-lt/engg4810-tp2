import java.text.DecimalFormat;
import com.fathzer.soft.javaluator.DoubleEvaluator;

/**
 * The application's model using the MVC architecture
 * @author Lisa Liu-Thorrold
 *
 */
public class DigiscopeModel extends EventEmitter {

	// Core items
	private OscilloscopeDisplay display;
	private DigiscopeServer comms;

	// Digiscope specific stuff
	private String deviceStatus;
	private FunctionGenerator currentFunctionGeneratorConfigurations;
	private String triggerMode;
	private String triggerType;
	private String channelCoupling;
	private String voltsPerDivision;
	private String timePerDivision;
	private String timePerDivisionDisplayed;
	private String voltsPerDivisionDisplayed;
	private String triggerThreshold;
	private String samplingMode;
	private char channelToTrigger;
	private int numSamplesToAcquire;
	private String bandpassSampling;
	
	// Connectivity Related
	private String ipAddress;
	private int portNumber;
	private Boolean deviceIsConnected;
	private Boolean firstConfigurationsSent;

	// The channels
	private ChannelA ChannelA;
	private ChannelB ChannelB;
	private MathChannel MathChannel;
	private FilterChannel FilterChannel;

	// Plotting related
	private Boolean plotChannelA;
	private Boolean plotChannelB;
	private Boolean plotMathChannel;
	private Boolean plotFilterChannel;

	// Misc
	private int samplingRate;
	private String filterChannelInput;
	private double selectedSampleVoltage;
	private boolean initialConfigSent;


	public DigiscopeModel() {
		comms = new DigiscopeServer(this);
		samplingRate = 100000;
		voltsPerDivisionDisplayed = "";
		timePerDivisionDisplayed = "";
		currentFunctionGeneratorConfigurations = 
				new FunctionGenerator(false, "", 0.0,0.0,(short) 0);
	}

	public void setOscilloscopeDisplay(OscilloscopeDisplay display) {
		this.display = display;
		this.deviceIsConnected = false;

		// Create the channels
		ChannelA = new ChannelA();
		ChannelB = new ChannelB();
		MathChannel = new MathChannel();
		FilterChannel = new FilterChannel();

		timePerDivision = "";
		voltsPerDivision = "";

		plotChannelA = false;
		plotChannelB = false;
		plotMathChannel = false;
		plotFilterChannel = false;
	}

	/*************************************************
	 *  Channel Methods
	 *************************************************/

	/**
	 * This method determines samples from which channel are used for the 
	 * filter calculations.
	 */
	public void computeFilterChannel() {
		String filterType = FilterChannel.getFilterType();
		double[] samples = null;

		//need to get the input channel to get the sample
		switch (filterChannelInput) {
		case "A":
			samples = ChannelA.getChannelSamples();
			break;
		case "B":
			samples = ChannelB.getChannelSamples();
			break;
		case "Math":
			samples = MathChannel.getChannelSamples();
			break;
		}

		// Perform the calculations based on the filter type
		switch (filterType) {
		case "FIR":
			computeFirFilterChannel(samples, FilterChannel.getFirFilter());
			break;
		case "IIR":
			Object[] temp = FilterChannel.getIirFilters();
			double[] iirFilter1 = (double[])temp[0];
			double[] iirFilter2 = (double[])temp[1];
			computeIirFilterChannel(samples, iirFilter1, iirFilter2);
			break;
		}
	}

	/**
	 * Computers the Filter Channel based on FIR filter using provided samples.
	 * Sets the samples for the FilterChannel object
	 * @param samples - The samples to use for filter channel calculation
	 * @param filter - The fir filter to apply
	 */
	private void computeFirFilterChannel(double[] samples, double[] filter ) {

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
		
		FilterChannel.setChannelSamples(filterChannelSamples, samplingRate);
	}

	/**
	 * Computers the Filter Channel based on IIR filter using provided samples.
	 * Sets the samples for the FilterChannel object. The iir filter is split
	 * into two double arrays (iirFilter1 is the first column in the file,
	 * iirFilter2 being the second column in the file).
	 * @param samples - The samples to use for filter channel calculation.
	 * @param iirFilter1 - The iir filter to apply
	 * @param iirFilter2 - The iir filter to apply
	 */
	private void computeIirFilterChannel(double[] samples, double[] iirFilter1, 
			double[] iirFilter2) {
		
		int size = samples.length;
		double[] filterChannelSamples = new double[size];
		
		for (int i=0; i<samples.length; i++) {

			double first = 0;
			double second = 0;

			for (int j=0; j<iirFilter2.length; j++) {
				if (i-j < 0) {
					first += 0;
				} else {
					first += iirFilter2[j] *  samples[i-j];
				}
			}
			
			for (int j=1; j<iirFilter1.length; j++) {
				if (i-j < 0) {
					second += 0;
				} else {
					second += iirFilter1[j] *  filterChannelSamples[i-j];
				}
			}

			filterChannelSamples[i] = (first - second) * (1/iirFilter1[0]);
			
		}
		
		FilterChannel.setChannelSamples(filterChannelSamples, samplingRate);
	}

	/**
	 * This method performs the relevant computation to generate math channel
	 * samples.
	 */
	public void computeMathChannel() {

		int size = ChannelA.getChannelSamples().length;
		String equation = MathChannel.getEquation();

		double[] mathChannelSamples = new double[size];

		// Need to loop that amount of times. If the expression contains A or B,
		// need to substitute it with the value from the corresponding array.
		for (int i=0; i<size; i++) {

			DecimalFormat df = new DecimalFormat("#.#################");      

			String channelASample = df.format(ChannelA.getChannelSamples()[i]);
			String channelBSample = df.format(ChannelB.getChannelSamples()[i]);

			String evalA = equation.replace("A", channelASample);
			String evalB = evalA.replace("B", channelBSample);

			// If equation contains F, then replace with F, otherwise leave as
			// is
			if (equation.contains("F")) {
				String filterChannelSample = df.format(
						FilterChannel.getChannelSamples()[i]);
				String evalFilter = evalB.replace("F", filterChannelSample);
				mathChannelSamples[i] =  new DoubleEvaluator().evaluate(evalFilter);
			} else {
				mathChannelSamples[i] = new DoubleEvaluator().evaluate(evalB);
			}


		}

		// Finally need to set the math channel
		MathChannel.setChannelSamples(mathChannelSamples, samplingRate);
	}
	

	/*************************************************
	 *  Private helper methods
	 *************************************************/
	
	/**
	 * Converts a string voltage unit to a standard voltage unit (1v) as a double
	 * @param voltageUnit - The string to convert
	 * @return the SI unit of the voltage
	 */
	private double getStandardVoltageUnit(String voltageUnit) {
		
		switch(voltageUnit) {
		case "20mV":
			return 0.02;
		case "50mV":
			return 0.05;
		case "100mV":
			return 0.1;
		case "200mV":
			return 0.2;
		case "500mV":
			return 0.5;
		case "1V":
			return 1;
		case "2V":
			return 2;
		}
		
		// should never reach here
		return 0;
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

	/*************************************************
	 *  Getter/setter methods
	 *************************************************/

	/* Connection related stuff */
	public Boolean isConnected() {
		return deviceIsConnected;
	}
	public void setPortNumber(int portNumber) {
		this.portNumber = portNumber;
	}
	public int getPortNumber() {
		return portNumber;
	}
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	public String getIpAddress() {
		return ipAddress;
	}
	public DigiscopeServer getDigiscopeServer() {
		return comms;
	}

	public void setConnected(Boolean deviceIsConnected) {
		this.deviceIsConnected = deviceIsConnected;
	}

	/* Function generator stuff */
	public void setCurrentFunctionGeneratorConfigurations(
			FunctionGenerator current) {
		this.currentFunctionGeneratorConfigurations = current;
	}

	public FunctionGenerator getCurrentFunctionGeneratorConfigurations() {
		return currentFunctionGeneratorConfigurations;
	}

	/* Channel stuff */
	public MathChannel getMathChannel() {
		return MathChannel;
	}

	public FilterChannel getFilterChannel() {
		return FilterChannel;
	}

	public ChannelA getChannelA() {
		return ChannelA;
	}

	public ChannelB getChannelB() {
		return ChannelB;
	}

	/* Display relate */
	public String getResolution() { 

		if (timePerDivisionDisplayed.isEmpty() ||
				voltsPerDivisionDisplayed.isEmpty()) {
			return "Current Resolution: ";
		}

		return "Current Resolution: " +
		timePerDivisionDisplayed + "/div, " + voltsPerDivisionDisplayed + "/div"; }

	public void setVoltsPerDivision(String voltsPerDivision) {
		this.voltsPerDivision = voltsPerDivision;
	}

	public void setTimePerDivision(String timePerDivision) {
		this.timePerDivision = timePerDivision;
	}
	
	public Double getVoltsPerDivisionInDouble() {
		return getStandardVoltageUnit(voltsPerDivision);
	}

	public String getVoltsPerDivisionStringValue() {
		return voltsPerDivision;
	}

	public Double getTimePerDivision() {
		return getStandardTimeUnit(timePerDivision);
	}

	public String getTimePerDivisionAsString() {
		return timePerDivision;
	}

	public void setVoltsPerDivisionDisplayed(String voltsPerDivisionDisplayed) {
		this.voltsPerDivisionDisplayed = voltsPerDivisionDisplayed;
	}

	public void setTimePerDivisionDisplayed(String timePerDivisionDisplayed) {
		this.timePerDivisionDisplayed = timePerDivisionDisplayed;
	}
	
	public double getTimePerDivisionDisplayed() {
		return getStandardTimeUnit(timePerDivisionDisplayed);
	}

	public String getTimePerDivisionDisplayedString() {
		return timePerDivisionDisplayed;
	}

	public double getVoltsPerDivisionDisplayed() {
		return getStandardVoltageUnit(voltsPerDivisionDisplayed);
	}

	public String getVoltsPerDivisionDisplayedString() {
		return voltsPerDivisionDisplayed;
	}

	/* Digiscope related */
	public String getTriggerMode() { return triggerMode; }
	public void setTriggerMode(String triggerMode) {
		this.triggerMode = triggerMode;
	}

	public void setPlotChannelA(Boolean plotChannelA) {
		this.plotChannelA = plotChannelA;
	}

	public void setPlotChannelB(Boolean plotChannelB) {
		this.plotChannelB = plotChannelB;
	}

	public void setPlotMathChannel(Boolean plotMathChannel) {
		this.plotMathChannel = plotMathChannel;
	}

	public void setPlotFilterChannel(Boolean plotFilterChannel) {
		this.plotFilterChannel = plotFilterChannel;
	}

	public void setSamplingMode(String samplingMode) {
		this.samplingMode = samplingMode;
	}

	public String getSamplingMode() {
		return samplingMode;
	}

	public void setNumSamplesToAcquire(int numSamplesToAcquire) {
		this.numSamplesToAcquire = numSamplesToAcquire;
	}

	public int getNumSamplesToAcquire() {
		return numSamplesToAcquire;
	}

	public Boolean getChannelAisPlotted() {
		return plotChannelA;
	}

	public Boolean getChannelBisPlotted() {
		return plotChannelB;
	}

	public Boolean getMathChannelIsPlotted() {
		return plotMathChannel;
	}

	public Boolean getFilterChannelIsPlotted() {
		return plotFilterChannel;
	}

	public void setChannelToTrigger(char channelToTrigger) {
		this.channelToTrigger = channelToTrigger;
	}

	public char getChannelToTrigger() {
		return channelToTrigger;
	}

	public void setFirstConfigurationsSent(Boolean firstConfigurationsSent) {
		this.firstConfigurationsSent = firstConfigurationsSent;
	}

	public Boolean getFirstConfigurationsSent() {
		return firstConfigurationsSent;
	}

	public OscilloscopeDisplay getOscilloscopeDisplay() {
		return display;
	}

	// To display on the ui so need to format it as a string
	public String getSamplingRateText() {
		if (samplingRate <= 0) {
			return "Sampling Rate: ";
		}

		return "Sampling Rate: " + samplingRate;
	}
	
	public int getSamplingRate() {
		return samplingRate;
	}

	public void setSamplingRate(int samplingRate) {
		this.samplingRate = samplingRate;
	}
	
	public void setSelectedSampleVoltage(double selectedSampleVoltage) {
		this.selectedSampleVoltage = selectedSampleVoltage;
		this.emit("sampleVoltageSelected");
	}
	
	public double getSelectedSampleVoltage() {
		return selectedSampleVoltage;
	}
	
	public void setFilterChannelInput(String filterChannelInput) {
		this.filterChannelInput = filterChannelInput;
	}
	
	public String getFilterChannelInput() {
		return filterChannelInput;
	}
	
	public void setInitialConfigSent(boolean initialConfigSent) {
		this.initialConfigSent = initialConfigSent;
	}
	
	public boolean getInitialConfigSent() {
		return initialConfigSent;
	}

	public String getChannelCoupling() {
		return channelCoupling;
	}

	public void setChannelCoupling(String channelCoupling) {
		this.channelCoupling = channelCoupling;
	}

	public String getTriggerThreshold() {
		return triggerThreshold;
	}

	public void setTriggerThreshold(String triggerThreshold) {
		this.triggerThreshold = triggerThreshold;
	}

	public String getTriggerType() {
		return triggerType;
	}

	public void setTriggerType(String triggerType) {
		this.triggerType = triggerType;
	}
	
	public void setDeviceStatus(String deviceStatus) {
		this.deviceStatus = deviceStatus;
	}
	
	public String getDeviceStatus() {
		return deviceStatus;
	}
	
	public void setBandpassSampling(String bandpassSampling) {
		this.bandpassSampling = bandpassSampling;
	}
	
	public String getBandpassSampling() {
		return bandpassSampling;
	}

}
