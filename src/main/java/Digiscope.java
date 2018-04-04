/**
 * This class contains the main method for entry into the application.
 * @author Lisa Liu-Thorrold
 *
 */
public class Digiscope {

	public static void main(String[] args) {
		new Digiscope();
	}
	
	
	/**
	 * This is the main entry point of the application.
	 */
	private Digiscope() {
		DigiscopeContainer container = new DigiscopeContainer();
		DigiscopeModel model = new DigiscopeModel();
		new DigiscopeController(model, container);
		container.run();
		
	}

}
