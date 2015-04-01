package caveyard.map;

import com.jme3.scene.Node;

/**
 * A Node knowing its map to update terrain, objects and the map's
 * collision shape.
 *
 * @author Maximilian Timmerkamp
 */
public class MapNode extends Node
{
	public static final String MAP_NODE_NAME = "mapNode";

	protected Map map;

	protected MapControl mapControl;
	protected MapTerrainPhysicsControl mapPhysics;

	public MapNode(Map map)
	{
		this(map, null, null);
	}

	public MapNode(Map map, MapControl mapControl, MapTerrainPhysicsControl mapPhysics)
	{
		super(MAP_NODE_NAME);

		this.map = map;
		this.mapControl = mapControl;
		this.mapPhysics = mapPhysics;
	}

	public Map getMap()
	{
		return map;
	}

	public MapControl getMapControl()
	{
		return mapControl;
	}

	public void setMapControl(MapControl mapControl)
	{
		this.mapControl = mapControl;
	}

	public MapTerrainPhysicsControl getMapPhysics()
	{
		return mapPhysics;
	}

	public void setMapPhysics(MapTerrainPhysicsControl mapPhysics)
	{
		this.mapPhysics = mapPhysics;
	}
}
