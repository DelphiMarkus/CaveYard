package caveyard.quest;

import caveyard.assets.QuestsKey;
import caveyard.xml.quests.*;
import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetManager;

import javax.xml.bind.JAXB;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages all quests.
 *
 * @author Maximilian Timmerkamp
 */
public class QuestManager
{
	protected static final Logger LOGGER = Logger.getLogger(QuestManager.class.getName());
	protected static QuestManager instance;

	protected AssetManager assetManager;
	protected HashMap<String, Quest> quests;

	protected QuestManager(AssetManager assetManager)
	{
		this.assetManager = assetManager;

		quests = new HashMap<>();
	}

	public static QuestManager getInstance(AssetManager assetManager)
	{
		if (instance == null)
		{
			instance = new QuestManager(assetManager);
		}
		return instance;
	}

	public static QuestManager getInstance()
	{
		return instance;
	}

	protected Quest loadQuest(QuestType questType)
	{
		String questId = questType.getUid();
		int startState = questType.getStart();
		QuestStatus questStatus;
		String scriptFilename = questType.getInterface().getScript();
		String questInterface = questType.getInterface().getInterface();
		ArrayList<QuestVariable> variables;
		HashMap<Integer, State> states;

		if (LOGGER.isLoggable(Level.FINE))
		{
			LOGGER.fine("Loading Quest: \"" + questId + "\"");
		}

		String strQuestStatus = questType.getDefaultStatus().toLowerCase();
		switch (strQuestStatus)
		{
			case "inactive":
				questStatus = QuestStatus.INACTIVE;
				break;
			case "active":
				questStatus = QuestStatus.ACTIVE;
				break;
			case "finished":
				questStatus = QuestStatus.FINISHED;
				break;
			case "aborted":
				questStatus = QuestStatus.ABORTED;
				break;
			default:
				LOGGER.warning("Quest \"" + questId + "\": Unrecognizable default quest status \"" + strQuestStatus +
						"\". Setting to INACTIVE.");
				questStatus = QuestStatus.INACTIVE;
				break;
		}

		variables = new ArrayList<>(questType.getVariables().getVariable().size());
		for (VariableType variableType: questType.getVariables().getVariable())
		{
			String name = variableType.getName();
			QuestVariable.Type type;

			if (variableType.getType() == null)
			{
				LOGGER.info("Quest \"" + questId + "\": data type not set (at \"" + name + "\"): skipping.");
				continue;
			}
			else
			{
				switch (variableType.getType())
				{
					case STRING:
						type = QuestVariable.Type.STRING;
						break;
					case BOOLEAN:
						type = QuestVariable.Type.BOOLEAN;
						break;
					case INTEGER:
						type = QuestVariable.Type.INTEGER;
						break;
					case FLOAT:
						type = QuestVariable.Type.FLOAT;
						break;
					case DOUBLE:
						type = QuestVariable.Type.DOUBLE;
						break;
					case CUSTOM:
						type = QuestVariable.Type.CUSTOM;
						break;
					default:
						LOGGER.info("Quest \"" + questId + "\": unknown data type (at \"" + name + "\"): skipping.");
						continue;
				}
			}

			String defaultValue = variableType.getDefault();

			QuestVariable variable = new QuestVariable(name, type, defaultValue);
			variables.add(variable);
		}

		states = new HashMap<>(questType.getStates().getState().size());
		for (StateType stateType: questType.getStates().getState())
		{
			int id = stateType.getId();
			String interfaceName = stateType.getInterface();
			State state = new State(id, interfaceName);
			states.put(id, state);
		}

		return new Quest(questId, startState, questStatus, scriptFilename, questInterface, variables, states);
		//return null;
	}

	public void loadQuests(String file)
	{
		if (LOGGER.isLoggable(Level.FINE))
		{
			LOGGER.fine("Loading quest file: \"" + file + "\"");
		}

		AssetKey<QuestsType> key = new QuestsKey(file);
		AssetInfo info = assetManager.locateAsset(key);
		if (info == null)
		{
			LOGGER.warning("Error loading quest: quest file \"" + file + "\" not found.");
			return;
		}

		QuestsType questsType = JAXB.unmarshal(info.openStream(), QuestsType.class);

		// load all includes
		for (IncludeType includeType: questsType.getInclude())
		{
			loadQuests(includeType.getFile());
		}

		// load quests
		for (QuestType questType: questsType.getQuest())
		{
			Quest quest = loadQuest(questType);
			quests.put(quest.getQuestID(), quest);
		}
	}
}
