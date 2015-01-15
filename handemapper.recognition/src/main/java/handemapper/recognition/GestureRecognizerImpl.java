/**
 * 
 */
package handemapper.recognition;

import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.swing.event.EventListenerList;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;

import handemapper.common.recognition.Gesture;
import handemapper.common.recognition.GestureRecognizer;
import handemapper.common.recognition.device.DeviceInfo;
import handemapper.recognition.GestureRecognizerWorker;
import handemapper.recognition.utils.Updater;

/**
 * Provides an implementation of the {@link GestureRecognizer} to connect and 
 * collect image data from a connected camera.
 * 
 * @author Chris Hartley
 */
public class GestureRecognizerImpl implements GestureRecognizer {

	/** Static log4j logger instance for this class. */
	private static final Logger logger = LogManager.getLogger(GestureRecognizerImpl.class);
	
	
	/**
	 * No available video capture devices found!
	 */
	private static final int NO_DEVICE_FOUND = -1;
	
	
	// Private member data.
	private int vcDeviceIndex = NO_DEVICE_FOUND;
	private long vcImageTimestamp = 0l;
	private State currentState = State.WAITING;
	private DeviceInfo deviceInfo = null;
	private VideoCapture vcDevice = null;
	private GestureRecognizerWorker grProcessor = null;
	
