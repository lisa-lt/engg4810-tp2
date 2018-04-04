/**
 * An class to represent Channel B, and store specific properties relating to 
 * it's channel. This includes it's channel offset, and graph line color to
 * render on the oscilloscope display.
 * @author Lisa Liu-Thorrold
 *
 */
public class ChannelB extends OscilloscopeChannel {

    public ChannelB() {
        // Channel B has blue line color
        int[] graphLineColor = {0,255,0};
        setGraphLineColor(graphLineColor);
        setAvailableForPlotting(true);
    }

}
