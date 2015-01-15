/**
 * 
 */
package handemapper.recognition.utils;


/**
 * Provides an easy generic callback for setting internal values.
 * @author Chris
 *
 * @param <T>
 */
public interface Updater<T> {

	/**
	 * Updates a value with the specified new value.
	 * @param newValue
	 */
	public void update(T newValue);
	
}
