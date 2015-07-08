package caveyard.quest.scriptinterface;

/**
 * Simple interface which allows reading some data types from a savegame. Used for quest scripts to store data.
 */
public interface Loader
{
	int loadInt();
	float loadFloat();
	double loadDouble();
	String loadString();

	Loader getCustomLoader();
}
