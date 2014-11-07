/**
 * 
 */
package handemapper.gui.translation;

import handemapper.common.recognition.event.GestureEvent;
import handemapper.common.recognition.event.GestureListener;
import handemapper.gui.util.EventQueue;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.util.Collection;
import java.util.Collections;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;



/**
 * This class provides a wrapper layer to the gesture recognizer which 
 * translates the detected gestures for both opened and closed hands into mouse
 * actions. The mouse actions are created through the use of the
 * {@link java.awt.Robot} class that allows low-level input overrides, 
 * specifically for the mouse in this instance. Thus, allowing hand gestures to
 * manipulate and simulate the mouse input device. 
 * 
 * @author Chris Hartley
 * 
 * @see java.awt.Robot
 * @see handemapper.common.recognition.event.GestureEvent
 * @see handemapper.common.recognition.event.GestureListener
 */
public class HandGestureRobot implements GestureListener {
	
	/** Static log4j logger instance for this class. */
	private static final Logger logger = LogManager.getLogger(HandGestureRobot.class);
	
	
	// Private member data.
	private final Robot gRobot;
	private boolean isHandOpened = true;
	private boolean isHandClosed = false;
	
	private boolean showFakeHandIcon = true;
	private boolean allowMouseOverride = false;
	private boolean allowMouseClicks = false;
	
	private final Collection<GestureEvent> eventQueue = 
			Collections.synchronizedCollection( new EventQueue() );
	
	
	/**
	 * Constructor for a new instance of this hand gesture robot.
	 */
	public HandGestureRobot() {
		Robot bot = null;
		
		try {
			bot = new Robot();
		}
		catch (AWTException e) {
			e.printStackTrace();
		}
		finally {
			gRobot = bot;
		}
	}


	/**
	 * @see handemapper.common.recognition.event.GestureListener#gestureDetected(GestureEvent)
	 */
	@Override
	public void gestureDetected(GestureEvent ge) {
		if (ge == null)
			return;
		
		synchronized (eventQueue) {
			try {
				if (eventQueue.add(ge)) {
					switch (ge.getID()) {
					case (GestureEvent.OPENED_HAND_DETECTED):
						openedHandDetected(ge);
						break;
					case (GestureEvent.CLOSED_HAND_DETECTED):
						closedHandDetected(ge);
						break;
					default:
						logger.error("Unknown gesture event id: " + ge.getID());
					}
				}
			}
			catch (Exception ignore) { }
		}
	}
	
	
	
	/**
	 * This method is called when an opened hand was detected by the
	 * gesture recognizer. This will also update the mouse pointer location and
	 * then determine if a drop action has occurred.
	 * 
	 * @param gesture	the {@link GestureEvent} responsible for this call.
	 */
	private final void openedHandDetected(GestureEvent gesture) {
		// Move mouse point location...
		setMouseLocation(gesture);
		
		if (isHandClosed) {
			// Thus; hand closed -> hand opened => drop action
			isHandClosed = false;
			isHandOpened = true;
			doDropGesture(gesture);
		}
	}
	
	
	/**
	 * This method is called when a closed hand was detected by the
	 * gesture recognizer. This will also update the mouse pointer location and
	 * then determine if a grab action has occurred.
	 * 
	 * @param gesture	the {@link GestureEvent} responsible for this call.
	 */
	private final void closedHandDetected(GestureEvent gesture) {
		// Move mouse point location...
		setMouseLocation(gesture);
		
		if (isHandOpened) {
			// Thus; hand opened -> hand closed => grab action
			isHandClosed = true;
			isHandOpened = false;
			doGrabGesture(gesture);
		}
	}
	
	
	/**
	 * <p>Based on the specified {@link GestureEvent} , this will move the mouse
	 * pointer location to the corresponding gesture location on the screen.</p>
	 * 
	 * <p><strong>Note:</strong> At this point the location specified by the 
	 * {@link GestureEvent} object should already be converted to the screen 
	 * coordinate system and not the location within the video capture image.
	 * </p>
	 * 
	 * @param gesture	the {@link GestureEvent} responsible for this call.
	 * 
	 * @see java.awt.Robot#mouseMove(int, int)
	 */
	private final void setMouseLocation(GestureEvent gesture) {
		if (gRobot != null && allowMouseOverride)
			gRobot.mouseMove(gesture.getX(), gesture.getY());
		else if (showFakeHandIcon)
			;
	}
	
	
	/**
	 * Method to implement a grab action based on the detected gestures. This
	 * simulates the mouse button being pressed, {@link Robot#mousePress(int)}
	 * with the parameter of {@link InputEvent#BUTTON1_DOWN_MASK}.
	 * 
	 * @param gesture	the {@link GestureEvent} responsible for this call.
	 * 
	 * @see java.awt.Robot#mousePress(int)
	 * @see java.awt.event.InputEvent#BUTTON1_DOWN_MASK
	 */
	protected final void doGrabGesture(GestureEvent gesture) {
		if (gRobot != null && allowMouseOverride && allowMouseClicks)
			gRobot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
	}
	
	
	/**
	 * Method to implement a drop action based on the detected gestures. This
	 * simulates the mouse button being released, 
	 * {@link Robot#mouseRelease(int)} with the parameter of 
	 * {@link InputEvent#BUTTON1_DOWN_MASK}.
	 * 
	 * @param gesture	the {@link GestureEvent} responsible for this call.
	 * 
	 * @see java.awt.Robot#mouseRelease(int)
	 * @see java.awt.event.InputEvent#BUTTON1_DOWN_MASK
	 */
	protected final void doDropGesture(GestureEvent gesture) {
		if (gRobot != null && allowMouseOverride && allowMouseClicks)
			gRobot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
	}
	
	
	

}
