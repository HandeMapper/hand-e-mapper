/**
 * 
 */
package handemapper.opencv.loader;

import java.util.regex.Pattern;

/**
 * @author Chris Hartley
 *
 */
public enum SystemArchitecture {

	X86_32("^i[36]86$", "x86_32"),
	X86_64("^(amd|x86_)64$", "x86_64");
	
	private final Pattern pattern;
	private final String dir;
	
	private SystemArchitecture(final String pattern, final String dir) {
		this.pattern = Pattern.compile(pattern);
		this.dir = dir + "/";
	}
	
	
	private boolean is(final String id) {
		return pattern.matcher(id).matches();
	}
	
	
	public final String getDirectory() {
		return new String(dir);
	}
	
	
	public static SystemArchitecture getCurrent() {
		String osArch = System.getProperty("os.arch");

		for (SystemArchitecture sa : SystemArchitecture.values())
			if (sa.is(osArch))
				return sa;
		
		throw new UnsupportedOperationException("System architecture is not supported: " + osArch);
	}
}
