/**
 * A class to represent the filter channel, and store specific properties 
 * relating to  it's channel. This includes it's channel offset, the iir/fir
 * filters, and and graph line color to render on the oscilloscope display.
 * @author Lisa Liu-Thorrold
 *
 */
public class FilterChannel extends OscilloscopeChannel {

    private String filterType;
    private double[] firFilter;
    private double[] iirFilter1;
    private double[] iirFilter2;
    private boolean filterChannelFilesSet;
    private boolean filterInputSet;

    public FilterChannel() {

        // Filter Channel has pastel purple color
        int[] graphLineColor = {173,153,226};
        setGraphLineColor(graphLineColor);
        setAvailableForPlotting(false);

    }
    
	/*************************************************
	 * Getter/ Setter methods
	 *************************************************/
    
    public void setFilterType(String filterType) {
    	this.filterType = filterType;
    }
    
    public String getFilterType() {
    	return filterType;
    }
    
    public void setFirFilter(double[] firFilter) {
    	this.firFilter = firFilter; 	
    }
    
    public void setIirFilter(double[] iirFilter1, double[] iirFilter2) {
    	this.iirFilter1 = iirFilter1;
    	this.iirFilter2 = iirFilter2;
    }
    
    public double[] getFirFilter() {
    	return firFilter;
    }
    
    public Object[] getIirFilters() {
    	return new Object[]{iirFilter1, iirFilter2};
    }

    public void setFilterChannelFilesSet(boolean filterChannelFilesSet) {
        this.filterChannelFilesSet = filterChannelFilesSet;
    }

    public boolean getFilterChannelFilesSet() {
        return filterChannelFilesSet;
    }

    public void setFilterInputSet(boolean filterInputSet) {
        this.filterInputSet = filterInputSet;
    }

    public boolean getFilterInputSet() {
        return filterInputSet;
    }

}
