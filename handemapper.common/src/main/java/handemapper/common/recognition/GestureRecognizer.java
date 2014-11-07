/**
 * 
 */
package handemapper.common.recognition;

import javax.swing.ImageIcon;

import org.opencv.highgui.VideoCapture;


/**
 * @author Chris Hartley
 *
 */
public interface GestureRecognizer {

	/**
	 * 
	 */
	public void start();
	
	
	/**
	 * 
	 */
	public void stop();
	
	
	/**
	 * 
	 * @param gesture
	 * @return
	 */
	public Gesture register(Gesture gesture);
	
	
	/**
	 * 
	 * @return
	 */
	public Gesture[] getGestures();
	
	
	/**
	 * 
	 * @return
	 */
	public double getFPS();


	/**
	 * 
	 * @return
	 */
	public ImageIcon getVideoCaptureImageIcon();
	
	
	/**
	 * 
	 * @return
	 */
	public VideoCapture getVideoCaptureDevice();
	
}
