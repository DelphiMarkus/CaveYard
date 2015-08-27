package caveyard.quest;

import caveyard.quest.scriptinterface.QuestInterface;
import com.jme3.export.*;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * @author Maximilian Timmerkamp
 */
public class Quest implements Savable
{
	protected final static Logger LOGGER = Logger.getLogger(Quest.class.getName());

	protected String questID;
	protected HashMap<Integer, State> states;

	protected int startState;
	protected QuestStatus defaultStatus;
	protected QuestStatus status = QuestStatus.ABORTED;
	protected int currentStateId;

	protected ArrayList<QuestVariable> variables;

	protected String scriptFilename;
	protected String questInterfaceName;
	protected QuestInterface questInterface;

	protected ScriptEngine engine;
	protected Bindings engineBindings;
	protected SimpleScriptContext scriptContext;

	public Quest(String questID, int startState, QuestStatus defaultStatus, String scriptFilename,
	             String questInterfaceName, ArrayList<QuestVariable> variables, HashMap<Integer, State> states)
	{
		this.questID = questID;
		this.startState = startState;
		this.defaultStatus = defaultStatus;
		this.scriptFilename = scriptFilename;
		this.questInterfaceName = questInterfaceName;
		this.states = states;
		this.variables = variables;
	}

	public String getQuestID()
	{
		return questID;
	}

	public QuestInterface getQuestInterface()
	{
		return questInterface;
	}

	public int getStateId()
	{
		return currentStateId;
	}

	public void changeStateId(int newStateId)
	{
		if (newStateId != currentStateId)
		{
			State currentState = getCurrentState();
			if (currentState != null)
			{
				if (status == QuestStatus.ACTIVE)
				{
					currentState.deinitState();
				}
				if (status == QuestStatus.ACTIVE || status == QuestStatus.INACTIVE)
				{
					currentState.exitState();
				}
			}

			currentStateId = newStateId;
			currentState = getCurrentState();
			if (status == QuestStatus.ACTIVE)
			{
				currentState.enterState();
			}
			if (status == QuestStatus.ACTIVE || status == QuestStatus.INACTIVE)
			{
				currentState.initState();
			}
		}
	}

	public State getCurrentState()
	{
		return states.get(currentStateId);
	}

	public void reset()
	{
		changeStateId(startState);
	}


	public void initQuest(ScriptEngine engine)
	{
		this.engine = engine;
		engineBindings = engine.createBindings();
		scriptContext = new SimpleScriptContext();
		scriptContext.setBindings(engineBindings, SimpleScriptContext.ENGINE_SCOPE);

		try
		{
			Reader reader = new InputStreamReader(new FileInputStream(scriptFilename));
			engine.eval(reader, scriptContext);
		}
		catch (FileNotFoundException e)
		{
			LOGGER.severe("Cannot init quest \"" + questID + "\": script file \"" + scriptFilename + "\" not found.");
			LOGGER.throwing("Quest", "initQuest", e);
			return;
		}
		catch (ScriptException e)
		{
			LOGGER.severe("Cannot init quest \"" + questID + "\": script file \"" + scriptFilename +
					"\" cannot be evaluated.");
			LOGGER.throwing("Quest", "initQuest", e);
			return;
		}

		try
		{
			questInterface = (QuestInterface) engineBindings.get(questInterfaceName);
		}
		catch (ClassCastException e)
		{
			LOGGER.severe("Cannot init quest \"" + questID + "\": Quest interface does not implement QuestInterface.");
			LOGGER.throwing("Quest", "initQuest", e);
			return;
		}

		initVariables();

		questInterface.setQuest(this);

		this.status = defaultStatus;
		changeStateId(this.startState);

	}

	protected void initVariables()
	{
		for (QuestVariable var: variables)
		{
			Object value = null;
			if (var.getDefaultValue() != null && var.getDefaultValue().length() > 0)
			{
				try
				{
					value = engine.eval(var.getDefaultValue(), scriptContext);
				} catch (ScriptException e)
				{
					LOGGER.warning("Quest \"" + questID + "\": Cannot evaluate default value for variable \"" +
							var.getName() + "\".");
					LOGGER.throwing("Quest", "initVariables", e);
				}
			}

			engineBindings.put(var.getName(), value);
		}
	}

	public Object getVariableValue(String name)
	{
		return engineBindings.get(name);
	}

	public QuestStatus getStatus()
	{
		return this.status;
	}

