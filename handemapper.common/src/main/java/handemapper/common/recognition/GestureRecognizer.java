/**
 * 
 */
package handemapper.common.recognition;

import handemapper.common.recognition.device.DeviceInfo;

import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;


/**
 * Provides an interface for any implementations for a gesture recognition 
 * process.
 * @author Chris Hartley
 */
public interface GestureRecognizer {

	/**
	 * Provides a enumeration of the possible states for this interface.
	 * @author Chris
	 */
	public enum State {
		
		/**
		 * The {@link GestureRecognizer} has been created but not yet
		 * initialized. This state requires that the instance becomes 
		 * initialized prior to starting.
		 */
		WAITING,
		
		/**
		 * The {@link GestureRecognizer} is created and initializing. Once the
		 * initialization process is completed, the state should change to 
		 * either STARTED or ERROR depending on initialization success.
		 */
		INITIALIZING,
		
		/**
		 * The {@link GestureRecognizer} is started and currently running.
		 */
		STARTED,
		
		/**
		 * The {@link GestureRecognizer} has been canceled by a user request.
		 * This state should allow for the {@link GestureRecognizer} to be 
		 * started again.
		 */
		CANCELED,
		
		/**
		 * The {@link GestureRecognizer} was terminated due to an internal error
		 * when capturing or processing the video capture frames.
		 */
		TERMINATED,
		
		/**
		 * The {@link GestureRecognizer} is currently paused and not requesting
		 * any frames from the camera. This state should allow for the 
		 * {@link GestureRecognizer} to be started again.
		 */
		PAUSED,
		
		/**
		 * The {@link GestureRecognizer} encountered an error while detecting
		 * a gesture within a specific {@link Gesture} implementation.
		 */
		ERROR
	}
	
	
	/**
	 * Provides an enumeration of the property change event types that contains
	 * their respective property name, accessible through {@link #toString()}.
	 * @author Chris
	 */
	public enum Property {
		
		/**
		 * The property name for a state status change. The values for a 
		 * property change will be of the type {@link State}.
		 */
		STATUS("gesture_recognizer_event_property_status"),
		
		/**
		 * The property name for a capture event when a new video frame has been
		 * retrieved from the video capture device. The values for a capture 
		 * property change will be of the type {@link BufferedImage}.
		 */
		CAPTURE("gesture_recognizer_event_property_capture");
		
		
		// Private member data.
		private final String propName;
		
		
		/**
		 * Private constructor for a new instance of a {@link Property}.
		 * @param propName
		 */
		private Property(String propName) {
			this.propName = propName;
		}
		
		@Override
		public String toString() {
			return this.propName;
		}
	}
	
	
	/**
	 * Starts the video capturing from a connected camera to be processed for
	 * any recognized gestures.
	 * 
	 * @return  a {@code true} if the detection worker was started successfully;
	 *          otherwise, returns {@code false} on any errors.
	 */
	public boolean start();
	
	
	/**
	 * Pauses the video capture worker and attempts to cancel any video capture
	 * frame processing.
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
	 * Adds the specified property change listener to this implementation. The
	 * properties that may change are; "status" [INITIALIZING, STARTED, CANCELED,
	 * TERMINATED, PAUSED, ERROR], "capture" as a new video capture frame of 
	 * type [org.opencv.core.Mat])
	 * @param pcl
	 */
	public void addPropertyChangeListener(PropertyChangeListener pcl);

	
	/**
	 * Removes the specified property change listener from this implementation.
	 * This does nothing if the property change listener has not been added.
	 * @param pcl
	 */
	public void removePropertyChangeListener(PropertyChangeListener pcl);
	
	
	/**
	 * Returns the current video capture device information object.
	 * @return
	 */
	public DeviceInfo getDeviceInfo();

}
