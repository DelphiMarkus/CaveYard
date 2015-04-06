package caveyard.map;

import caveyard.map.math.Rect;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

import java.util.logging.Logger;

/**
 * @author Maximilian Timmerkamp
 */
public class Cell
{
	public static final String TERRAIN_NODE = "terrain";
	public static final String OBJECTS_NODE = "objects";

	protected static Logger LOGGER = Logger.getLogger(Cell.class.getName());

	protected Node node;
	protected Node terrainNode;
	protected Node objectsNode;

	protected RigidBodyControl terrainControl;
	protected boolean loaded;

	protected String filename;
	protected String nodeName;
	protected Rect area;
	protected Vector3f pos;
	protected Vector3f nodeOffset;

	public Cell(String filename, String nodeName, Vector3f pos, Rect area)
	{
		this.filename = filename;
		this.nodeName = nodeName;
		this.area = area;
		this.pos = pos;

		this.loaded = false;
		this.node = new Node();
	}

	public Node getNode()
	{
		return node;
	}

	public Node getTerrainNode()
	{
		return terrainNode;
	}

	public Node getObjectsNode()
	{
		return objectsNode;
	}

	public Rect getArea()
	{
		return area;
	}

	public Vector3f getPos()
	{
		return pos;
	}

	public Vector3f getNodeOffset()
	{
		return nodeOffset;
	}

	public boolean isLoaded()
	{
		return loaded;
	}

	public RigidBodyControl getTerrainControl()
	{
		return terrainControl;
	}

	public void setNodeOffset(Vector3f nodeOffset)
	{
		this.nodeOffset = nodeOffset;
	}

	public boolean loadCell(AssetManager assetManager, ObjectsTree objectsTree)
	{
		if (isLoaded()) return true;

		LOGGER.info("Loading cell " + filename + ":" + nodeName);

		Node node = (Node) assetManager.loadModel(filename);
		if (nodeName != null && nodeName.length() != 0 && !node.getName().equals(nodeName))
		{
			Node child = (Node) node.getChild(nodeName);
			if (child != null)
			{
				node = child;
			}
			else
			{
				LOGGER.warning("Unable to load Node \"" + nodeName + "\" from \"" + filename + "\"" +
						"; using Model as a whole.");
			}
		}

//		this.node.detachAllChildren();
//		this.node.setLocalTranslation(pos);
//		this.node.attachChild(node);

		for (Spatial child: node.getChildren())
		{
			if (child.getName().equals(Cell.TERRAIN_NODE))
			{
				terrainNode = new Node();
				terrainNode.setLocalTranslation(pos);
				child.setLocalTranslation(nodeOffset.negate());
				terrainNode.attachChild(child);
			}
			else if (child.getName().equals(Cell.OBJECTS_NODE))
			{
				for (Spatial object : ((Node) child).getChildren())
				{
					object.removeFromParent();

					Vector3f objectPos = object.getLocalTranslation();
					objectPos.addLocal(pos);
					objectPos.subtractLocal(nodeOffset);
					object.setLocalTranslation(objectPos);

					objectsTree.insert(object);
				}
			}
		}

		if (objectsNode == null && terrainNode == null)
		{
			LOGGER.finer("Model \"" + filename + "\" does not have terrain or " +
					"objects node. Displaying nothing.");
		}

		calculateTerrainShape();

		loaded = true;
		return true;
	}

	public void calculateTerrainShape()
	{
		if (terrainControl != null || terrainNode == null) return;

		CollisionShape terrainShape = CollisionShapeFactory.createMeshShape(terrainNode);
		terrainControl = new RigidBodyControl(terrainShape, 0);
		terrainControl.setPhysicsLocation(pos);
	}

	public void unloadCell()
	{
		if (isLoaded())
		{
			node.detachAllChildren();
			terrainNode = null;
			objectsNode = null;
			loaded = false;
		}
	}
}
