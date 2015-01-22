/**
 * 
 */
package handemapper.opencv.loader;

import java.net.URISyntaxException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.opencv.core.Core;


/**
 * Provides support for dynamic OpenCV native library loader that determines the
 * system architecture and operating system to load the appropriate native file.
 * @author Chris Hartley
 */
public final class OpenCvNativeLibrary {

	/** Static log4j logger instance for this class. */
	private static final Logger logger = 
			LogManager.getLogger(OpenCvNativeLibrary.class);
	
	
	/**
	 * The default OpenCV native library version/folder.
	 */
	private static final String openCV_Version = "2.4.9";
	
	
	/**
	 * The base directory to the native OpenCV libraries.
	 */
	private static final String baseDir = "./native/opencv/" + openCV_Version + "/";
	
	
	/**
	 * Attempts to load the appropriate native OpenCV library based on this 
	 * class' default values for the version and base directory.
	 * @return  {@code true} if the native library was correctly loaded; 
	 *          otherwise, returns {@code false} on any exceptions thrown.
	 * @see #load(ClassLoader)
	 */
	public static final boolean load() {
		return load(OpenCvNativeLibrary.class.getClassLoader());
	}
	
	
	/**
	 * Attempts to load the appropriate native OpenCV library based on the 
	 * specified path to the library file using this class' {@link ClassLoader}.
	 * @param path  The native OpenCV library path.
	 * @return  {@code true} if the native library was correctly loaded; 
	 *          otherwise, returns {@code false} on any exceptions thrown.
	 * @see #load(ClassLoader, String)
	 */
	public static final boolean load(String path) {
		return load(OpenCvNativeLibrary.class.getClassLoader(), path);
	}
	
	
	/**
	 * Attempts to load the appropriate native OpenCV library relative to the 
	 * specified class loader. This method uses this class' default path for 
	 * the native file locations.
	 * @param cl  The specific class loader to use when resolving the default
	 *            library path.
	 * @return  {@code true} if the native library was correctly loaded; 
	 *          otherwise, returns {@code false} on any exceptions thrown.
	 * @see OperatingSystem
	 * @see SystemArchitecture
	 * @see #load(ClassLoader, String)
	 * @see org.opencv.core.Core#NATIVE_LIBRARY_NAME
	 */
	public static final boolean load(ClassLoader cl) {
		StringBuilder path = new StringBuilder(baseDir);
		path.append(OperatingSystem.getCurrent().getDirectory());
		path.append(SystemArchitecture.getCurrent().getDirectory());
		path.append(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
		path.append(OperatingSystem.getCurrent().getExtension());
		
		return load(cl, path.toString());
	}
	
	
	/**
	 * Attempts to load the appropriate native OpenCV library based on the 
	 * specified file path and class loader.
	 * @param cl    The specific class loader to use when resolving the library 
	 *              path.
	 * @param path  The native OpenCV library path.
	 * @return  {@code true} if the native library was correctly loaded; 
	 *          otherwise, returns {@code false} on any exceptions thrown.
	 */
	public static final boolean load(ClassLoader cl, String path) {
		logger.debug("System.getProperty('opencv.java.version') = "
				+ System.getProperty("opencv.java.version"));
		
		String msg = null;
		try {
			System.load(cl.getResource(path).toURI().normalize().getPath());
	    	return true;
	    }
	    catch (SecurityException se) {
	    	msg = "Security manager dose not allow loading of the native "
	    			+ "library for OpenCV: '"
	    			+ Core.NATIVE_LIBRARY_NAME + "'. Error message:\n"
	    			+ se.getMessage();
	    }
	    catch (UnsatisfiedLinkError usle) {
	    	msg = "Required dynamic library is unsatisfiably linked: '"
	    			+ Core.NATIVE_LIBRARY_NAME + "'. Error message:\n"
	    	        + usle.getMessage();
	    }
	    catch (NullPointerException npe) {
	    	msg = "Required dynamic library for OpenCV is <null>. "
	    			+ "Error message:\n" + npe.getMessage();
	    }
		catch (URISyntaxException urise) {
			msg = "URISyntaxException: " + urise.getMessage();
		}
		
		if (msg != null) {
			logger.error(msg);
		}
		
		return false;
	}
	
}
