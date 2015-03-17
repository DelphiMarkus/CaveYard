package caveyard.script;

import caveyard.assets.ScriptKey;
import com.jme3.asset.AssetManager;

/**
 *
 * @author Maximilian Timmerkamp
 */
public class ScriptManager
{
    protected AssetManager assetManager;
    protected String scriptPath = "Scripts/";
    
    public ScriptManager(AssetManager assetManager)
    {
        this.assetManager = assetManager;
    }
    
    
    /***
     * Loads a script from Assets folder "Scripts/".
     * @param name Name of the script to load.
     * @return Contents of script as String or null.
     */
    public String loadScript(String name)
    {
        
        name = scriptPath + name;
        return assetManager.loadAsset(new ScriptKey(name));
    }
    
    
}
