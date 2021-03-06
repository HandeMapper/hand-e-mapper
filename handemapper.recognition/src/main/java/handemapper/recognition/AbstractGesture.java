/**
 * <h1>Hand-e-Mapper</h1>
 * Hand Gesture Detection with OpenCV<br />
 * Group Project - CSC 484, Winter Quarter 2014
 *               & CSC 581, Spring Quarter 2014<br />
 * California Polytechnic State University, SLO
 * 
 * <h2>Team members:</h2><ul>
 * <li>Chris Hartley (cnhartle@calpoly.edu)</li>
 * <li>Adin Miller   (amille@calpoly.edu)</li>
 * <li>Haikal Saliba (observer)</li>
 * </ul>
 * <h2>Collaboration with:</h2><ul>
 * <li>Carmen Badea, Intel Corp. (carmen.t.badea@intel.com)</li>
 * </ul>
 * <h2>Revisions:</h2><ol>
 * <li>05/22/2014 - chartley - Added feature of initializing which includes both
 *							methods {@link #initialize()} and
 *							{@link #needsInitializing()}.</li>
 * </ol>
 */
package handemapper.recognition;

import java.awt.Color;
import java.util.UUID;

import javax.swing.event.EventListenerList;

import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import handemapper.common.recognition.Gesture;
import handemapper.common.recognition.event.GestureEvent;
import handemapper.common.recognition.event.GestureListener;

/**
 * <p>This provides an abstract class for any detectable gesture based on the
 * video capture image matrix returned from the OpenCV interface.
 * <p>This abstract class is required in order for the {@link GestureRecognizer}
 * to call its {@code detect(Mat)} method where the specific detection is
 * implemented.
 * <p>Example:
 * <p><code>
 *<pre>public class GestureX extends AbstractGesture {
 *
 *	private static final SOME_GESTURE_ID = GestureEvent.OPENED_HAND_DETECTED;
 *	                                    // GestureEvent.CLOSED_HAND_DETECTED;
 *
 *	public GestureX() {
 *		super("Gesture X", "...", true);
 *		addGestureListener( ... );  // some GestureListener
 *	}
 *
 *	public void detect(Mat mat) {
 *		boolean hasDetected = false;
 *		Point centerPoint = null;
 *
 *		// Verify that this instance has been initialized...
 *		if (needsInitializing()) {
 *			someFunctionToInitialize();
 *			initialized();
 *		}
 *
 *		// 1. Process the image matrix...
 *
 *		// 2. Update hasDetected to true if found and set the centerPoint...
 *
 *		if (hasDetected)
 *			fireGestureDetected(SOME_GESTURE_ID, centerPoint.x, centerPoint.y);
 *		else
 *			initialize();
 *	}
 *}</pre>
 * </code>
 * 
 * @author Chris Hartley
 * @author Adin Miller
 */
public abstract class AbstractGesture implements Gesture {

	/*
	 * The unique serial version user interface identification number.
	 */
	private static final long serialVersionUID = -8866579913710676820L;
	
