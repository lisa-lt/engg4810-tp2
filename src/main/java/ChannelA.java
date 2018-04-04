/**
 * An class to represent Channel A, and store specific properties relating to 
 * it's channel. This includes it's channel offset, and graph line color to
 * render on the oscilloscope display.
 * @author Lisa Liu-Thorrold
 *
 */
public class ChannelA extends OscilloscopeChannel {

	double[] bandpassedSamples;

    public ChannelA() {
        // Channel A has red graph line color
        int[] graphLineColor = {255,0,0};
        setGraphLineColor(graphLineColor);
        setAvailableForPlotting(true);

    }
    
    public void setBandpassedSamples(double[] bandpassedSamples) {
    	this.bandpassedSamples = bandpassedSamples;
    }
    
    public double[] getbandpassedSamples() {
    	return bandpassedSamples;
    }

}
