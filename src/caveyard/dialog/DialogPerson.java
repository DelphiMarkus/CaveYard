package caveyard.dialog;

/**
 * A very simple class to link internal dialog names with external names in the 3d world.
 *
 * @author Maximilian Timmerkamp
 */
public class DialogPerson
{
	protected String internalName;
	protected String externalName;

	public DialogPerson(String intName, String extName)
	{
		this.internalName = intName;
		this.externalName = extName;
	}

	public String getExternalName()
	{
		return externalName;
	}

	public String getInternalName()
	{
		return internalName;
	}
}
