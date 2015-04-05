package caveyard.util;

/**
 * A simple interface to enable merging objects.
 *
 * @author Maximilian Timmerkamp
 */
public interface Mergeable<T>
{

	/**
	 * Merges this object and the passed object <code>other</code> into one
	 * object.
	 * @param other Object to merge with this object.
	 * @return Merged object.
	 * @throws MergeImpossibleException Throws an exception if the objects cannot be merged.
	 */
	T merge(T other) throws MergeImpossibleException;
}
