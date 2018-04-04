import java.util.ArrayList;
import java.util.Arrays;

import processing.core.PApplet;

/**
 * This class is the oscilloscope display. It is responsible for plotting the
 * graph data which include the legend(s), and replotting when resolution 
 * changes. It also is responsible for transforming a clicked sample back into
 * a voltage resolution.
 * @author Lisa Liu-Thorrold
 *
 */
public class OscilloscopeDisplay extends PApplet {

	private DigiscopeModel model;

	private final float WIDTH = 1000;
	private final float HEIGHT = 650;
	private final int NUM_VERTICAL_SECTIONS = 12;
	private final int NUM_HORIZONTAL_SECTIONS = 16;
	private final float VERTICAL_SECTION_SIZE = HEIGHT/NUM_VERTICAL_SECTIONS;
	private final float HORIZONTAL_SECTION_SIZE = WIDTH/NUM_HORIZONTAL_SECTIONS;
	// the x coordinate that the y axis cuts across.
	private final float VERTICAL_ZERO = WIDTH/2;
	// the y coordinate that the x axis cuts across.
	private final float HORIZONTAL_ZERO = HEIGHT/2;
	private float prevX = (float)0;
	private float prevY = (float)HEIGHT/2;
	private short triggerIndex;

	public OscilloscopeDisplay(DigiscopeModel model) {
		this.model = model;
	}

	public void setup() {
		size(Math.round(WIDTH), Math.round(HEIGHT));
		background(51);
		drawGrid();
	}

	public void draw() {
		noLoop();
	}

	/**
	 * This method is called by the digiscope client when samples are 
	 * initially received by the client. The samples are initially plotted to 
	 * fit the width of the display, the one vertical section one unit of 
	 * vertical resolution. 
	 * @param samples - The samples to plot
	 * @param verticalResolution - The vertical resolution to plot at
	 * @param channelColors - The line colors of the graph
	 */

	/**
	 * This method is called by the digiscope client when samples are
	 * initially received by the client. The samples are initially plotted to
	 * fit the width of the display, the one vertical section one unit of
	 * vertical resolution.
	 * @param verticalResolution - The vertical resolution to plot at
	 * @param channel - The channel to get the samples and colors to plot from
	 * @param bandpass - Whether this is a bandpass channel to plot or not
     */
	public void initialPlotChannel(double verticalResolution,
			OscilloscopeChannel channel, boolean bandpass) {

		int[] channelColors = channel.getGraphLineColor();
		double[] chanSamples;

		if (bandpass) {
			chanSamples = ((ChannelA)channel).getbandpassedSamples();
		} else {
			chanSamples = channel.getChannelSamples();
		}

		ArrayList<Double> visibleChannelSamples = new ArrayList<Double>();
		channel.setVerticallyOffTheScreen(false);

		// To handle the first one that isn't plotted - check if it's vertically
		// off the screen
		if (!verticallyOffScreen(channel, (float)(-(chanSamples[0] *
				VERTICAL_SECTION_SIZE * 1/verticalResolution) + HORIZONTAL_ZERO))) {
			visibleChannelSamples.add(chanSamples[0]);
		}

		float x;
		float y;
		float widthScaling = (float)1000.0/(float)chanSamples.length;

		prevX = (float)chanSamples[0];
		prevY = (float)(-(chanSamples[0] * VERTICAL_SECTION_SIZE) + HORIZONTAL_ZERO);

		// Plot Channel. All samples are made to initially fit the width of the
		// screen. The user can zoom in and out thereafter.
		for (int i=1; i < chanSamples.length; i++) {
			stroke(channelColors[0], channelColors[1], channelColors[2]);
			x = widthScaling * i;
			y = (float)(-(chanSamples[i] * VERTICAL_SECTION_SIZE *
					1/verticalResolution) + HORIZONTAL_ZERO);
			line(prevX,prevY, x, y);

			prevX = x;
			prevY = y;	

			if (i == triggerIndex) {
				//draw a vertical line to indicate the trigger point
				stroke(255,153,204,80);
				line(x,0,x,HEIGHT);
			}


			// Don't plot any longer we are off the horizontal access - waste
			// of time/processing
			if(horizontallyOffScreen(x)) {
				break;
			}

			// check if vertically off the screen - if so, add it to the list of
			// samples that we perform the calculations on - set flag to display
			// n/a for frequency calculation
			if (!verticallyOffScreen(channel, y))  {
				visibleChannelSamples.add(chanSamples[i]);
			}

		}

		double[] visibleChannelSamplesArray = visibleChannelSamples.stream().
				mapToDouble(Double::doubleValue).toArray();

		channel.setVisibleChannelSamples(visibleChannelSamplesArray,
				model.getSamplingRate());

		redraw();
	}

