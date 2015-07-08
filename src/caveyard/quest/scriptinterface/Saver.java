package caveyard.quest.scriptinterface;

/**
 * Simple interface to save data to a savegame. Used to save quest data.
 *
 * @author Maximilian Timmerkamp
 */
public interface Saver
{
	void saveInt(int value);
	void saveFloat(float value);
	void saveDouble(double value);
	void saveString(String value);

	Saver getCustomSaver();
}
