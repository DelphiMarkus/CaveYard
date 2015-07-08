package caveyard.dialog;

/**
 * A simple class which specifies which text is said by which person.
 */
public class DialogText
{
	protected String text;
	protected DialogPerson speaker;

	public DialogText(String text, DialogPerson speaker)
	{
		this.speaker = speaker;
		this.text = text;
	}

	public String getText()
	{
		return text;
	}

	public DialogPerson getSpeaker()
	{
		return speaker;
	}
}
