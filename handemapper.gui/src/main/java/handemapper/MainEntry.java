/**
 * Hand Gesture Detection with OpenCV
 * Group Project - CSC 484, Winter Quarter 2014
 * California Polytechnic State University, San Luis Obispo
 * 
 * Collaboration with:
 *   Carmen Badea, Intel Corp. (carmen.t.badea@intel.com)
 * 
 * Project Facilty Advisor:
 *   Franz J. Kurfess (fkurfess@calpoly.edu)
 * 
 * Team members:
 *   @author Chris Hartley (cnhartle@calpoly.edu)
 *   @author Adin Miller   (amille@calpoly.edu)
 *   @author Shubham Kahal (observer)
 * 
 * Abstract:
 * ...
 * 
 *
 */
package handemapper;

import handemapper.gui.tray.GestureTrayApp;
import handemapper.opencv.loader.OpenCvNativeLibrary;

import java.awt.AWTException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;


/**
 * Provide the main entry point for the graphical user interface and supporting
 * gesture detection implementations of this package. This class supplies the
 * {@code main(String[] args)} method to allow command-line parameters and load
 * the full-screen application to demonstrate the hand gestures masked to mouse
 * input.
 * 
 * @author Chris Hartley
 * @author Adin Miller
 */
public final class MainEntry {

	/** Static log4j logger instance for this class. */
	private static final Logger logger = LogManager.getLogger(MainEntry.class);
	
	/**
	 * Provides a static field containing the name of this application to be
	 * used in any implementation environment.
	 */
	public static final String APP_NAME =
			"Gesture Recognization - Grab, Drag, Drop, and Throw";
	
	
	// Private member data.
	private static boolean reconfigure = false;
	private static boolean runInSystemTray = true;
	
	
	/**
	 * This method provides the main entry point of the overall application.
	 * Several command-line parameters may be used in order to specify certain
	 * application settings on start-up.
	 * 
	 * @param args	an array of {@link String}s for each whitespace separated
	 * 				command-line word.
	 */
	public static void main(String[] args) {
		// Parse the command-line parameters...
		parseCommandLineParameters(args);
		
		// Set to the system look & feel...
	    setSystemLookAndFeel();
	    
		// If command-line parameters contained reconfigure or no configuration
	    // file was found, create a new configuration for this program.
		if (reconfigure) {// || !config.checkIfConfigFileExists()) {
			;//TODO FIX: createNewConfiguration();
		}
		
		// Initialize the OpenCV preliminary configurations...
		loadOpenCvNativeLibrary();
		
	    // Initialize and run the application...
	    SwingUtilities.invokeLater( initializeGraphicalUserInterface() );
	}
	
	
	/**
	 * Initializes the graphical user interface (GUI) based on the command-line
	 * parameters and the configuration file and returns the reference to the 
	 * GUI as a {@link Runnable} interface. The {@code run()} method of the GUI
	 * to load initializes and displays their application implementation.
	 * 
	 * @return	a reference to the {@link Runnable} GUI interface to be
	 * 			executed by the caller.
	 */
	private static final Runnable initializeGraphicalUserInterface() {
		Runnable app = null;
	    if (runInSystemTray) {
	    	try {
				app = new GestureTrayApp(APP_NAME);
			}
	    	catch (UnsupportedOperationException
	    			| SecurityException
	    			| AWTException cause)
	    	{
				cause.printStackTrace();
				logger.error("Unable to create system tray application!", cause);
			}
	    }
	    else {
	    	logger.warn("No other loadable versions of the application!");
	    }
	    
	    return app;
	}
	
	
	/**
	 * Attempts to set the look and feel of the user interface to the users'
	 * default system look and feel. If any exceptions are caught while 
	 * attempting to set the look and feel nothing happens.
	 */
	private static final void setSystemLookAndFeel() {
		try {
	    	UIManager.setLookAndFeel(
	    			UIManager.getSystemLookAndFeelClassName() );
	    }
	    catch (Exception cause) {
	    	logger.warn("Unable to get system look and feel", cause);
	    }
	}
	
	
	/**
	 * Parses the command-line arguments specified in the {@link String} array
	 * parameter.
	 * 
	 * @param args
	 */
	private static final void parseCommandLineParameters(String[] args) {
		final List<String> params = new ArrayList<String>();
		for (String arg : args)
			params.add(arg);
		
		runInSystemTray |= params.contains("--tray") || params.contains("-t");
		reconfigure |= params.contains("--reconfig") || params.contains("-r");
	}
	
	
	/**
	 * Load the native libraries for the OpenCV installation. If there are any
	 * errors, this method will attempt to reconfigure the configuration file
	 * and request for updated locations of the require native library files
	 * for OpenCV.
	 * 
	 * @see handemapper.opencv.loader.OpenCvNativeLibrary#load()
	 */
	private static final void loadOpenCvNativeLibrary() {
		if (!OpenCvNativeLibrary.load()) {
			throw new RuntimeException("Could not load native OpenCV libraries!");
		}
	}

}
