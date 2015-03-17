package caveyard.assets;

import caveyard.map.Map;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetProcessor;
import com.jme3.asset.cache.AssetCache;
import com.jme3.asset.cache.WeakRefAssetCache;

/**
 * Created by maximilian on 17.03.15.
 */
public class MapKey extends AssetKey<Map>
{
	public MapKey(String name)
	{
		super(name);
	}


	@Override
	public Class<? extends AssetCache> getCacheType()
	{
		return WeakRefAssetCache.class;
	}

	@Override
	public Class<? extends AssetProcessor> getProcessorType()
	{
		return null;
	}
}