	/**
	 * This method detects a mouse click, and displays a voltage value. We are 
	 * only concerned about the value of the voltage (height).
	 */
	public void mousePressed() {

		double selectedSampleVoltage = (mouseY - HORIZONTAL_ZERO)
				* -(model.getVoltsPerDivisionDisplayed() / VERTICAL_SECTION_SIZE);

		model.setSelectedSampleVoltage(selectedSampleVoltage);
	}


	/*************************************************
	 * Resolution related methods
	 *************************************************/

	/**
	 * Takes method replots the samples at the new resolution. It does this
	 * by taking the appropriate scaling factors of what is currently being
	 * displayed.
	 */
	public void updateResolution(double horizScalingFactor) {

		if(model.getChannelAisPlotted() || model.getChannelBisPlotted() ||
				model.getMathChannelIsPlotted() || model.getFilterChannelIsPlotted()) {
			// reset the currently selected voltage sample
			model.setSelectedSampleVoltage(Double.NEGATIVE_INFINITY);
		}

		clearGrid();

		double verticalResolution = model.getVoltsPerDivisionDisplayed();
		double scalingFactor = model.getTimePerDivision() /
				model.getTimePerDivisionDisplayed();

		if (model.getChannelAisPlotted()) {

			// Is channel a - check bandpass
			boolean bandpassOn = model.getBandpassSampling().equals("On") ?
					true : false;
			plotChannelNewResolution(verticalResolution,
					scalingFactor, model.getChannelA(), bandpassOn);
		}

		// Update channel b if user wants it plotted
		if (model.getChannelBisPlotted()) {
			plotChannelNewResolution(verticalResolution, scalingFactor,
					model.getChannelB(), false);
		}

		// update math channel if user wants it plotted
		if (model.getMathChannelIsPlotted() &&
				model.getMathChannel().getAvailableForPlotting()) {
			plotChannelNewResolution(verticalResolution, scalingFactor,
					model.getMathChannel(), false);
		}

		// update filter channel if user wants it plotted
		if (model.getFilterChannelIsPlotted() &&
				model.getFilterChannel().getAvailableForPlotting()) {
			plotChannelNewResolution(verticalResolution, scalingFactor,
					model.getFilterChannel(), false);
		}

		redraw();
	}

	/**
	 * Helper method for replotting a channel with the new resolution
	 * @param channel - The osciloscope channel containing information such as
	 *                	colors and samples
	 * @param verticalResolution - The new vertical resolution
	 * @param horizontalScalingFactor - The scaling factor for the horizontal
	 * 									resolution adjustment
	 */
	private void plotChannelNewResolution(double verticalResolution,
			double horizontalScalingFactor, OscilloscopeChannel channel,
										  boolean bandpass) {

		double[] chanSamples;

		if (bandpass) {
			chanSamples = ((ChannelA)channel).getbandpassedSamples();
		} else {
			chanSamples = channel.getChannelSamples();
		}


		int[] channelColors = channel.getGraphLineColor();
		ArrayList<Double> visibleChannelSamples = new ArrayList<Double>();
		channel.setVerticallyOffTheScreen(false);

		//To handle the first one that isn't plotted
		if (!verticallyOffScreen(channel, (float)(-(chanSamples[0] *
				VERTICAL_SECTION_SIZE * 1/verticalResolution) + HORIZONTAL_ZERO))) {
			visibleChannelSamples.add(chanSamples[0]);
		}

		float x;
		float y;
		float widthScaling =  (float)1000.0/(float)chanSamples.length;

		prevX = (float)0;
		prevY = (float)(-(chanSamples[0] * VERTICAL_SECTION_SIZE * 1/verticalResolution)
				+ HORIZONTAL_ZERO);

		// Plot the channel
		for (int i=1; i < chanSamples.length; i++) {

			stroke(channelColors[0], channelColors[1], channelColors[2]);

			x = (float) (widthScaling * i * horizontalScalingFactor);
			y = (float)(-(chanSamples[i] * VERTICAL_SECTION_SIZE * 1/verticalResolution)
					+ HORIZONTAL_ZERO);
			line(prevX,prevY, x, y);

			prevX = x;
			prevY = y;			

			if (i == triggerIndex) {
				//draw a vertical line
				stroke(255,153,204,80);
				line(x,0,x,HEIGHT);
			}

			// stop plotting if coordinates fall off x axis - save time
			if(horizontallyOffScreen(x)) {
				break;
			}

			// check if vertically off the screen
			if (!verticallyOffScreen(channel, y))  {
				visibleChannelSamples.add(chanSamples[i]);
			}

		}

		// Set the visible channel samples for measurement recalculation
		double[] visibleChannelSamplesArray = visibleChannelSamples.
				stream().mapToDouble(Double::doubleValue).toArray();

		channel.setVisibleChannelSamples(
				visibleChannelSamplesArray, model.getSamplingRate());

		redraw();

	}

