package caveyard.quest.scriptinterface;

import caveyard.quest.Quest;
import com.jme3.export.InputCapsule;
import com.jme3.export.OutputCapsule;

/**
 * @author Maximilian Timmerkamp
 */
public interface QuestInterface
{
	/***
	 * initializes quest (e.g. init additional variables)
	 */
	void initQuest();


	/***
	 * called to save the quest's state (only "custom" variables must be
	 * saved, all others are saved be the QuestSystem)
	 *
	 * @param in JmeImporter used to load quest state.
	 */
	void loadQuest(InputCapsule in);

	/***
	 * load saved quest state (again: only need to load "custom" data)
	 *
	 * @param out JmeExporter used to save quest state.
	 */
	void saveQuest(OutputCapsule out);


	/***
	 * If a state defines no StateInterface the quest interface is asked to return an
	 * interface which can be used to interface with the state.
	 * @param stateID id of the state
	 * @return a state interface for the desired state
	 *
	 * @see StateInterface
	 */
	StateInterface getStateInterface(int stateID);


	/***
	 * Get the quest object which controls this interface. Can be used to change the state
	 * and install Listeners.
	 *
	 * @return the quest object controling this interface.
	 */
	Quest getQuest();
}
