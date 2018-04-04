import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;

/**
 * This abstract class represents an oscilloscope channel. It implements 
 * common methods that all of it's subclasses will extend, and is extended
 * by Channel A, Channel B, Math Channel and Filter Channel
 * @author Lisa Liu-Thorrold
 *
 */
public abstract class OscilloscopeChannel {

	// Private instance variables
	private double minVoltage;
	private double maxVoltage;
	private double maxP2Pvoltage;
	private double averageVoltage;
	private double standardVoltageDeviation;
	private double frequency;
	private int[] graphLineColor;
	private double[] channelSamples;
	private boolean availableForPlotting;
	private double[] visibleChannelSamples;
	private boolean verticallyOffTheScreen;

	public OscilloscopeChannel() {
		this.verticallyOffTheScreen = false;
	}


	/*************************************************
	 * Getter methods
	 *************************************************/

	public double getMinVoltage() {
		return minVoltage;
	}

	public double getMaxVoltage() {
		return maxVoltage;
	}

	public double getMaxP2Pvoltage() {
		return maxP2Pvoltage;
	}

	public double getAverageVoltage() {
		return averageVoltage;
	}

	public double getFrequency() {
		return frequency;
	}

	public double[] getChannelSamples() {
		return channelSamples;
	}
	
	public double[] getVisibleChannelSamples() {
		return visibleChannelSamples;
	}

	public boolean getVerticallyOffTheScreen() {
		return verticallyOffTheScreen;
	}


	public double getStandardVoltageDeviation() {
		return standardVoltageDeviation;
	}

	public boolean getAvailableForPlotting() {
		return availableForPlotting;
	}

	public int[] getGraphLineColor() { return graphLineColor; }

	/*************************************************
	 * Setter methods methods
	 *************************************************/

	public void setAvailableForPlotting(boolean availableForPlotting) {
		this.availableForPlotting = availableForPlotting;
	}


	public void setVerticallyOffTheScreen(boolean verticallyOffTheScreen) {
		this.verticallyOffTheScreen = verticallyOffTheScreen;
	}

        
	public void setChannelSamples(double[] channelSamples, int samplingRate) {
		this.channelSamples = channelSamples;

		DescriptiveStatistics ds = new DescriptiveStatistics(channelSamples);
		this.minVoltage = ds.getMin();
		this.maxVoltage = ds.getMax();
		this.maxP2Pvoltage = maxVoltage - minVoltage;
		this.averageVoltage = ds.getMean();
		this.standardVoltageDeviation = ds.getStandardDeviation();
		this.frequency = calculateFrequency(channelSamples, samplingRate);

	}

	public void setMinVoltage(double minVoltage) {
		this.minVoltage = minVoltage;
	}

	public void setMaxVoltage(double maxVoltage) {
		this.maxVoltage = maxVoltage;
	}

	public void setMaxP2Pvoltage(double maxP2Pvoltage) {
		this.maxP2Pvoltage = maxP2Pvoltage;
	}

	public void setStandardVoltageDeviation(double stdDev) {
		this.standardVoltageDeviation = stdDev;
	}

	public void setAverageVoltage(double averageVoltage) {
		this.averageVoltage = averageVoltage;
	}

	public void setFrequency(double frequency) {
		this.frequency = frequency;
	}

	public void setGraphLineColor(int[] graphLineColor) {
		this.graphLineColor = graphLineColor;
	}
	
	public void setVisibleChannelSamples(double[] visibleChannelSamples,
										 int samplingRate) {
		this.visibleChannelSamples = visibleChannelSamples;
		
		if (visibleChannelSamples.length == 0) {
			this.minVoltage = Double.NEGATIVE_INFINITY;
			this.maxVoltage = Double.NEGATIVE_INFINITY;
			this.maxP2Pvoltage = Double.NEGATIVE_INFINITY;
			this.averageVoltage = Double.NEGATIVE_INFINITY;
			this.standardVoltageDeviation = Double.NEGATIVE_INFINITY;
		} else {
			DescriptiveStatistics ds = new DescriptiveStatistics(
					visibleChannelSamples);
			this.minVoltage = ds.getMin();
			this.maxVoltage = ds.getMax();
			this.maxP2Pvoltage = maxVoltage - minVoltage;
			this.averageVoltage = ds.getMean();
			this.standardVoltageDeviation = ds.getStandardDeviation();
		}

	}
	
	/*************************************************
	 * Private helper methods
	 *************************************************/
	
	/**
	 * This method calculates the frequency of the samples.
	 * @param channelSamples - The samples to perform the freq calculation on
	 * @param samplingRate - The sample rate which is necessary for the calculation
	 * @return the calculated frequency
	 */
	private double calculateFrequency(double[] channelSamples, int samplingRate) {
		
		int numSamples = channelSamples.length;
		
		DoubleFFT_1D fft = new DoubleFFT_1D(numSamples);
		double[] fftData = new double[numSamples*2];
		double[] magnitude = new double[numSamples];
		 
		 for(int i = 0; i < numSamples; i++) {
			 fftData[2*i] = channelSamples[i];
			 fftData[2*i+1] = 0;
		 }
		 
		 fft.complexForward(fftData);
		 
		 
		 for (int i = 0; i < numSamples; i++) {
			 double real = fftData[2*i];
			 double imag = fftData[2*i+1];
			 magnitude[i] = Math.sqrt(real*real + imag*imag);
		 }
		 
		 double maxMagnitude = 0.0000000001;
		 int maxIndex = 0;
		 
		 for (int i = 1; i < numSamples; i++) {
			 if (magnitude[i] > maxMagnitude) {
				 maxMagnitude = magnitude[i];
				 maxIndex = i;
			 }
		 }

		 if (maxIndex > (channelSamples.length)/2) {
			 maxIndex = channelSamples.length - maxIndex;
		 }
		
		return  maxIndex * (samplingRate) / numSamples;
	}
	
}
