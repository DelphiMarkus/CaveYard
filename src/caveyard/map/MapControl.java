package caveyard.map;

import caveyard.map.math.Circle;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;

import java.util.HashSet;
import java.util.logging.Logger;

/**
 * @author Maximilian Timmerkamp
 */
public class MapControl extends AbstractControl
{
	protected static Logger logger = Logger.getLogger(MapControl.class.getName());

	protected Spatial player;
	protected Map map;

	protected Vector2f lastUpdatePos;
	protected float renderRadius;
	protected float reloadThreshold;

	public MapControl(Spatial player, float renderRadius, float reloadThreshold)
	{
		this.player = player;
		this.renderRadius = renderRadius;
		this.reloadThreshold = reloadThreshold;

		this.map = null;
	}

	public void setPlayer(Spatial player)
	{
		this.player = player;
	}

	public Spatial getPlayer()
	{
		return player;
	}

	public float getRenderRadius()
	{
		return renderRadius;
	}

	public void setRenderRadius(float renderRadius)
	{
		this.renderRadius = renderRadius;
	}

	public float getReloadThreshold()
	{
		return reloadThreshold;
	}

	public void setReloadThreshold(float reloadThreshold)
	{
		this.reloadThreshold = reloadThreshold;
	}

	@Override
	public void setSpatial(Spatial spatial)
	{
		if (spatial instanceof MapNode)
		{
			super.setSpatial(spatial);
			this.map = ((MapNode) spatial).getMap();
			((MapNode) spatial).setMapControl(this);
		}
		else
		{
			throw new RuntimeException("MapControl can only handle MapNodes. Got \"" +
					spatial.getClass().getName() + "\" instead.");
		}
	}

	@Override
	protected void controlUpdate(float tpf)
	{
		final Vector3f pos = player.getWorldTranslation();

		final Vector2f pos2D = new Vector2f(pos.x, pos.z);
		// Only do an update if we moved since last update.
		if (lastUpdatePos == null || pos2D.distance(lastUpdatePos) >= renderRadius*reloadThreshold)
		{
			logger.fine("Updating cells...");

			map.terrain.detachAllChildren();
			map.objects.detachAllChildren();
			map.visibleCells.clear();

			final Circle circle = new Circle(pos2D.x, pos2D.y, renderRadius);
			for (Cell cell : map.find(circle))
			{
				if (!cell.isLoaded())
					cell.loadCell(map.assetManager);

				map.visibleCells.add(cell);

				// get terrain and objects nodes
				if (cell.getTerrainNode() != null)
					map.terrain.attachChild(cell.getTerrainNode());
				if (cell.getObjectsNode() != null)
					map.objects.attachChild(cell.getObjectsNode());
			}
			logger.fine("Update finished. Visible terrain nodes: " + map.visibleCells.size());

			lastUpdatePos = pos2D;

			// tell MapTerrainPhysicsControl to do an update
			map.getMapNode().getMapPhysics().setNeedsUpdate(true);
		}
	}

	@Override
	protected void controlRender(RenderManager rm, ViewPort vp)
	{
		// nothing
	}
}
