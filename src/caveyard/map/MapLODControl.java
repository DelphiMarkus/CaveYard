package caveyard.map;

import caveyard.map.math.Circle;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;

import java.util.logging.Logger;

/**
 * This control updates a list of all visible cells. If a cells is visible is
 * determined by its distance to the {@link #player} spatial. If the distance
 * is shorter than {@link #renderRadius} the cell will be visible. Otherwise
 * it will be hidden.
 * To conserve processing power the {@link #player}'s position is checked to
 * be farther away from its position on the last cell update than
 * {@link #reloadDistance}. If the current position is not far away enough,
 * no update is done to the visible cells.
 *
 * <p>
 *     TODO: Implement cell unloading if they are far enough away.
 * </p>
 *
 * @author Maximilian Timmerkamp
 */
public class MapLODControl extends AbstractControl
{
	protected static Logger LOGGER = Logger.getLogger(MapLODControl.class.getName());

	/**
	 * Spatial to check positions.
	 */
	protected Spatial player;
	/**
	 * The current map node this control is attached to.
	 */
	protected MapNode mapNode;
	/**
	 * The map of the current map node for easy access.
	 */
	protected Map map;

	/**
	 * Position where te last update on the visible cells were done.
	 */
	protected Vector2f lastUpdatePos;
	/**
	 * Radius of the circle around the {@link #player} spatial to make
	 * cells visible.
	 */
	protected float renderRadius;
	/**
	 * Minimum distance of the {@link #player} spatial and the
	 * {@link #lastUpdatePos} necessary to perform an update on visible cells.
	 */
	protected float reloadDistance;

	public MapLODControl(Spatial player, float renderRadius, float reloadDistance)
	{
		this.player = player;
		this.renderRadius = renderRadius;
		this.reloadDistance = reloadDistance;

		this.mapNode = null;
	}

	/**
	 * Registeres the spatial which position is used to determine the
	 * cells to load.
	 * @param player New player spatial.
	 */
	public void setPlayer(Spatial player)
	{
		this.player = player;
	}

	/**
	 * Returns the spatial currently used to calculate the cells to
	 * show around it.
	 * @return The target spatial.
	 */
	public Spatial getPlayer()
	{
		return player;
	}

	/**
	 * Returns the radius around the player spatial in which all cells are
	 * loaded and made visible.
	 * @return Radius of visible cells.
	 */
	public float getRenderRadius()
	{
		return renderRadius;
	}

	/**
	 * Sets the radius around the player spatial in which all cells are loaded
	 * and made visible. Value is in units of the game's coordinate system.
	 * @param renderRadius Radius of visible cells.
	 */
	public void setRenderRadius(float renderRadius)
	{
		this.renderRadius = renderRadius;
	}

	/**
	 * Gets the distance the {@link #player}'s position must differ from the
	 * position it had on last update. If the distance is shorter, no update
	 * on cells is performed.
	 * @return Minimum distance between two updates.
	 */
	public float getReloadDistance()
	{
		return reloadDistance;
	}

	/**
	 * Sets the distance the {@link #player}'s position must differ from the
	 * position it had on last update. If the distance is shorter, no update
	 * on cells is performed, otherwise the visible cells are updated.
	 * @param reloadDistance The distance beween two reloads.
	 */
	public void setReloadDistance(float reloadDistance)
	{
		this.reloadDistance = reloadDistance;
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
			mapNode.setMapLODControl(this);

			this.map = mapNode.getMap();
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
		if (lastUpdatePos == null || pos2D.distance(lastUpdatePos) >= reloadDistance)
		{
			LOGGER.finer("Updating cells...");

			map.terrain.detachAllChildren();
			map.objects.detachAllChildren();
			map.visibleCells.clear();

			final Circle circle = new Circle(pos2D.x, pos2D.y, renderRadius);
			for (Cell cell : map.find(circle))
			{
				if (!cell.isLoaded())
					cell.loadCell(map.assetManager, map.objectsTree);

				map.visibleCells.add(cell);

				// get terrain node
				if (cell.getTerrainNode() != null)
				{
					map.terrain.attachChild(cell.getTerrainNode());
				}
			}

			Vector2f p1 = pos2D.add(Vector2f.UNIT_XY.mult(-renderRadius));
			Vector2f p2 = pos2D.add(Vector2f.UNIT_XY.mult(renderRadius));
			for (Spatial object: map.objectsTree.findObjects(p1, p2))
			{
				map.objects.attachChild(object);
			}

			LOGGER.fine("Updated cells. Visible cells: " + map.visibleCells.size());

			lastUpdatePos = pos2D;

			// tell MapTerrainPhysicsControl to do an update
			mapNode.getMapPhysics().setNeedsUpdate(true);
		}
	}

	@Override
	protected void controlRender(RenderManager rm, ViewPort vp)
	{
		// nothing
	}
}
