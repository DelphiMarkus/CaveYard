package caveyard.script;

import caveyard.assets.ScriptKey;
import com.jme3.asset.AssetManager;

/**
 * @author Maximilian Timmerkamp
 */
public class ScriptManager
{
	private static ScriptManager instance;

	protected AssetManager assetManager;
	//protected String scriptPath = "Scripts/";

	private ScriptManager(AssetManager assetManager)
	{
		this.assetManager = assetManager;
	}

	public static ScriptManager getInstance(AssetManager assetManager)
	{
		if (instance == null)
		{
			instance = new ScriptManager(assetManager);
		}
		return instance;
	}

	public static ScriptManager getInstance()
	{
		return instance;
	}

	/***
	 * Loads a script from Assets folder "Scripts/".
	 *
	 * @param name Name of the script to load.
	 * @return Contents of script as String or null.
	 */
	public String loadScript(String name)
	{
		//name = scriptPath + name;
		return assetManager.loadAsset(new ScriptKey(name));
	}


}
