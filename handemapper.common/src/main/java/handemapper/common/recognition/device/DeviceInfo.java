/**
 * 
 */
package handemapper.common.recognition.device;

import java.io.Serializable;

/**
 * Provides a holder for a video capture device information. This should be 
 * populated using the {@link org.opencv.highgui.VideoCapture} properties:<ul>
 * <li>CV_CAP_PROP_FRAME_WIDTH Width of the frames in the video stream.
 * <li>CV_CAP_PROP_FRAME_HEIGHT Height of the frames in the video stream.
 * </ul>
 *
 * @author Chris
 *
 */
public interface DeviceInfo extends Serializable {

	public String getId();
	
	public int getIndex();
	
	public int getWidth();
	
	public int getHeight();
	
	public double getFPS();
	
	public long getTimestamp();
	
	public boolean isOpened();
	
}
