package caveyard.quest;

import caveyard.quest.scriptinterface.QuestInterface;
import com.jme3.export.*;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;
import java.io.IOException;
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
	protected QuestStatus status;
	protected int currentStateId;

	protected ArrayList<QuestVariable> variables;
	protected QuestInterface questInterface;

	protected ScriptEngine engine;
	protected Bindings engineBindings;
	protected SimpleScriptContext scriptContext;

	public Quest(String questID, int startState, HashMap<Integer, State> states, ArrayList<QuestVariable> variables)
	{
		this.questID = questID;
		this.startState = startState;
		this.states = states;
		this.variables = variables;
	}

	public void changeState(int newStateId)
	{
		if (newStateId != currentStateId)
		{
			State currentState = getCurrentState();
			if (currentState != null)
			{
				currentState.deinitState();
				currentState.exitState();
			}

			currentStateId = newStateId;
			currentState = getCurrentState();
			currentState.enterState();
			currentState.initState();
		}
	}

	public State getCurrentState()
	{
		return states.get(currentStateId);
	}

	public void reset()
	{
		changeState(startState);
	}


	public void initQuest(ScriptEngine engine)
	{
		this.status = QuestStatus.INACTIVE;
		changeState(this.startState);

		this.engine = engine;
		engineBindings = engine.createBindings();
		scriptContext = new SimpleScriptContext();
		scriptContext.setBindings(engineBindings, SimpleScriptContext.ENGINE_SCOPE);

		initVariables();
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
				case INT:
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
				case INT:
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
