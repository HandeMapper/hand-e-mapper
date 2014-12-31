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
	 * Starts the video capturing from a connected camera to be processed for
	 * any recognized gestures.
	 */
	public void start();
	
	
	/**
	 * Stops the video capture and cancels any processing.
	 */
	public void stop();
	
	
	/**
	 * Registers the specified {@link Gesture} to this implementation of the 
	 * gesture recognizer for processing.
	 * @param gesture
	 * @return
	 */
	public Gesture register(Gesture gesture);
	
	
	/**
	 * Returns an array of all registered {@link Gesture}s to this 
	 * implementation of a gesture recognizer.
	 * @return
	 */
	public Gesture[] getGestures();
	
	
	/**
	 * Returns the current frames per second rate, as a double, of the video 
	 * capture.
	 * @return
	 */
	public double getFPS();


	/**
	 * Returns the {@link ImageIcon} which contains the most recent video 
	 * capture from the camera.
	 * @return
	 */
	public ImageIcon getVideoCaptureImageIcon();
	
	
	/**
	 * Returns the {@link VideoCapture} device used in this implementation.
	 * @return
	 */
	public VideoCapture getVideoCaptureDevice();
	
}