	private final Updater<BufferedImage> frmUpdater = new VideoFrameUpdater();
	private final EventListenerList listeners = new EventListenerList();
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
		if (autoStart)
			start();
	}
	
	
	@Override
	public final Gesture register(Gesture gesture) {
		if (gestures == null)
			return null;
		
		final String gName = gesture.getName();
		synchronized (gestures) {
			if (!this.gestures.containsKey(gName))
				this.gestures.put(gName, gesture);
		}
		
		return gesture;
	}
	
	
	/**
	 * Initializes this implementation by determining the appropriate video 
	 * capture device to open and updates the status.
	 */
	private final void initialize() {
		fireStatusChanged(currentState, State.INITIALIZING);
		int vcCount = getVideoCaptureCount();
		
		if (vcCount == 1) {
			logger.info("Single video capture device found!");
			vcDeviceIndex = 0;
		}
		else if (vcCount > 1) {
			//TODO multiple video capture devices available!
			/* User needs to select the appropriate video device to use in the
			 * gesture detection. Currently, default to the first index (0).
			 */
			logger.info("Multiple video capture devices found! Currently there are " + vcCount + " connected.");
			vcDeviceIndex = 0;
		}
		else {
			logger.error("No video capture devices found!");
			vcDeviceIndex = NO_DEVICE_FOUND;
			return;
		}
		
		vcDevice = new VideoCapture();
		if (!vcDevice.isOpened())
			vcDevice.open(vcDeviceIndex);
	}
	
	/**
	 * Returns the number of connected and available video capture devices.
	 * @return  the number of available video capture devices.
	 */
	private final int getVideoCaptureCount() {
		VideoCapture vc;
		int count = 0;
		while ((vc = new VideoCapture()).open(count)) {
			vc.release();
			count++;
		}
		return count;
	}
	
	
	@Override
	public boolean start() {
		if (currentState == State.STARTED) {
			stop();
		}
		
		logger.debug("Starting the GestureRecognizer...");
		if (vcDevice == null) {
			initialize();
		}
		
		if(!vcDevice.isOpened()) {
			fireStatusChanged(currentState, State.ERROR);
			logger.error("Default device not opened for video capture!");
			return false;
		}
		
		grProcessor = new GestureRecognizerWorker(frmUpdater, vcDevice, gestures);
		grProcessor.execute();
		
		fireStatusChanged(currentState, State.STARTED);
		return true;
	}
	
	
	@Override
	public void stop() {
		if (grProcessor != null) {
			grProcessor.cancel(true);
		}
		if (vcDevice != null) {
			vcDevice.release();
		}
		
		fireStatusChanged(currentState, State.PAUSED);
	}
	
	
	@Override
	public String toString() {
		return getClass().getSimpleName()
				+ "[device=" + vcDevice + ", gesture(s)='"
				+ Arrays.toString(getGestureNames())
				+ "']";
	}
	
	
	/**
	 * Returns an array of the names for all of the registered {@link Gesture}s.
	 * @return
	 */
	public String[] getGestureNames() {
		String[] tmp = new String[1];
		synchronized (gestures) {
			tmp = gestures.keySet().toArray(tmp);
		}
		return tmp;
	}
	
	
	@Override
	public final Gesture[] getGestures() {
		Gesture[] tmp = new Gesture[1];
		synchronized (gestures) {
			tmp = gestures.values().toArray(tmp);
		}
		return tmp;
	}
	

	/**
	 * Convienance method for fire a property change event for status state 
	 * changes and sets the current state to the new state specified.
	 * @param oldState
	 * @param newState
	 */
	protected final void fireStatusChanged(State oldState, State newState) {
		this.currentState = newState;
		firePropertyChanged(Property.STATUS.toString(), oldState, newState);
	}
	
	
	/**
	 * 
	 * @param propertyName
	 * @param oldValue
	 * @param newValue
	 */
	protected final void firePropertyChanged(String propertyName,
			Object oldValue, Object newValue)
	{
		if (propertyName == null || propertyName.isEmpty())
			return;
		
		final PropertyChangeEvent event =
				new PropertyChangeEvent(this, propertyName, oldValue, newValue);
		
		for (PropertyChangeListener pcl : listeners.getListeners(PropertyChangeListener.class))
			pcl.propertyChange(event);
	}
	
	
	@Override
	public void addPropertyChangeListener(PropertyChangeListener pcl) {
		if (pcl != null)
			listeners.add(PropertyChangeListener.class, pcl);
	}


	@Override
	public void removePropertyChangeListener(PropertyChangeListener pcl) {
		if (pcl != null)
			listeners.remove(PropertyChangeListener.class, pcl);
	}


	@Override
	public DeviceInfo getDeviceInfo() {
		if (deviceInfo == null)
			deviceInfo = new DeviceInfoImpl();
		
		return deviceInfo;
	}
	
	
	/**
	 * Provides an implementation of the {@link DeviceInfo} interface for use 
	 * as the return structure of {@link GestureRecognizerImpl#getDeviceInfo()}
	 * method.
	 * 
	 * @author Chris
	 */
	private final class DeviceInfoImpl implements DeviceInfo {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -3197157484231357192L;

		
		// Private member data.
		private final String uuid = UUID.randomUUID().toString();
		
		
		/**
		 * Returns a {@link Double} value of the video capture device property.
		 * @param propId
		 * @return
		 */
		private final Double getDeviceProperty(int propId) {
			return vcDevice != null ?
					new Double(vcDevice.get(propId)) : Double.NaN;
		}
		
		@Override
		public String getId() {
			return uuid;
		}
		
		@Override
		public int getIndex() {
			return vcDeviceIndex;
		}
		
		@Override
		public int getWidth() {
			return getDeviceProperty(Highgui.CV_CAP_PROP_FRAME_WIDTH).intValue();
		}

		@Override
		public int getHeight() {
			return getDeviceProperty(Highgui.CV_CAP_PROP_FRAME_HEIGHT).intValue();
		}

		@Override
		public double getFPS() {
			return grProcessor != null ? grProcessor.getFPS() : 0d;
		}

		@Override
		public long getTimestamp() {
			return vcImageTimestamp;
		}

		@Override
		public boolean isOpened() {
			return vcDevice != null && vcDevice.isOpened();
		}
		
	}
	
	
	/**
	 * Provides a video capture updater with the {@link BufferedImage} as the
	 * parameter type. This calls the fire property changed method and keeps a
	 * cache of the previous frame image.
	 * 
	 * @author Chris
	 */
	private final class VideoFrameUpdater implements Updater<BufferedImage> {
		
		// Private member data.
		private BufferedImage oldFrame = null;
		
		
		@Override
		public final void update(BufferedImage newFrame) {
			firePropertyChanged(Property.CAPTURE.toString(), oldFrame, newFrame);
			oldFrame = newFrame;
		}
		
	}

}
