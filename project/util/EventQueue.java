/**
 * 
 */
package project.util;

import java.util.Comparator;
import java.util.PriorityQueue;

import project.recognition.event.GestureEvent;


/**
 * 
 * 
 * @author Chris Hartley
 *
 * @see java.util.PriorityQueue
 * @see project.recognition.event.GestureEvent
 */
public class EventQueue extends PriorityQueue<GestureEvent> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8981197398632195900L;
	
	/**
	 * 
	 */
	private static final int QUEUE_CAP = 11;

	
	/**
	 * 
	 */
	public EventQueue() {
		this(QUEUE_CAP);
	}
	
	
	/**
	 * 
	 * @param maxSize
	 */
	public EventQueue(int maxSize) {
		super(maxSize, new EventQueueComparator());
	}
	
	
	@Override
	public boolean add(GestureEvent ge) {
		if (size() == QUEUE_CAP)
			poll();
		
		return super.add(ge);
	}
	
	
	@Override
	public boolean offer(GestureEvent ge) {
		if (size() == QUEUE_CAP)
			poll();
		
		return super.offer(ge);
	}
	
	
	/**
	 * 
	 * 
	 * @author Chris Hartley
	 *
	 * @see java.util.Comparator
	 */
	private static final class EventQueueComparator
			implements Comparator<GestureEvent>
	{

		@Override
		public int compare(GestureEvent ge1, GestureEvent ge2) {
			long t1 = ge1 != null ? ge1.getWhen() : 0l;
			long t2 = ge2 != null ? ge2.getWhen() : 0l;
			
			return (int)(t1 - t2);
		}
		
	}
	
}
