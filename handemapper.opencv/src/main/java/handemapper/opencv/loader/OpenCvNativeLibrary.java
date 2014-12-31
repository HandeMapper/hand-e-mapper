/**
 * 
 */
package handemapper.opencv.loader;

import java.net.URISyntaxException;

import org.opencv.core.Core;


/**
 * @author Chris Hartley
 *
 */
public final class OpenCvNativeLibrary {

	private static final String openCV_Version = "2.4.9";
	private static final String baseDir = "/native/opencv/" + openCV_Version + "/";
	static {
		System.out.println("opencv version=" + System.getProperty("opencv.java.version"));
	}
	
	
	public static boolean load() {
		return load(OpenCvNativeLibrary.class.getClassLoader());
	}
	
	
	public static boolean load(ClassLoader cl) {
		String path = baseDir;
		String msg = null;
		try {
			path += OperatingSystem.getCurrent().getDirectory();
			path += SystemArchitecture.getCurrent().getDirectory();
			path += org.opencv.core.Core.NATIVE_LIBRARY_NAME;
			path += OperatingSystem.getCurrent().getExtension();
			System.load(cl.getResource("." + path).toURI().normalize().getPath());
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
		
		if (msg != null)
			System.err.println(msg);
		
		return false;
	}
}
