package caveyard.map;

import caveyard.util.VecUtil;
import caveyard.util.quadtree.QuadPoint;
import caveyard.util.quadtree.QuadRange;
import caveyard.util.quadtree.QuadTree;
import com.jme3.math.Vector2f;
import com.jme3.scene.Spatial;

import java.util.*;
import java.util.logging.Logger;

/**
 * @author Maximilian Timmerkamp
 */
public class ObjectsTree
{
	protected final static Logger LOGGER = Logger.getLogger(ObjectsTree.class.getName());

	protected QuadTree<Float, ObjectsCell> tree;

	protected Vector2f upperLeft;
	protected Vector2f lowerRight;

	protected Vector2f cellSize;

	protected QuadRange<Float> treeRange;


	public ObjectsTree(Vector2f upperLeft, Vector2f lowerRight, Vector2f minCellSize)
	{
		treeRange = new QuadRange<>(QuadPoint.fromVector2f(upperLeft), QuadPoint.fromVector2f(lowerRight));
		tree = new QuadTree<>(treeRange, true);

		this.upperLeft = upperLeft;
		this.lowerRight = lowerRight;
		cellSize = minCellSize;

		Vector2f size = new Vector2f(lowerRight.getX() - upperLeft.getX(), lowerRight.getY() - upperLeft.getY());
		Vector2f center = upperLeft.add(size.divide(2));

		tree.insert(QuadPoint.fromVector2f(center), null);
		subdivideTree(center, size, minCellSize);
	}

	private void subdivideTree(Vector2f center, Vector2f size, Vector2f minCellSize)
	{
		final Vector2f halfSize = size.divide(2.0f);
		final Vector2f newCellsHalfSize = halfSize.divide(2);
		boolean subdivideFinished = halfSize.x <= minCellSize.x && halfSize.y <= minCellSize.y;

		Vector2f center1 = new Vector2f(center.x - halfSize.x/2, center.y - halfSize.y/2);
		Vector2f center2 = new Vector2f(center1.x + halfSize.x, center1.y);
		Vector2f center3 = new Vector2f(center2.x, center2.y + halfSize.y);
		Vector2f center4 = new Vector2f(center3.x - halfSize.x, center3.y);

		for (Vector2f centerN: new Vector2f[] {center1, center2, center3, center4})
		{
			QuadPoint<Float> point = new QuadPoint<>(centerN.x, centerN.y);
			if (subdivideFinished)
			{
				ObjectsCell cell = new ObjectsCell(centerN, newCellsHalfSize);
				tree.insert(point, cell);
			}
			else
			{
				tree.insert(point, null);
				subdivideTree(centerN, halfSize, minCellSize);
			}
		}
	}

	public void insert(Spatial object)
	{
		Vector2f pos = VecUtil.toXZVector(object.getWorldTranslation());

		ObjectsCell cell = get(pos);
		cell.objects.add(object);
	}

	/**
	 * Inserts all objects of the cell to one cell in the tree.
	 * @param cell
	 */
	public void insertSimple(ObjectsCell cell)
	{
		ObjectsCell objectsCell = get(cell.pos);
		objectsCell.objects.addAll(cell.objects);
	}

	/**
	 * Inserts and empties an ObjectsCell. Beware that the cells size
	 * ({@link ObjectsCell#halfSize}) must be set correctly, otherwise
	 * there will be objects left in the <code>insertCell</code>.
	 * If everything is set correctly, the cell will be empty.
	 * @param insertCell Cell which objects to insert.
	 */
	public void insertAndEmpty(ObjectsCell insertCell)
	{
		Vector2f p1 = insertCell.getPos().subtract(insertCell.getHalfSize());
		Vector2f p2 = insertCell.getPos().add(insertCell.getHalfSize());
		final QuadRange<Float> insertRange = pointsToRange(p1, p2);

		List<ObjectsCell> cells = find(insertRange);
		for (ObjectsCell cell: cells)
		{
			final QuadRange<Float> range = pointsToRange(cell.getPos().subtract(cell.getHalfSize()),
					cell.getPos().add(cell.getHalfSize()));

			for (Iterator<Spatial> it = insertCell.objects.iterator(); it.hasNext();)
			{
				Spatial object = it.next();

				final Vector2f pos = VecUtil.toXZVector(object.getWorldTranslation());
				final QuadPoint<Float> objectPoint = QuadPoint.fromVector2f(pos);
				if (range.contains(objectPoint))
				{
					it.remove();
					cell.objects.add(object);
				}
			}
		}
	}

