/**
 * 
 */
package handemapper.common.recognition;

import handemapper.common.recognition.event.GestureListener;

import java.io.Serializable;

import org.opencv.core.Mat;


/**
 * 
 * @author Chris Hartley
 */
public interface Gesture extends Serializable {
	
	
	/**
	 * Returns the name for this particular instance of the gesture as a
	 * {@link String}.
	 * 
	 * @return	the name for this instance of the gesture.
	 */
	public String getName();
	
	/**
	 * Sets the name for this instance of the gesture with the specified name
	 * parameter.
	 *  
	 * @param name	The new name for this instance of the gesture.
	 */
	public void setName(String name);
	
	/**
	 * Returns the description for this particular instance of the gesture as a
	 * {@link String}. If no description has been set for the instance, a
	 * zero-length {@link String} is returned.
	 * 
	 * @return	the description for this instance of the gesture.
	 */
	public String getDescription();
	
	/**
	 * Sets the description for this instance of the gesture with the specified
	 * description parameter. If the parameter is {@code null}, the description
	 * will be set to a zero-length {@link String}.
	 * 
	 * @param description	The detailed description for this instance of the
	 * 						gesture.
	 */
	public void setDescription(String desc);
	
	/**
	 * This method sets a flag indicating that the gesture implementation should
	 * initialize or re-initialize the abstract gesture before proceeding with
	 * the detection method. The implementation should use the
	 * {@link #needsInitializing()} method to determine if initializing is
	 * required and if so, take the appropriate sets.
	 * <p>Example:
	 * <p><code>
	 *<pre>public class GestureX extends AbstractGesture {
	 *
	 *	public void detect(final Mat matrix) {
	 *		// Parse and detect any gestures...
	 *		...
	 *
	 *		// If none were detected, re-initialize...
	 *		if (nothingDetected)
	 *			initialize();
	 *	}
	 *}</pre>
	 * </code>
	 * 
	 * @see #initialized()
	 * @see #needsInitializing()
	 */
	public void initialize();
	
	
	/**
	 * <p>Primary means for this implementation of the gesture detection. The 
	 * matrix parameter is a {@link org.opencv.core.Mat} which should not be
	 * modified! If there are required modification to the image data, make a
	 * copy and use the copy to manipulate the data. Otherwise, any changes to
	 * the original image data will be reflected in the publishing of the image
	 * from within the {@link handemapper.common.recognition.GestureRecognizer}
	 * 
	 * @param matrix	The matrix of image data captured from the connected web
	 * 					camera through the OpenCV interface.
	 * 
	 * @see org.opencv.core.Mat
	 * @see handemapper.common.recognition.GestureRecognizer
	 */
	public void detect(final Mat matrix);
	
	
	/**
	 * Returns {@code true} if this instance of the gesture is currently
	 * enabled; otherwise, returns {@code false}.
	 * 
	 * @return	{@code true} if the gesture is enabled; otherwise, {@code false}
	 */
	public boolean isEnabled();
	
	
	/**
	 * Set this instance of the gesture to be enabled or disabled based on the
	 * parameter specified.
	 * 
	 * @param enabled	whether this gesture is enabled or not.
	 */
	public void setEnabled(boolean enabled);
	
	
	/**
	 * Registers the specified {@link GestureListener} to this instance of the
	 * gesture for notification when the gesture has been detected from the
	 * {@link #detect(Mat)} method.
	 * 
	 * @param gl	The new {@link GestureListener} to register.
	 * 
	 * @see handemapper.common.recognition.event.GestureListener
	 * @see javax.swing.event.EventListenerList#add(Class, java.util.EventListener)
	 */
	public void addGestureListener(GestureListener gl);
	
	
	/**
	 * Removes the specified {@link GestureListener} from the registered 
	 * listener for this instance of the gesture. 
	 * 
	 * @param gl
	 * 
	 * @see handemapper.common.recognition.event.GestureListener
	 * @see javax.swing.event.EventListenerList#remove(Class, java.util.EventListener)
	 */
	public void removeGestureListener(GestureListener gl);
	
}
