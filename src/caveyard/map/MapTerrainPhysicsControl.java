package caveyard.map;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;

import java.util.HashSet;
import java.util.Iterator;

/**
 * A control which updates the physics space to contain all
 * necessary Collision shapes of the map's terrain.
 *
 * This control can be added to {@link MapNode}s only. Otherwise an
 * exception will be thrown.
 *
 * @see #setSpatial(Spatial)
 *
 * @author Maximilian Timmerkamp
 */
public class MapTerrainPhysicsControl extends AbstractControl
{
	/**
	 * The physical environment of the map.
	 */
	protected PhysicsSpace physicsSpace;
	/**
	 * The map node this control is attached to. Also stored in
	 * {@link #spatial} but in a less strong data type.
	 */
	protected MapNode mapNode;
	/**
	 * The map of the current map node for easy access.
	 */
	protected Map map;

	/**
	 * Cells in the physics space since last update.
	 */
	protected HashSet<Cell> oldVisibleCells;
	/**
	 * Determines if an update of the physics space is performed on
	 * the next {@link #controlUpdate(float)} call.
	 */
	protected boolean needsUpdate;

	public MapTerrainPhysicsControl(PhysicsSpace physicsSpace)
	{
		this.physicsSpace = physicsSpace;

		this.oldVisibleCells = new HashSet<>();
		this.needsUpdate = true;

		mapNode = null;
		map = null;
	}

	/**
	 * Determines if this control updates during the next update call.
	 * @return Value of the update flag.
	 */
	public boolean isNeedsUpdate()
	{
		return needsUpdate;
	}

	/**
	 * Sets a flag which determines if the next call to {@link #controlUpdate(float)}
	 * updates the terrain collision shapes in the physics space.
	 * This method is used by {@link MapLODControl} to tell this control
	 * about changes in the displayed terrain cells.
	 *
	 * @param needsUpdate If update is needed on next update call.
	 */
	public void setNeedsUpdate(boolean needsUpdate)
	{
		this.needsUpdate = needsUpdate;
	}

	/**
	 * This control can only be added to {@link MapNode}s otherwise
	 * an exception is thrown. It does not make any sense to add this control
	 * to any other sort of node as only maps can be updated by it.
	 *
	 * @param spatial Map node to use.
	 */
	@Override
	public void setSpatial(Spatial spatial)
	{
		if (spatial instanceof MapNode)
		{
			super.setSpatial(spatial);
			this.mapNode = (MapNode) spatial;
			mapNode.setMapPhysics(this);

			this.map = mapNode.getMap();
		}
		else
		{
			throw new RuntimeException("MapTerrainPhysicsControl can only handle MapNodes. Got \"" +
					spatial.getClass().getName() + "\" instead.");
		}
	}

	/**
	 * Updates the collision information of the physics space to ensure
	 * correct collision with other objects.
	 */
	@Override
	protected void controlUpdate(float tpf)
	{
		if (!needsUpdate || mapNode == null) return;

		Iterator<Cell> it = oldVisibleCells.iterator();
		while(it.hasNext())
		{
			Cell cell = it.next();
			if (cell.getTerrainControl() != null)
			{
				physicsSpace.remove(cell.getTerrainControl());
			}

			it.remove();
		}

		// update collision shape
		for (Cell cell: map.visibleCells)
		{
			oldVisibleCells.add(cell);
			if (cell.getTerrainControl() != null)
			{
				physicsSpace.add(cell.getTerrainControl());
			}
		}

		needsUpdate = false;
	}

	@Override
	protected void controlRender(RenderManager rm, ViewPort vp)
	{
	}
}
