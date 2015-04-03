package caveyard.map;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;

import java.util.List;
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
	protected Vector3f lastUpdatePos;

	public MapObjectsPhysicsControl(Spatial target, float physicsRadius, float reloadDistance,
									PhysicsSpace physicsSpace)
	{
		this.target = target;
		this.physicsRadius = physicsRadius;
		this.reloadDistance = reloadDistance;
		this.physicsSpace = physicsSpace;
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
		final Vector3f pos = target.getWorldTranslation().clone();
		pos.setY(0);

		if (lastUpdatePos == null || pos.distance(lastUpdatePos) >= reloadDistance)
		{
			for (Spatial cellObjects: map.objects.getChildren())
			{
				List<Spatial> cells = ((Node) cellObjects).getChildren();
				Node objects = (Node) cells.get(0);
				if (cells.size() > 1) LOGGER.warning("More than one child. All other children are not processed!");

				for (Spatial object : objects.getChildren())
				{
					if (pos.distance(object.getWorldTranslation().clone().setY(0)) <= physicsRadius)
					{
						RigidBodyControl control = object.getControl(RigidBodyControl.class);
						if (control == null)
						{
							CollisionShape shape = CollisionShapeFactory.createDynamicMeshShape(object);
							control = new RigidBodyControl(shape, 10);
							object.addControl(control);
						}
						physicsSpace.add(control);
					}
					else
					{
						RigidBodyControl control = object.getControl(RigidBodyControl.class);
						if (control != null)
						{
							physicsSpace.remove(control);
						}
					}
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
