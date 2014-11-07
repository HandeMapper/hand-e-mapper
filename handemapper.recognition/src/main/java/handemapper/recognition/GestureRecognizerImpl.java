/**
 * 
 */
package handemapper.recognition;

import java.awt.Component;
import java.awt.Image;
import java.awt.image.ImageObserver;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.opencv.highgui.VideoCapture;

import handemapper.common.recognition.Gesture;
import handemapper.common.recognition.GestureRecognizer;
import handemapper.recognition.GestureRecognizerWorker;

/**
 * 
 * @author Chris Hartley
 *
 */
public class GestureRecognizerImpl implements GestureRecognizer {

	/** Static log4j logger instance for this class. */
	private static final Logger logger = LogManager.getLogger(GestureRecognizerImpl.class);
	
	// Private member data.
	private ImageIcon vcImage = null;
	private VideoCapture vcDevice = null;
	private GestureRecognizerWorker grProcessor = null;
	private final Map<String,Gesture> gestures =
			Collections.synchronizedMap(new HashMap<String,Gesture>());
	

	/**
	 * Constructor for a new instance of a gesture recognizer that will 
	 * auto-start the video capture worker thread.
	 */
	public GestureRecognizerImpl() {
		this(true);
	}
	
	
	/**
	 * Constructor for a new instance of a gesture recognizer with the specified
	 * parameter to auto-start the video capture device.
	 * 
	 * @param autoStart If {@code true}, this automatically starts the video 
	 *                  capture thread; otherwise, the {@link #start()} method 
	 *                  must be called.
	 */
	public GestureRecognizerImpl(boolean autoStart) {
		vcImage = new ImageIcon() {
			
			private static final long serialVersionUID = 1529931784440692166L;

			@Override
			public void setImage(Image image) {
				super.setImage(image);
				
				ImageObserver iob = getImageObserver();
				if (iob instanceof Component)
					((Component)iob).repaint();
			}
			
		};
		
		if (autoStart)
			start();
	}
	
	
	@Override
	public final ImageIcon getVideoCaptureImageIcon() {
		return vcImage;
	}
	
	
	@Override
	public final VideoCapture getVideoCaptureDevice() {
		return vcDevice;
	}
	
	
	@Override
	public final Gesture register(Gesture gesture) {
		if (gesture == null || gestures.containsKey(gesture.getName()))
			return null;
		
		gestures.put(gesture.getName(), gesture);
		return gesture;
	}
	
	
	@Override
	public double getFPS() {
		return grProcessor != null ? grProcessor.getFPS() : 0d;
	}
	
	
	@Override
	public void start() {
		stop();
		logger.debug("Starting the GestureRecognizer...");
		if (vcDevice == null) {
			vcDevice = new VideoCapture(0);
			if (!vcDevice.isOpened())
				vcDevice.open(0);
		}
		
		if(!vcDevice.isOpened()) {
			logger.error("Default device not opened for video capture!");
			return;
		}
		
		grProcessor = new GestureRecognizerWorker(vcDevice, vcImage, gestures);
		grProcessor.execute();
	}
	
	
	@Override
	public void stop() {
		if (grProcessor != null) {
			grProcessor.cancel(true);
		}
		if (vcDevice != null) {
			vcDevice.release();
		}
	}
	
	
	@Override
	public String toString() {
		return getClass().getSimpleName()
				+ "[device=" + vcDevice + ", gesture(s)='"
				+ Arrays.toString(getGestureNames())
				+ "']";
	}
	
	/**
	 * 
	 * @return
	 */
	public String[] getGestureNames() {
		String[] tmp = new String[1];
		return gestures.keySet().toArray(tmp);
	}
	
	
	@Override
	public final Gesture[] getGestures() {
		Gesture[] tmp = new Gesture[1];
		return gestures.values().toArray(tmp);
	}


	/**
	 * 
	 * @return
	 */
	public final int getVideoCaptureWidth() {
		return vcImage != null ? vcImage.getIconWidth() : 0;
	}


	/**
	 * 
	 * @return
	 */
	public final int getVideoCaptureHeight() {
		return vcImage != null ? vcImage.getIconHeight() : 0;
	}

}
