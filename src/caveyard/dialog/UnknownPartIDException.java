package caveyard.dialog;

/**
 * This Exceptions is thrown when a dialog part id was used in the dialog
 * to reference another part but this other part could not be found.
 * This usually happens if the id was misspelled.
 *
 * @author Maximilian Timmerkamp
 */
public class UnknownPartIDException extends RuntimeException
{
	public UnknownPartIDException(String msg)
	{
		super(msg);
	}

	public UnknownPartIDException(String msg, Throwable e)
	{
		super(msg, e);
	}

	public UnknownPartIDException(Throwable e)
	{
		super(e);
	}
}