	/**
	 * Checks if a coordinate is vertically off the screen
	 * @param channel - The channel whose visible coordinates are off the screen
	 * @param yCoordinate - The y coordinate to check
     * @return whether the coordinate is outside the plotting screen
     */
	private boolean verticallyOffScreen(OscilloscopeChannel channel,
										float yCoordinate) {
		if ((yCoordinate < 0) || (yCoordinate > 650)) {
			// let the channel know, to invalidate frequency calculation
			channel.setVerticallyOffTheScreen(true);
			return true;
		}

		return false;
	}

	/**
	 * Check if the xCoordinate is off the screen, so we can stop processing
	 * samples, and plotting ones that won't be visible
	 * @param xCoordinate - The x coordinate to check
	 * @return whether the coordinate is beyond the horizontal range of the
	 * 		   display
     */
	private boolean horizontallyOffScreen(float xCoordinate) {
		if (xCoordinate > 1000) {
			return true;
		}

		return false;
	}

	/*************************************************
	 * Set up methods
	 *************************************************/

	/**
	 * This method draws the inital grid
	 */
	private void drawGrid() {

		System.out.println("Draw grid called");
		// Legend Channel A
		fill(200);
		text("Channel A",10,15);

		int[] channelAColors = model.getChannelA().getGraphLineColor();
		fill(channelAColors[0],channelAColors[1],channelAColors[2]);
		text("----------", 80, 15);

		// Legend Channel B
		fill(200);
		text("Channel B",180,15);

		int[] channelBColors = model.getChannelB().getGraphLineColor();
		fill(channelBColors[0],channelBColors[1],channelBColors[2]);
		text("----------", 250, 15);

		// Legend Math Channel
		fill(200);
		text("Math Channel",350,15);

		int[] mathChannelColors = model.getMathChannel().getGraphLineColor();
		fill(mathChannelColors[0],mathChannelColors[1],mathChannelColors[2]);
		text("----------", 450, 15);


		// Legend Filter Channel
		fill(200);
		text("Filter Channel",545,15);

		int[] filterChannelColors = model.getFilterChannel().getGraphLineColor();
		fill(filterChannelColors[0],filterChannelColors[1],filterChannelColors[2]);
		text("----------", 640, 15);

		// Current Resolution
		fill(200);
		text(model.getResolution(),735,15);


		/* Draw the grid */
		stroke(122, 122, 122, 100);

		//vertical center line
		line (VERTICAL_ZERO, 0, VERTICAL_ZERO, HEIGHT);

		//horizontal center line
		line (0, HORIZONTAL_ZERO, WIDTH, HORIZONTAL_ZERO);

		stroke(122, 122, 122, 70);

		// draw the horizontal
		for(int i = 1; i <= NUM_VERTICAL_SECTIONS; i++) {

			if (HEIGHT/NUM_VERTICAL_SECTIONS*i  != HORIZONTAL_ZERO)  {
				drawDashedLine(VERTICAL_SECTION_SIZE*i, true);
			}
		}

		//draw the vertical
		for(int i = 1; i <= NUM_HORIZONTAL_SECTIONS; i++) {

			if (WIDTH/NUM_HORIZONTAL_SECTIONS*i != VERTICAL_ZERO)  {
				drawDashedLine(HORIZONTAL_SECTION_SIZE*i, false);
			}
		}

	}


	/*************************************************
	 * Helper methods
	 *************************************************/

	/**
	 * This method clears the grid and is invoked when either a new lot of
	 * samples have been received, or the resolution has been updated.
	 */
	public void clearGrid() {
		System.out.println("Clearing grid called");
		background(51);
		drawGrid();
	}


	/**
	 * Keep track of the current trigger index
	 * @param triggerIndex
	 */
	public void setTriggerIndex(short triggerIndex) {
		this.triggerIndex = triggerIndex;
	}

	/**
	 * This method draws a dashed line
	 * @param coord
	 * @param horizontal
	 */
	void drawDashedLine(float coord, boolean horizontal) {
		// keep the y coordinates the same
		if (horizontal) {

			int dashValue = 0;
			int dashPoint = 20;
			while(dashValue*dashPoint < WIDTH) {
				line(dashValue * dashPoint, coord,
						(dashValue * dashPoint) + 10, coord);
				dashValue++;
			}
		} else {

			int dashValue = 0;
			int dashPoint = 20;
			while (dashValue*dashPoint < HEIGHT) {
				line(coord, dashValue * dashPoint, coord,
						(dashValue * dashPoint) + 10);
				dashValue++;
			}

		}
	}
}
