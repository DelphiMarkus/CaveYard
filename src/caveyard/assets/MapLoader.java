package caveyard.assets;

import caveyard.map.Map;
import caveyard.xml.map.MapType;
import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetLoader;

import javax.xml.bind.JAXB;
import java.io.IOException;

/**
 * @author Maximilian Timmerkamp
 */
public class MapLoader implements AssetLoader
{

	@Override
	public Object load(AssetInfo assetInfo) throws IOException
	{
		MapType xmlMap = JAXB.unmarshal(assetInfo.openStream(), MapType.class);

		return Map.load(xmlMap, assetInfo.getManager());
	}
}
