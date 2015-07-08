package caveyard.quest;

import caveyard.quest.scriptinterface.StateInterface;

/**
 * @author Maximilian Timmerkamp
 */
public class State
{
	protected int stateID;
	protected String interfaceName;

	protected StateInterface stateInterface;

	public State(int stateID, String interfaceName)
	{
		this.stateID = stateID;
		this.interfaceName = interfaceName;
	}

	public State(int stateID)
	{
		this.stateID = stateID;
		this.interfaceName = null;
	}

	public void enterState()
	{}

	public void initState()
	{}

	public void stateCallback()
	{}

	public void deinitState()
	{}

	public void exitState()
	{}
}