	public ObjectsCell get(Vector2f pos)
	{
		QuadPoint<Float> quadPoint = QuadPoint.fromVector2f(pos);
		if (treeRange.contains(quadPoint))
		{
			QuadRange<Float> range = new QuadRange<>(quadPoint, quadPoint);
			List<ObjectsCell> cells = tree.find(range);
			if (cells.size() != 1) throw new IndexOutOfBoundsException();
			return cells.get(0);
		}
		else
		{
			// TODO: Better exception
			throw new IndexOutOfBoundsException("Position outside of this tree.");
		}
	}

	protected QuadRange<Float> pointsToRange(Vector2f p1, Vector2f p2)
	{
		QuadPoint<Float> quadPoint1 = QuadPoint.fromVector2f(p1);
		QuadPoint<Float> quadPoint2 = QuadPoint.fromVector2f(p2);
		return new QuadRange<>(quadPoint1, quadPoint2);
	}

	protected List<ObjectsCell> find(QuadRange<Float> range)
	{
		return tree.find(range);
	}

	public List<ObjectsCell> find(Vector2f p1, Vector2f p2)
	{
		QuadRange<Float> range = pointsToRange(p1, p2);
		return find(range);
	}

	public Set<Spatial> findObjects(Vector2f p1, Vector2f p2)
	{
		QuadRange<Float> range = pointsToRange(p1, p2);
		List<ObjectsCell> cells = find(range);

		Set<Spatial> objects = new HashSet<>();
		for (ObjectsCell cell: cells)
		{
			for (Spatial object: cell.objects)
			{
				QuadPoint<Float> pos = QuadPoint.fromVector2f(VecUtil.toXZVector(object.getWorldTranslation()));
				if (range.contains(pos))
				{
					objects.add(object);
				}
			}
		}

		return objects;
	}

	/**
	 * Searches the tree for {@link ObjectsCell}s in the given axis aligned x-z-rectangle
	 * specified py <code>p1</code> and <code>p2</code>.
	 * Objects which are in the search range are removed from their original ObjectCells
	 * and added to the <code>objects</code> collection passed.
	 *
	 * <p>There are corresponding functions which return {@link List}s or {@link ObjectsCell}s
	 * filled with the found objects.</p>
	 *
	 * @param p1 Upper left point of searched rectangle.
	 * @param p2 Lower right point of searched rectangle.
	 * @param objects Collection all results are added to.
	 * @return The <code>objects</code> parameter after adding found objects.
	 *
	 * @see #findObjectsAndRemove(Vector2f, Vector2f)
	 * @see #findObjectsAndRemoveToCell(Vector2f, Vector2f)
	 */
	public Collection<Spatial> findObjectsAndRemoveTo(Vector2f p1, Vector2f p2, Collection<Spatial> objects)
	{
		final QuadRange<Float> range = pointsToRange(p1, p2);
		List<ObjectsCell> cells = find(range);


		for (ObjectsCell cell: cells)
		{
			//LOGGER.fine("Filtering from " + cell.objects.size() + " objects...");
			Iterator<Spatial> it = cell.objects.iterator();
			while(it.hasNext())
			{
				Spatial object = it.next();
				QuadPoint<Float> pos = QuadPoint.fromVector2f(VecUtil.toXZVector(object.getWorldTranslation()));
				if (range.contains(pos))
				{
					it.remove();
					objects.add(object);
				}
			}
		}

		return objects;
	}

	public Set<Spatial> findObjectsAndRemove(Vector2f p1, Vector2f p2)
	{
		Set<Spatial> objects = new HashSet<>();
		findObjectsAndRemoveTo(p1, p2, objects);

		return objects;
	}

	public ObjectsCell findObjectsAndRemoveToCell(Vector2f p1, Vector2f p2)
	{
		HashSet<Spatial> objects = new HashSet<>();
		findObjectsAndRemoveTo(p1, p2, objects);

		ObjectsCell cell = ObjectsCell.fromRect(p1, p2);
		cell.setObjects(objects);

		return cell;
	}
}
