/**
 * This method conceptually represents the Function Generator data type.
 * Makes it easier to encapsulate it into an object to send to the hardware
 * as configurations.
 * @author Lisa Liu-Thorrold
 */
public class FunctionGenerator {

    private String waveType;
    private double peakToPeakVoltage;
    private double offset;
    private short frequency;
    private boolean outputOn;

    public FunctionGenerator(boolean outputOn, String waveType,
                             double peakToPeakVoltage, double offset,
                             short frequency) {
        this.outputOn = outputOn;
        this.waveType = waveType;
        this.peakToPeakVoltage = peakToPeakVoltage;
        this.offset = offset;
        this.frequency = frequency;
    }

    public String getWaveType() {
        return waveType;
    }

    public double getPeakToPeakVoltage() {
        return peakToPeakVoltage;
    }

    public double getOffset() {
        return offset;
    }

    public short getFrequency() {
        return frequency;
    }

    public boolean getOutputOn() { return outputOn; }

    public void setWaveType(String waveType) {
        this.waveType = waveType;
    }

    public void setPeakToPeakVoltage(double peakToPeakVoltage) {
        this.peakToPeakVoltage = peakToPeakVoltage;
    }

    public void setOffset(double offset) {
        this.offset = offset;
    }

    public void setFrequency(short frequency) {
        this.frequency = frequency;
    }

    public void setOutputOn(boolean outputOn) {
        this.outputOn = outputOn;
    }

    /**
     * Return the function generator characteristics in a comma separated line:
     * outputOn,waveType,peakToPeakVoltage,offset,frequency
     *
     * @return
     */
    @Override
    public String toString() {
        return outputOn + "," + waveType + "," + Double.toString(peakToPeakVoltage)
                + "," + Double.toString(offset) + "," + Double.toString(frequency);
    }


}