	/*
	 * The event listener list containing all registered event listeners for
	 * this instance of the gesture.
	 */
	private final EventListenerList listenerList = new EventListenerList();

	
	// Member data.
	private boolean requireInitialization = true;
	private boolean enabled = true;
	private String name = null;
	private String desc = "";
	
	
	/**
	 * Constructor for a new instance of this gesture with the name set to a 
	 * universally unique identifier (UUID) string.
	 */
	public AbstractGesture() {
		this(UUID.randomUUID().toString(), null, true);
	}
	
	
	/**
	 * Constructor for a new instance of this gesture with the specified name
	 * and set to have no description and the default of enabled.
	 * 
	 * @param name	The {@link String} name for this gesture.
	 */
	public AbstractGesture(String name) {
		this(name, null, true);
	}
	
	
	/**
	 * Constructor for a new instance of this gesture with the specified name
	 * and whether the gesture is enabled or not. This does not have a 
	 * description associated by default.
	 * 
	 * @param name		The {@link String} name for this gesture.
	 * @param enabled	The {@code boolean} value indicating if the gesture is
	 * 					enabled, {@code true} or not {@code false}.
	 */
	public AbstractGesture(String name, boolean enabled) {
		this(name, null, enabled);
	}
	
	
	/**
	 * Constructor for a new instance of this gesture with the specified name
	 * and description. By default, this new instance will be enabled.
	 * 
	 * @param name		The {@link String} name for this gesture.
	 * @param desc		The {@link String} description for this gesture.
	 */
	public AbstractGesture(String name, String desc) {
		this(name, desc, true);
	}
	
	
	/**
	 * Constructor for a new instance of this gesture with the specified name,
	 * description, and whether the gesture is enabled or not.
	 * 
	 * @param name		The {@link String} name for this gesture.
	 * @param desc		The {@link String} description for this gesture.
	 * @param enabled	The {@code boolean} value indicating if the gesture is
	 * 					enabled, {@code true} or not {@code false}.
	 */
	public AbstractGesture(String name, String desc, boolean enabled) {
		setName(name);
		setDescription(desc);
		setEnabled(enabled);
		initialize();
	}
	
	
	@Override
	public final void initialize() {
		requireInitialization = true;
	}
	
	
	/**
	 * Indicates that the initialization has completed successfully. After 
	 * calling this method, the {@link #needsInitializing()} should return
	 * {@code false}; until the {@link #initialize()} method is called. Refer
	 * to the {@link #needsInitializing()} for an example.
	 * 
	 * @see #initialize()
	 * @see #needsInitializing()
	 */
	protected final void initialized() {
		requireInitialization = false;
	}
	
	
	/**
	 * Returns {@code true} if this instance of the gesture requires
	 * initialization before proceeding with any detection; otherwise, returns
	 * {@code false}.
	 * <p>Example:
	 * <p><code>
	 *<pre>public class GestureX extends AbstractGesture {
	 *
	 *	public void detect(final Mat matrix) {
	 *		// Verify that this instance has been initialized...
	 *		if (needsInitializing()) {
	 *			someFunctionToInitialize();
	 *			initialized();
	 *		}
	 *			
	 *		// Parse and detect any gestures...
	 *		...
	 *	}
	 *}</pre>
	 * </code>
	 * 
	 * @return	{@code true} if the implementation requires initializing;
	 * 			otherwise, {@code false}.
	 * 
	 * @see #initialize()
	 * @see #initialized()
	 */
	protected final boolean needsInitializing() {
		return requireInitialization;
	}
	
	
	@Override
	public boolean isEnabled() {
		return enabled;
	}
	
	
	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	
	/**
	 * <p>If the implementation detects the appropriate object(s), use the
	 * {@link #fireGestureDetected(int, int, int)} method to notify the
	 * registered {@link handemapper.recognition.event.GestureListener}s.
	 * 
	 * @see #fireGestureDetected(int, int, int)
	 */
	@Override
	abstract public void detect(final Mat matrix);
	
	
	@Override
	public String getName() {
		return name;
	}
	
	
	@Override
	public void setName(String name) {
		this.name = name;
	}
	
	
	@Override
	public String getDescription() {
		return desc;
	}
	
	
	@Override
	public void setDescription(String description) {
		this.desc = description == null ? "" : description;
	}
	
	
	@Override
	public final void addGestureListener(GestureListener gl) {
		listenerList.add(GestureListener.class, gl);
	}
	
	
	@Override
	public final void removeGestureListener(GestureListener gl) {
		listenerList.remove(GestureListener.class, gl);
	}
	
	
	/**
	 * This creates a new {@link GestureEvent} based on the specified parameters
	 * and notifies all of the registered {@link GestureListener}s by calling
	 * {@link #notifyGestureListeners(GestureEvent)}.
	 * 
	 * @param id	The unique identifier for the detection type.
	 * @param x 	The center X-coordinate for the focus gesture detected
	 * @param y 	The center Y-coordinate for the focus gesture detected
	 * 
	 * @see project.recognition.event.GestureEvent
	 */
	protected synchronized final void fireGestureDetected(int id, int x, int y)
	{
		GestureEvent ge = new GestureEvent(this, id,
				System.currentTimeMillis(), x, y);
		
		notifyGestureListeners(ge);
	}
	
	
	/**
	 * Notifies all registered {@link GestureListener}s of this instance of the
	 * gesture with the specified {@link GestureEvent}. This notification is 
	 * handled in a separate thread.
	 * 
	 * @param ge	The new gesture event to notify all appropriate registered
	 * 				listeners with.
	 * 
	 * @see project.recognition.event.GestureEvent
	 */
	private final void notifyGestureListeners(final GestureEvent ge) {
		if (ge == null)
			return;
		
		final GestureListener[] listeners =
				listenerList.getListeners(GestureListener.class);
		
		new Thread( new Runnable() {

			@Override
			public void run() {
				for (GestureListener gl : listeners)
					gl.gestureDetected(ge);
			}
			
		} ).start();
	}
	
	
	/**
	 * Converts the {@link java.awt.Color} to the OpenCV
	 * {@link org.opencv.core.Scalar} value for the color.
	 * 
	 * @param color	the {@link Color} to convert to the {@link Scalar}.
	 * 
	 * @return	the converted {@link Scalar} value for the specified
	 * 			{@link Color}.
	 */
	protected static final Scalar convertColorToScalar(Color color) {
		return color != null ? new Scalar(color.getBlue(), color.getGreen(),
				color.getRed(), color.getAlpha()) : new Scalar(0, 0, 0, 0);
	}
	
	
	/**
	 * Converts the OpenCV {@link org.opencv.core.Scalar} value to the
	 * {@link java.awt.Color} equivalent.
	 * 
	 * @param scalar	the {@link Scalar} value to convert to the {@link Color}
	 * 
	 * @return	the converted {@link Color} for the specified {@link Scalar}
	 * 			value.
	 */
	protected static final Color convertScalarToColor(Scalar scalar) {
		return scalar != null ? new Color((float)scalar.val[0],
				(float)scalar.val[1], (float)scalar.val[2], 
				(float)scalar.val[3]) : new Color(0, 0, 0, 0);
	}
	
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "[name='" + getName() + "',"
				+ "enabled=" + isEnabled() + "]";
	}

}
