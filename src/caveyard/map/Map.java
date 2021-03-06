package caveyard.map;

import caveyard.map.math.Area;
import caveyard.map.math.Rect;
import caveyard.xml.map.CellType;
import caveyard.xml.map.MapType;
import com.jme3.asset.AssetManager;
import com.jme3.math.Vector2f;
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

	protected HashSet<Cell> visibleCells;

	protected Node terrain;
	protected Node objects;
	protected Node physicsObjects;
	protected MapNode mapNode;

	protected ObjectsTree objectsTree;
    
    public Map(AssetManager assetManager)
    {
		this.assetManager = assetManager;

		//cellTree = new CellTree();
		cells = new ArrayList<>();
		visibleCells = new HashSet<>();

		terrain = new Node("terrain");
		objects = new Node("objects");
		physicsObjects = new Node("physicsObjects");

		mapNode = new MapNode(this);

		mapNode.attachChild(terrain);
		mapNode.attachChild(objects);
		mapNode.attachChild(physicsObjects);

		// TODO: get proper coordinates!
		objectsTree = new ObjectsTree(new Vector2f(-100, -100), new Vector2f(100, 100), new Vector2f(10f, 10f));
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
			if (cellType.getOffset() != null)
			{
				CellType.Offset offset = cellType.getOffset().getValue();
				Vector3f nodeOffset = new Vector3f(offset.getX(), offset.getY(), offset.getZ());
				cell.setNodeOffset(nodeOffset);
			}
			else
			{
				cell.setNodeOffset(Vector3f.ZERO);
			}

			map.addCell(cell);
		}

		return map;
	}

	public Node getObjects()
	{
		return objects;
	}

	public Node getTerrain()
	{
		return terrain;
	}

	public MapNode getMapNode()
	{
		return mapNode;
	}
}
