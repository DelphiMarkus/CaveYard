package caveyard.map;

import caveyard.map.math.Rect;
import com.jme3.asset.AssetManager;
import com.jme3.light.Light;
import com.jme3.light.PointLight;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

import java.util.logging.Logger;

/**
 * @author Maximilian Timmerkamp
 */
public class Cell
{
	protected Logger logger = Logger.getLogger(Cell.class.getName());

	protected Node cellNode;

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

		this.cellNode = new Node();
	}

	public Node getCellNode()
	{
		return cellNode;
	}

	public Rect getArea()
	{
		return area;
	}

	public Vector3f getNodeOffset()
	{
		return nodeOffset;
	}

	public void setNodeOffset(Vector3f nodeOffset)
	{
		this.nodeOffset = nodeOffset;
	}

	public boolean loadCell(AssetManager assetManager)
	{
		if (cellNode.getChildren().size() != 0) return true;

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
				logger.warning("Unable to load Node \"" + nodeName + "\" from \"" + filename + "\"" +
						"; using Model as a whole.");
			}
		}

		Vector3f oldNodePos = node.getWorldTranslation();

		cellNode.detachAllChildren();
		cellNode.setLocalTranslation(pos);
		cellNode.attachChild(node);
		// remove offset
		node.setLocalTranslation(nodeOffset.mult(-1));

		Vector3f newNodePos = node.getWorldTranslation();
		Vector3f translation = oldNodePos.subtract(newNodePos);

		for (Light light: node.getLocalLightList())
		{
			if (light instanceof PointLight)
			{
				Vector3f newLightPos = ((PointLight) light).getPosition();
				newLightPos = newLightPos.add(translation);

				((PointLight) light).setPosition(newLightPos);
			}
		}


		return true;
	}

	public void unloadCell()
	{
		cellNode.detachAllChildren();
	}
}
