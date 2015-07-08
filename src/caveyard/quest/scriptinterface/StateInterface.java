package caveyard.quest.scriptinterface;

/**
 * @author Maximilian Timmerkamp
 */
public interface StateInterface
{
	/***
	 * Called when entering this state. Changes the internal quest state.
	  * Usually only called once in a whole game.
	 */
	void enterState();

	/***
	 * Called when state was entered to initialize e.g. listeners.
	 * Everything which interfaces with the current game state must be initialized here.
	 * This method is called after loading a quest state from a savegame. Therefore the
	 * state is not "entered" again but only initialized.
	 */
	void initState();

	/***
	 * Called when a listener was triggered. Can only be triggered after entering
	 * and initializing the state.
	 */
	void callback(); //TODO: pass a parameter what called the callback

	/***
	 * Called when a state is going to be exited or the quest is going to be
	 * deactivated e.g. by exiting the game. Save any unsaved status by writing into
	 * some quest variables or set a "dirty" flag to save any additional data in a call
	 * to "saveQuest".
	 */
	void deinitState();

	/***
	 * Called when a state is finally exited. E.g. when the next state is going to be entered.
	 */
	void exitState();
}
