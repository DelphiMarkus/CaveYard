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
import java.util.logging.Logger;

/**
 * Controls physics of non-static objects.
 */
public class MapObjectsPhysicsControl extends AbstractControl
{
	protected static final Logger LOGGER = Logger.getLogger(MapObjectsPhysicsControl.class.getName());

	protected MapNode mapNode;
	protected Map map;

	protected Spatial target;

	protected PhysicsSpace physicsSpace;
	protected float physicsRadius;
	protected float reloadDistance;
	protected Vector2f lastUpdatePos;

	protected ObjectsCell currentObjects;

	public MapObjectsPhysicsControl(Spatial target, float physicsRadius, float reloadDistance,
									PhysicsSpace physicsSpace)
	{
		this.target = target;
		this.physicsRadius = physicsRadius;
		this.reloadDistance = reloadDistance;
		this.physicsSpace = physicsSpace;

		lastUpdatePos = null;
	}

	public float getPhysicsRadius()
	{
		return physicsRadius;
	}

	public void setPhysicsRadius(float physicsRadius)
	{
		this.physicsRadius = physicsRadius;
	}

	public float getReloadDistance()
	{
		return reloadDistance;
	}

	public void setReloadDistance(float reloadDistance)
	{
		this.reloadDistance = reloadDistance;
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

		if (lastUpdatePos == null || pos.distance(lastUpdatePos) >= reloadDistance)
		{
			if (currentObjects != null)
			{
				map.objectsTree.insertAndEmpty(currentObjects);
				if (currentObjects.getObjects().size() > 0)
					LOGGER.warning("Objects left: " + currentObjects.getObjects().size());
			}
			ObjectsCell oldObjects = currentObjects;

			Vector2f p1 = pos.add(Vector2f.UNIT_XY.mult(-physicsRadius));
			Vector2f p2 = pos.add(Vector2f.UNIT_XY.mult(physicsRadius));
			currentObjects = map.objectsTree.findObjectsAndRemoveToCell(p1, p2);

			if (oldObjects != null)
			{
				for (Spatial object: oldObjects.getObjects())
				{
					currentObjects.getObjects().add(object);
				}
			}

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
						control = new RigidBodyControl(shape, 10);
						object.addControl(control);
					}
					physicsSpace.add(control);

					map.physicsObjects.attachChild(object);
				}
			}

			lastUpdatePos = pos;
		}
	}

	@Override
	protected void controlRender(RenderManager rm, ViewPort vp)
	{
	}
}
