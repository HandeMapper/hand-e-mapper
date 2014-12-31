/**
 * 
 */
package handemapper.gui;

import java.awt.Image;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * 
 * @author Chris Hartley
 * @see Runnable
 */
public abstract class GestureApplication implements Runnable {

	/** Static log4j logger instance for this class. */
	protected static final Logger logger = LogManager.getLogger(GestureApplication.class);
	
	
	// Private static fields.
	private static final String trayIconPathFrmt = "/icons/trayIcon_%dx%d.png"; 
	private static final Map<Integer,Image> iconSizeToImageMap = loadIcons();
	private static final Toolkit toolkit = Toolkit.getDefaultToolkit();
	
	// Private member data.
	private final String title;
	
	
	/**
	 * Constructor for a new instance of this gesture application with the
	 * specified title.
	 * @param title
	 */
	protected GestureApplication(String title) {
		this.title = title;
	}
	
	
	/**
	 * Returns this gesture applications title.
	 * @return
	 */
	public final String getTitle() {
		return "" + this.title;
	}
	
	
	/**
	 * Returns this instances associated {@link Toolkit}.
	 * @return
	 */
	public final Toolkit getToolkit() {
		return toolkit;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public final List<Image> getIcons() {
		List<Image> list = new ArrayList<Image>(iconSizeToImageMap.size());
		list.addAll(iconSizeToImageMap.values());
		return Collections.unmodifiableList(list);
	}
	
	
	/**
	 * Returns the appropriate icon based on the specified pixel size parameter.
	 * @param size
	 * @return
	 */
	public final Image getIcon(int size) {
		if (!iconSizeToImageMap.containsKey(size)) {
			if (size > 42)
				size = 48;
			else if (size > 31)
				size = 36;
			else if (size > 20)
				size = 24;
			else
				size = 16;
		}
		return iconSizeToImageMap.get(size);
	}
	
	
	/**
	 * 
	 */
	abstract public void close();
	
	
	/**
	 * 
	 * @return
	 */
	private static final Map<Integer,Image> loadIcons() {
		final int[] sizes = { 16, 24, 36, 48 };
		final Map<Integer,Image> map = new HashMap<Integer,Image>(sizes.length);
		String imgPath = null;
		
		for (int size : sizes) {
			try {
				imgPath = String.format(trayIconPathFrmt, size, size);
				map.put(size, ImageIO.read(
						GestureApplication.class.getResourceAsStream(imgPath)
				));
			} catch (Exception ignore) { }
		}
		return Collections.unmodifiableMap(map);
	}
	
}
