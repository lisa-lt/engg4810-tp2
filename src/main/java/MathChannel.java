/**
 * An class to represent the math channel, and store specific properties 
 * relating to it's channel. This includes it's channel offset, equation, 
 * and graph line color to render on the oscilloscope display.
 * @author Lisa Liu-Thorrold
 *
 */
public class MathChannel extends OscilloscopeChannel {

    String equation;

    public MathChannel() {
        // Math Channel has yellow graph line color
        int[] graphLineColor = {255,255,0};
        setGraphLineColor(graphLineColor);
        setAvailableForPlotting(false);
        equation = "";
    }


    /*************************************************
     *  Getter/ Setter methods
     *************************************************/

    public void setEquation(String equation) {
        this.equation = equation;
    }

    public String getEquation() {
        return equation;
    }



}
