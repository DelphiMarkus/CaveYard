package caveyard.util.quadtree;

import caveyard.util.MergeImpossibleException;
import caveyard.util.Mergeable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Maximilian Timmerkamp
 */
public class QuadTree<Key extends Comparable<Key>, Value>
{
	public abstract class Node
	{
		protected QuadPoint<Key> point; // x- and y-coordinates
		protected QuadRange<Key> range;

		protected Node(QuadPoint<Key> point, QuadRange<Key> range)
		{
			this.point = point;
			this.range = range;
		}

		public abstract Node insert(QuadPoint<Key> point, QuadRange<Key> range, Value value);
		public abstract List<Value> find2D(QuadRange<Key> range, List<Value> results);

		@Override
		public String toString()
		{
			return point.toString();
		}
	}

	public class InnerNode extends Node
	{
		protected Node nw, ne, sw, se;

		public InnerNode(QuadPoint<Key> point, QuadRange<Key> range)
		{
			super(point, range);

			nw = new EmptyLeaf();
			ne = new EmptyLeaf();
			sw = new EmptyLeaf();
			se = new EmptyLeaf();
		}

		@Override
		public Node insert(QuadPoint<Key> point, QuadRange<Key> range, Value value)
		{
			QuadPoint<Key> p1, p2;
			QuadRange<Key> newRange;

			switch (point.relativePosTo(this.point))
			{
				case EQUAL:
				case NORTH_WEST:
					newRange = new QuadRange<>(range.upLeft, this.point);
					nw = nw.insert(point, newRange, value);
					break;
				case NORTH_EAST:
					p1 = new QuadPoint<>(this.point.x, range.upLeft.y);
					p2 = new QuadPoint<>(range.bottomRight.x, this.point.y);
					newRange = new QuadRange<>(p1, p2);
					ne = ne.insert(point, newRange, value);
					break;
				case SOUTH_EAST:
					newRange = new QuadRange<>(this.point, range.bottomRight);
					se = se.insert(point, newRange, value);
					break;
				case SOUTH_WEST:
					p1 = new QuadPoint<>(range.upLeft.x, this.point.y);
					p2 = new QuadPoint<>(this.point.x, range.bottomRight.y);
					newRange = new QuadRange<>(p1, p2);
					sw = sw.insert(point, newRange, value);
					break;
			}

			return this;
		}

		@Override
		public List<Value> find2D(QuadRange<Key> range, List<Value> results)
		{
			boolean searchNW = false;
			boolean searchNE = false;
			boolean searchSE = false;
			boolean searchSW = false;

			if (range.contains(point))
			{
				searchNW = searchNE = searchSE = searchSW = true;
			}
			else
			{
				switch (range.getUpLeft().relativePosTo(point))
				{
					case EQUAL:
					case NORTH_WEST:
						searchNW = true;
						searchNE = true;
						searchSW = true;
						break;
					case NORTH_EAST:
						searchNE = true;
						searchSW = true;
						break;
					case SOUTH_WEST:
						searchSE = true;
						searchSW = true;
						break;
					case SOUTH_EAST:
						searchSE = true;
						break;
				}

				switch (range.getBottomRight().relativePosTo(point))
				{
					case EQUAL:
					case NORTH_WEST:
						searchNE &= false;
						searchSE &= false;
						searchSW &= false;
						break;
					case NORTH_EAST:
						searchSE &= false;
						searchSW &= false;
						break;
					case SOUTH_WEST:
						searchNE &= false;
						searchSE &= false;
						break;
					case SOUTH_EAST:
						searchNW &= false;
						break;
				}
			}

			if (searchNW)
				this.nw.find2D(range, results);
			if (searchNE)
				this.ne.find2D(range, results);
			if (searchSE)
				this.se.find2D(range, results);
			if (searchSW)
				this.sw.find2D(range, results);

			return results;
		}
	}

	public class Leaf extends Node
	{
		protected Value value;

		public Leaf(QuadPoint<Key> point, QuadRange<Key> range, Value value)
		{
			super(point, range);
			this.value = value;
		}

		@Override
		public Node insert(QuadPoint<Key> point, QuadRange<Key> range, Value value)
		{
			Node node;
			if (point.relativePosTo(this.point) != QuadPoint.CompareResult.NORTH_WEST ||
					(this.value == null && mergeNull))
			{
				node = new InnerNode(this.point, this.range);
				if (!(this.value == null && mergeNull))
				{
					node.insert(this.point, this.range, this.value);
				}
				node.insert(point, range, value);
			}
			else
			{
				try
				{
					Mergeable<Value> mergeable = (Mergeable<Value>) value;
					Value mergedValue = mergeable.merge(value);

					node = new Leaf(this.point, this.range, mergedValue);
				}
				catch (ClassCastException | MergeImpossibleException e)
				{
					node = new InnerNode(point, this.range);
					node.insert(point, range, value);
					node.insert(this.point, this.range, this.value);
				}
			}
			return node;
		}

		@Override
		public List<Value> find2D(QuadRange<Key> range, List<Value> results)
		{
			if (range.contains(point) || range.intersectsWith(this.range))
			{
				results.add(value);
			}
			return results;
		}
	}

	public class EmptyLeaf extends Node
	{
		public EmptyLeaf()
		{
			super(null, null);
		}

		@Override
		public Node insert(QuadPoint<Key> point, QuadRange<Key> range, Value value)
		{
			return new Leaf(point, range, value);
		}

		@Override
		public List<Value> find2D(QuadRange<Key> range, List<Value> results)
		{
			return results;
		}

		@Override
		public String toString()
		{
			return "(empty)";
		}
	}

	protected Node rootNode;
	protected QuadRange<Key> treeRange;

	protected boolean mergeNull;

	public QuadTree(QuadRange<Key> treeRange)
	{
		this(treeRange, true);
	}

	public QuadTree(QuadRange<Key> treeRange, boolean mergeNull)
	{
		this.treeRange = treeRange;
		this.mergeNull = mergeNull;
		rootNode = new EmptyLeaf();
	}

	public void insert(QuadPoint<Key> key, Value value)
	{
		if (!treeRange.contains(key))
		{
			throw new IndexOutOfBoundsException("Key (" + key + ") not in range (" + treeRange + ") of this tree!");
		}
		rootNode = rootNode.insert(key, treeRange, value);
	}

	public List<Value> find(QuadRange<Key> range)
	{
		ArrayList<Value> results = new ArrayList<>();
		rootNode.find2D(range, results);

		return results;
	}
}
