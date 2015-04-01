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
 * necessary Collision shapes of the terrain.
 *
 * @author Maximilian Timmerkamp
 */
public class MapTerrainPhysicsControl extends AbstractControl
{
	protected PhysicsSpace physicsSpace;
	protected Map map;

	protected HashSet<Cell> oldVisibleCells;
	protected boolean needsUpdate;

	public MapTerrainPhysicsControl(PhysicsSpace physicsSpace)
	{
		this.physicsSpace = physicsSpace;

		this.oldVisibleCells = new HashSet<>();
		this.needsUpdate = true;
	}

	public boolean isNeedsUpdate()
	{
		return needsUpdate;
	}

	public void setNeedsUpdate(boolean needsUpdate)
	{
		this.needsUpdate = needsUpdate;
	}

	@Override
	public void setSpatial(Spatial spatial)
	{
		if (spatial instanceof MapNode)
		{
			super.setSpatial(spatial);
			this.map = ((MapNode) spatial).getMap();
			((MapNode) spatial).setMapPhysics(this);
		}
		else
		{
			throw new RuntimeException("MapTerrainPhysicsControl can only handle MapNodes. Got \"" +
					spatial.getClass().getName() + "\" instead.");
		}
	}

	@Override
	protected void controlUpdate(float tpf)
	{
		if (!needsUpdate) return;

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
