package caveyard.map;

import caveyard.map.math.Circle;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;

import java.util.logging.Logger;

/**
 * @author Maximilian Timmerkamp
 */
public class MapControl extends AbstractControl
{
	protected static Logger logger = Logger.getLogger(MapControl.class.getName());

	protected Map map;

	protected Vector2f lastUpdatePos;
	protected float renderRadius;
	protected float reloadThreshold;

	public MapControl(Map map, float renderRadius, float reloadThreshold)
	{
		this.map = map;
		this.renderRadius = renderRadius;
		this.reloadThreshold = reloadThreshold;
	}

	public void setMap(Map map)
	{
		this.map = map;
	}

	public Map getMap()
	{
		return map;
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
	protected void controlUpdate(float tpf)
	{
		final Vector3f pos = spatial.getWorldTranslation();

		final Vector2f pos2D = new Vector2f(pos.x, pos.z);
		// Only do an update if we moved since last update.
		if (lastUpdatePos == null || pos2D.subtract(lastUpdatePos).length() >= renderRadius*reloadThreshold)
		{
			logger.fine("Updating cells...");

			map.visibleCells.detachAllChildren();

			//final Rect rect = new Rect(pos2D.x - renderRadius, pos2D.y - renderRadius,
			//		pos2D.x + renderRadius, pos2D.y + renderRadius);
			final Circle circle = new Circle(pos2D.x, pos2D.y, renderRadius);
			for (Cell cell : map.find(circle))
			{
				cell.loadCell(map.assetManager);
				map.visibleCells.attachChild(cell.getCellNode());
			}
			logger.fine("Update finished. Visible cells: " + map.visibleCells.getQuantity());

			lastUpdatePos = pos2D;
		}
	}

	@Override
	protected void controlRender(RenderManager rm, ViewPort vp)
	{
		// nothing
	}
}
