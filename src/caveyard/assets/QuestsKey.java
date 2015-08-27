package caveyard.assets;

import caveyard.xml.quests.QuestsType;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetProcessor;
import com.jme3.asset.cache.AssetCache;
import com.jme3.asset.cache.WeakRefAssetCache;

/**
 * Created by maximilian on 08.07.15.
 */
public class QuestsKey extends AssetKey<QuestsType>
{
	public QuestsKey(String name)
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
