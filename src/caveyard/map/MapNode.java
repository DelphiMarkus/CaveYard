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

	/**
	 * Map which owns this node.
	 */
	protected Map map;

	/**
	 * The map's LOD control.
	 */
	protected MapLODControl mapLODControl;
	/**
	 * The map's physics control which handles all terrain physics.
	 */
	protected MapTerrainPhysicsControl mapPhysics;

	/**
	 * Creates a new Node. {@link #mapLODControl} and {@link #mapPhysics} are set to null.
	 * If a {@link MapLODControl} is attached to this node, this node's attribute
	 * mapLODControl will be set automatically. The same happens when a
	 * {@link MapTerrainPhysicsControl} is attached.
	 *
	 * @param map Map which owns this map node.
	 */
	public MapNode(Map map)
	{
		this(map, null, null);
	}

	/**
	 * Creates a new Node. mapLodControl and mapPhysics are automatically added
	 * to this node if not null. If a {@link MapLODControl} is attached to this
	 * node, this node's attribute mapLODControl will be set (and overwritten)
	 * automatically. The same happens when a {@link MapTerrainPhysicsControl}
	 * is attached.
	 *
	 * @param map Map which owns this map node.
	 * @param mapLODControl LOD control to use.
	 * @param mapPhysics terrain physics to use.
	 */
	public MapNode(Map map, MapLODControl mapLODControl, MapTerrainPhysicsControl mapPhysics)
	{
		super(MAP_NODE_NAME);

		this.map = map;

		this.mapLODControl = mapLODControl;
		this.mapPhysics = mapPhysics;

		if (mapLODControl != null)
		{
			this.addControl(mapLODControl);
		}
		if (mapPhysics != null)
		{
			this.addControl(mapPhysics);
		}

		this.map.mapNode = this;
	}

	/**
	 * Get the map which owns this node.
	 * @return The owning map.
	 */
	public Map getMap()
	{
		return map;
	}

	public Node getTerrainNode()
	{
		return map.getTerrain();
	}

	public Node getObjectsNode()
	{
		return map.getObjects();
	}

	/**
	 * Get the LOD control.
	 * @return The map's MapLODControl.
	 */
	public MapLODControl getMapLODControl()
	{
		return mapLODControl;
	}

	/**
	 * Set the map LOD control. A map only has one LOD control.
	 * @param mapLODControl The map's LOD control.
	 */
	public void setMapLODControl(MapLODControl mapLODControl)
	{
		this.mapLODControl = mapLODControl;
	}

	/**
	 * Gets the map's  physics control of the terrain.
	 * @return The current physics control.
	 */
	public MapTerrainPhysicsControl getMapPhysics()
	{
		return mapPhysics;
	}

	/**
	 * Sets the map's physics control. A map only has one terrain physics
	 * control.
	 * @param mapPhysics The map's terrain physics control.
	 */
	public void setMapPhysics(MapTerrainPhysicsControl mapPhysics)
	{
		this.mapPhysics = mapPhysics;
	}
}
