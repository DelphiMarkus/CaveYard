package caveyard.map;

import caveyard.map.xml.MapType;
import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetManager;

import javax.xml.bind.JAXB;

/**
 *
 * @author Maximilian Timmerkamp
 */
public class MapManager
{
	protected AssetManager assetManager;

	public MapManager(AssetManager assetManager)
	{
		this.assetManager = assetManager;
	}

    public Map loadMap(String filename)
    {
		AssetKey<MapType> key = new AssetKey<>(filename);
		AssetInfo info = assetManager.locateAsset(key);
		MapType xmlMap = JAXB.unmarshal(info.openStream(), MapType.class);

		return Map.load(xmlMap, assetManager);
    }
}
