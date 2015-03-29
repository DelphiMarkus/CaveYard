package caveyard.map;

import caveyard.assets.MapKey;
import caveyard.xml.map.MapType;
import caveyard.xml.maps.Maps;
import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetManager;

import javax.xml.bind.JAXB;
import java.util.HashMap;

/**
 *
 * @author Maximilian Timmerkamp
 */
public class MapManager
{
	protected final String mapsFile = "Data/map/maps.xml";
	protected static MapManager mapManager = null;

	protected AssetManager assetManager;
	protected HashMap<String, Maps.Map> maps;

	protected MapManager(AssetManager assetManager)
	{
		this.assetManager = assetManager;
		loadMaps();
	}

	public static MapManager getInstance(AssetManager assetManager)
	{
		if (mapManager == null)
		{
			mapManager = new MapManager(assetManager);
		}

		return mapManager;
	}

	protected void loadMaps()
	{
		AssetKey<MapType> key = new AssetKey<>(mapsFile);
		AssetInfo info = assetManager.locateAsset(key);

		Maps mapsType = JAXB.unmarshal(info.openStream(), Maps.class);

		maps = new HashMap<>();

		for (Maps.Map mapDefinition: mapsType.getMap())
		{
			String id = mapDefinition.getId();

			maps.put(id, mapDefinition);
		}
	}

    public Map loadMap(String id)
    {
		Maps.Map mapDefinition = maps.get(id);

		if (mapDefinition == null)
		{

			return null;
		}
		else
		{
			return assetManager.loadAsset(new MapKey(mapDefinition.getFile()));
		}
    }
}
