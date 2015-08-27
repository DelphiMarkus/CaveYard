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
	{
		stateInterface.enterState();
	}

	public void initState()
	{
		stateInterface.initState();
	}

	public void stateCallback()
	{
		stateInterface.callback();
	}

	public void deinitState()
	{
		stateInterface.deinitState();
	}

	public void exitState()
	{
		stateInterface.exitState();
	}
}
