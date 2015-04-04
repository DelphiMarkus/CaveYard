package caveyard.util;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Maximilian Timmerkamp
 */
public class QuadTree<Key extends Comparable<Key>, Value>
{
	private abstract class Node
	{
		protected QuadPoint<Key> point; // x- and y-coordinates

		protected Node(QuadPoint<Key> point)
		{
			this.point = point;
		}

		public abstract Node insert(QuadPoint<Key> point, Value value);
		public abstract List<Value> find2D(QuadRange<Key> range, List<Value> results);
	}

	private class InnerNode extends Node
	{
		protected Node nw, ne, sw, se;

		public InnerNode(QuadPoint<Key> point)
		{
			super(point);

			nw = new EmptyLeaf();
			ne = new EmptyLeaf();
			sw = new EmptyLeaf();
			se = new EmptyLeaf();
		}

		@Override
		public Node insert(QuadPoint<Key> point, Value value)
		{
			switch (point.relativePosTo(this.point))
			{
				case EQUAL:
				case NORTH_WEST:
					nw = nw.insert(point, value);
					break;
				case NORTH_EAST:
					ne = ne.insert(point, value);
					break;
				case SOUTH_EAST:
					se = se.insert(point, value);
					break;
				case SOUTH_WEST:
					sw = sw.insert(point, value);
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

	private class Leaf extends Node
	{
		protected Value value;

		public Leaf(QuadPoint<Key> point, Value value)
		{
			super(point);
			this.value = value;
		}

		@Override
		public Node insert(QuadPoint<Key> point, Value value)
		{
			InnerNode node;
			if (!(point.relativePosTo(this.point) == QuadPoint.CompareResult.NORTH_WEST))
			{
				node = new InnerNode(this.point);
				node.insert(this.point, this.value);
				node.insert(point, value);
			}
			else
			{
                node = new InnerNode(point);
				node.insert(point, value);
				node.insert(this.point, this.value);
			}
			return node;
		}

		@Override
		public List<Value> find2D(QuadRange<Key> range, List<Value> results)
		{
			if (range.contains(point))
			{
				results.add(value);
			}
			return results;
		}
	}

	private class EmptyLeaf extends Node
	{
		public EmptyLeaf()
		{
			super(null);
		}

		@Override
		public Node insert(QuadPoint<Key> point, Value value)
		{
			return new Leaf(point, value);
		}

		@Override
		public List<Value> find2D(QuadRange<Key> range, List<Value> results)
		{
			return results;
		}
	}

	protected Node rootNode;

	public QuadTree()
	{
		rootNode = new EmptyLeaf();
	}

	public void insert(QuadPoint<Key> key, Value value)
	{
		rootNode = rootNode.insert(key, value);
	}

	public List<Value> find(QuadRange<Key> range)
	{
		ArrayList<Value> results = new ArrayList<>();
		rootNode.find2D(range, results);

		return results;
	}
}
