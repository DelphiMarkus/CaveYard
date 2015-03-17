package caveyard.map;

import caveyard.xml.map.CellType;
import caveyard.xml.map.MapType;
import caveyard.map.math.Area;
import caveyard.map.math.Rect;
import com.jme3.asset.AssetManager;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.logging.Logger;

/**
 *
 * @author Maximilian Timmerkamp <>
 */
public class Map
{
	protected static Logger logger = Logger.getLogger(Map.class.getName());
	protected AssetManager assetManager;

	//protected CellTree cellTree;
	protected ArrayList<Cell> cells;

    protected Node attachedNodes;
    protected Node visibleCells;
    
    public Map(AssetManager assetManager)
    {
		this.assetManager = assetManager;

		//cellTree = new CellTree();
		cells = new ArrayList<>();

        attachedNodes = new Node("attachedNodes");
        visibleCells = new Node("visibleCells");
    }

    protected void addCell(Cell cell)
    {
		//cellTree.insert(rect, cell);
		cells.add(cell);
    }

	protected HashSet<Cell> find(Area area)
	{
		HashSet<Cell> cells = new HashSet<>();

		for (Cell cell: this.cells)
		{
			if (cell.getArea().intersectsWith(area))
			{
				cells.add(cell);
			}
		}

		return cells;
	}

	/**
	 * Loads a Map from XML bindings.
	 *
	 * @return A map loaded from XML.
	 */
	public static Map load(MapType xmlMap, AssetManager assetManager)
	{
		Map map = new Map(assetManager);

		for (CellType cellType: xmlMap.getCells().getCell())
		{
			// Create rectangle
			Vector3f pos = new Vector3f(cellType.getX(), cellType.getY(), cellType.getZ());
			Rect rect = new Rect(pos.x, pos.z, pos.x + cellType.getSizeX(), pos.z + cellType.getSizeZ());

			// Create cell and add it to the map
			Cell cell = new Cell(cellType.getFile(), cellType.getNode(), pos, rect);
			// Get offset and tell the cell about
			if (!cellType.getOffset().isNil())
			{
				CellType.Offset offset = cellType.getOffset().getValue();
				Vector3f nodeOffset = new Vector3f(offset.getX(), offset.getY(), offset.getZ());
				cell.setNodeOffset(nodeOffset);
			}

			map.addCell(cell);
		}

		return map;
	}


	public void attachTo(Node node)
	{
		node.attachChild(visibleCells);
		attachedNodes.attachChild(node);
	}

	public void detachFrom(Node node)
	{
		node.detachChild(visibleCells);
		attachedNodes.detachChild(node);
	}
}