	public void setStatus(QuestStatus status)
	{
		switch (this.status)
		{
			case ACTIVE:
				switch (status)
				{
					case INACTIVE:
					case FINISHED:
					case ABORTED:
						getCurrentState().deinitState();
						break;
				}
				// FALL-TROUGH
			case INACTIVE:
				switch (status)
				{
					case ACTIVE:
						getCurrentState().initState();
						break;
					case FINISHED:
					case ABORTED:
						getCurrentState().deinitState();
						break;
				}
				break;
			case FINISHED:
			case ABORTED:
				switch (status)
				{
					case INACTIVE:
						getCurrentState().enterState();
						break;
					case ACTIVE:
						getCurrentState().enterState();
						getCurrentState().initState();
						break;
				}
				break;
		}
		this.status = status;
	}

	protected boolean saveQuestVariable(QuestVariable var, OutputCapsule capsule)
	{
		Object value = engineBindings.get(var.getName());

		try
		{
			switch (var.getType())
			{
				case BOOLEAN:
					capsule.write((Boolean) value, var.getName(), false);
					break;
				case INTEGER:
					capsule.write((Integer) value, var.getName(), 0);
					break;
				case FLOAT:
					capsule.write((Float) value, var.getName(), 0.0);
					break;
				case DOUBLE:
					capsule.write((Double) value, var.getName(), 0.0);
					break;
				case STRING:
					capsule.write((String) value, var.getName(), "");
					break;
				case CUSTOM:
					return false;
			}
		}
		catch (IOException e)
		{
			LOGGER.severe("Quest \"" + questID + "\": Cannot save variable \"" + var.getName() + "\": IOError.");
			LOGGER.throwing("Quest", "saveQuestVariable", e);
			return false;
		}
		catch (ClassCastException e)
		{
			LOGGER.severe("Quest \"" + questID + "\": Cannot save variable \"" + var.getName() +
					"\": variable has unexpected type.");
			LOGGER.throwing("Quest", "saveQuestVariable", e);
			return false;
		}
		return true;
	}

	protected boolean loadQuestVariable(QuestVariable var, InputCapsule capsule)
	{
		Object value = null;

		try
		{
			switch (var.getType())
			{
				case BOOLEAN:
					value = capsule.readBoolean(var.getName(), false);
					break;
				case INTEGER:
					value = capsule.readInt(var.getName(), 0);
					break;
				case FLOAT:
					value = capsule.readFloat(var.getName(), 0.0f);
					break;
				case DOUBLE:
					value = capsule.readDouble(var.getName(), 0.0);
					break;
				case STRING:
					value = capsule.readString(var.getName(), "");
					break;
				case CUSTOM:
					return false;
			}
		}
		catch (IOException e)
		{
			LOGGER.severe("Quest \"" + questID + "\": Cannot save variable \"" + var.getName() + "\": IOError.");
			LOGGER.throwing("Quest", "saveQuestVariable", e);
			return false;
		}
		catch (ClassCastException e)
		{
			LOGGER.severe("Quest \"" + questID + "\": Cannot save variable \"" + var.getName() +
					"\": variable has unexpected type.");
			LOGGER.throwing("Quest", "saveQuestVariable", e);
			return false;
		}

		engineBindings.put(var.getName(), value);

		return true;
	}

	/***
	 * Loads all quest variables defining the quest state.
	 * @param capsule capsule to write data to.
	 */
	protected void loadVariables(InputCapsule capsule)
	{
		for (QuestVariable var: variables)
		{
			if (var.getType() != QuestVariable.Type.CUSTOM)
			{
				loadQuestVariable(var, capsule);
			}
		}

		questInterface.loadQuest(capsule);
	}

	/***
	 * Saves all quest variables defining the quest state to capsule.
	 * @param capsule
	 */
	protected void saveVariables(OutputCapsule capsule)
	{
		for (QuestVariable var: variables)
		{
			if (var.getType() != QuestVariable.Type.CUSTOM)
			{
				saveQuestVariable(var, capsule);
			}
		}

		questInterface.saveQuest(capsule);
	}

	@Override
	public void write(JmeExporter ex) throws IOException
	{
		State currentState = getCurrentState();
		currentState.deinitState();

		OutputCapsule capsule = ex.getCapsule(this);

		// save quest status
		capsule.write(status.ordinal(), "_quesStatus", QuestStatus.INACTIVE.ordinal());
		capsule.write(currentStateId, "_currentStateId", startState);

		// save quest variables
		saveVariables(capsule);
	}

	@Override
	public void read(JmeImporter im) throws IOException
	{
		InputCapsule capsule = im.getCapsule(this);

		// read quest status
		int questStatus = capsule.readInt("_questStatus", QuestStatus.INACTIVE.ordinal());
		this.status = QuestStatus.values()[questStatus];
		this.currentStateId = capsule.readInt("_currentStateId", startState);

		loadVariables(capsule);

		State currentState = getCurrentState();
		currentState.initState();
	}
}
