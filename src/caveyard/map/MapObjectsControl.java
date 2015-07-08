package caveyard.map;

import caveyard.util.VecUtil;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.math.Vector2f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;

/**
 * This control updates the physics space if there are new physical objects
 * near the player and handles adding objects to the scene to display them.
 *
 * @author Maximilian Timmerkamp
 */
public class MapObjectsControl extends AbstractControl
{
	protected static Logger LOGGER = Logger.getLogger(MapObjectsControl.class.getName());

	/**
	 * Spatial to check positions.
	 */
	protected Spatial target;
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
	protected float updateDistance;

	protected float physicsRadius;
	protected float renderRadius;

	protected PhysicsSpace physicsSpace;
	protected ObjectsCell currentObjects;

	public MapObjectsControl(Spatial target, float renderRadius, float physicsRadius, float reloadDistance,
							 PhysicsSpace physicsSpace)
	{
		this.target = target;
		this.physicsRadius = physicsRadius;
		this.renderRadius = renderRadius;
		this.updateDistance = reloadDistance;
		this.physicsSpace = physicsSpace;

		lastUpdatePos = null;
		currentObjects = null;
	}

	public float getUpdateDistance()
	{
		return updateDistance;
	}

	public void setUpdateDistance(float updateDistance)
	{
		this.updateDistance = updateDistance;
	}

	public float getPhysicsRadius()
	{
		return physicsRadius;
	}

	public void setPhysicsRadius(float physicsRadius)
	{
		this.physicsRadius = physicsRadius;
	}

	public float getRenderRadius()
	{
		return renderRadius;
	}

	public void setRenderRadius(float renderRadius)
	{
		this.renderRadius = renderRadius;
	}

	@Override
	public void setSpatial(Spatial spatial)
	{
		if (spatial instanceof MapNode)
		{
			super.setSpatial(spatial);
			mapNode = (MapNode) spatial;
			map = mapNode.getMap();
		}
		else
		{
			throw new RuntimeException("This Control can only handle MapNodes. Got \"" +
					spatial.getClass().getName() + "\" instead.");
		}
	}


	@Override
	protected void controlUpdate(float tpf)
	{
		final Vector2f pos = VecUtil.toXZVector(target.getWorldTranslation());

		if (lastUpdatePos == null || pos.distance(lastUpdatePos) >= updateDistance)
		{
			map.objects.detachAllChildren();

			updatePhysicsObjects(pos);
			updateObjects(pos);

			lastUpdatePos = pos;
		}
	}

	protected void updatePhysicsObjects(Vector2f pos)
	{
		if (currentObjects != null)
		{
			map.objectsTree.insertAndEmpty(currentObjects);
			if (currentObjects.getObjects().size() > 0)
			{
				LOGGER.warning("Objects left: " + currentObjects.getObjects().size() + ". Inserting one by one...");
				for (Spatial object: currentObjects.getObjects())
				{
					map.objectsTree.insert(object);
				}
			}
		}

		Vector2f p1 = pos.add(Vector2f.UNIT_XY.mult(-physicsRadius));
		Vector2f p2 = pos.add(Vector2f.UNIT_XY.mult(physicsRadius));
		currentObjects = map.objectsTree.findObjectsAndRemoveToCell(p1, p2);

		//LOGGER.finer("number of physics objects: " + currentObjects.getObjects().size());

		HashSet<Spatial> remainingObjects = new HashSet<>();
		for (Iterator<Spatial> it = map.physicsObjects.getChildren().iterator(); it.hasNext(); )
		{
			Spatial object = it.next();

			if (!currentObjects.getObjects().contains(object))
			{
				RigidBodyControl control = object.getControl(RigidBodyControl.class);
				if (control != null)
				{
					physicsSpace.remove(control);
				}
				it.remove();
			}
			else
			{
				remainingObjects.add(object);
			}
		}

		for (Spatial object: currentObjects.getObjects())
		{
			if (!remainingObjects.contains(object))
			{
				RigidBodyControl control = object.getControl(RigidBodyControl.class);
				if (control == null)
				{
					CollisionShape shape = CollisionShapeFactory.createDynamicMeshShape(object);
					control = new RigidBodyControl(shape, 10);  // TODO: Remove stub; Set real weight
					object.addControl(control);
				}
				physicsSpace.add(control);

				map.physicsObjects.attachChild(object);
			}
		}
	}

	protected void updateObjects(Vector2f pos)
	{
		Vector2f p1 = pos.add(Vector2f.UNIT_XY.mult(-renderRadius));
		Vector2f p2 = pos.add(Vector2f.UNIT_XY.mult(renderRadius));
		Set<Spatial> objects = map.objectsTree.findObjects(p1, p2);
		//LOGGER.finer("number of static objects: " + objects.size());
		for (Spatial object: objects)
		{
			map.objects.attachChild(object);
		}
	}

	@Override
	protected void controlRender(RenderManager rm, ViewPort vp)
	{
	}
}
