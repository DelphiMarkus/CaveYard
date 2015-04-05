package caveyard.util;

/**
 * This exception if thrown if {@link caveyard.util.Mergeable}s cannot merge
 * into a new object.
 *
 * @author Maximilian Timmerkamp
 */
public class MergeImpossibleException extends RuntimeException
{
	public MergeImpossibleException(String msg)
	{
		super(msg);
	}

	public MergeImpossibleException(String msg, Throwable throwable)
	{
		super(msg, throwable);
	}
}
