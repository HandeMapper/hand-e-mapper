/**
 * 
 */
package handemapper.opencv.loader;

import java.util.regex.Pattern;

/**
 * @author Chris Hartley
 *
 */
public enum OperatingSystem {

	OSX("^[Mm]ac OS X$", "osx", "dylib"),
	LINUX("^[Ll]inux$", "linux", "so"),
	WINDOWS("^[Ww]indows.*", "windows", "dll");
	
	private final Pattern pattern;
	private final String dir;
	private final String ext;
	
	private OperatingSystem(final String pattern, final String dir, final String ext) {
		this.pattern = Pattern.compile(pattern);
		this.dir = dir + "/";
		this.ext = "." + ext;
	}
	
	
	private boolean is(final String id) {
		return pattern.matcher(id).matches();
	}
	
	
	public final String getDirectory() {
		return new String(dir);
	}
	
	
	public final String getExtension() {
		return new String(ext);
	}
	
	
	public static OperatingSystem getCurrent() {
		String osName = System.getProperty("os.name");

		for (OperatingSystem os : OperatingSystem.values())
			if (os.is(osName))
				return os;
		
		throw new UnsupportedOperationException("Operating system is not supported: " + osName);
	}

}
