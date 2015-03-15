package caveyard.map;

import caveyard.map.math.Rect;

import java.util.HashSet;
import java.util.Stack;

/**
 * Stores Cells in a binary tree and can find all cells in a given area.
 * Cells can be added but not removed. This tree implementation does not
 * do any balancing.
 *
 * @author Maximilian Timmerkamp
 */
public class CellTree
{
	private static abstract class Node
	{
		protected Rect rect;

		public Node(Rect rect)
		{
			this.rect = rect;
		}

		/***
		 * Find all cells which areas intersect with area.
		 *
		 * @param area Area in which to search for cells.
		 * @return All cells in given area.
		 */
		public abstract HashSet<Cell> find(Rect area);

		public abstract Node insert(Rect rect, Cell cell, Stack<Rect> checked_rects);
	}

	private static class InnerNode extends Node
	{
		protected Node left;
		protected Node right;

		public InnerNode(Rect rect, Node left, Node right)
		{
			super(rect);
			this.left = left;
			this.right = right;
		}

		@Override
		public HashSet<Cell> find(Rect area)
		{
			HashSet<Cell> cells;

			cells = right.find(area);
			if (rect.intersectsWith(area))
			{
				HashSet<Cell> cells_left = left.find(area);

				if (cells == null) cells = cells_left;
				else if (cells_left != null) cells.addAll(cells_left);
			}

			return cells;
		}

		@Override
		public Node insert(Rect rect, Cell cell, Stack<Rect> checked_rects)
		{
			checked_rects.push(this.rect);

			if (this.rect.intersectsWith(rect, true))
			{
				this.left = this.left.insert(rect, cell, checked_rects);
			}
			this.right = this.right.insert(rect, cell, checked_rects);

			checked_rects.pop();

			return this;
		}
	}

	private static class Leaf extends Node
	{
		protected HashSet<Cell> cells;

		private Leaf(Rect rect, Cell cell)
		{
			super(rect);
			this.cells = new HashSet<>();
			this.cells.add(cell);
		}

		@Override
		public HashSet<Cell> find(Rect rect)
		{
			if (rect.intersectsWith(this.rect) && cells.size() != 0)
				return new HashSet<>(cells);
			return null;
		}

		@Override
		public Node insert(Rect rect, Cell cell, Stack<Rect> checked_rects)
		{
			Node node;

			if (this.rect.intersectsWith(rect) && this.rect.getIntersection(rect).hasArea() &&
					!this.rect.contains(rect))
			{

				Rect intersectionRect = this.rect.getIntersection(rect);
				Leaf intersectionLeaf = new Leaf(intersectionRect, cell);
				intersectionLeaf.cells.addAll(this.cells);
				InnerNode intersectionNode = new InnerNode(intersectionRect, intersectionLeaf, this);

				if (checked_rects.contains(this.rect))
				{
					node = intersectionNode;
					if (checked_rects.contains(node.rect))
						node = intersectionLeaf;
				}
				else
				{
					Leaf right = new Leaf(rect, cell);
					node = new InnerNode(this.rect, intersectionNode, right);
				}
			}
			else
			{
				Leaf left = new Leaf(rect, cell);
				Leaf right = this;

				node = new InnerNode(rect, left, right);
			}

			return node;
		}
	}

	protected Node root;

	public CellTree()
	{
		this.root = null;
	}

	/***
	 * Finds all Cells in a given area.
	 *
	 * @param rect Area to search for influencing cells.
	 * @return All cells influencing rect.
	 */
	public HashSet<Cell> find(Rect rect)
	{
		if (root != null)
		{
			HashSet<Cell> cells = root.find(rect);
			if (cells != null) return cells;
			else return new HashSet<>(0);
		}
		return null;
	}

	/***
	 * Inserts a cell at a specific area.
	 *
	 * @param rect Area which is influenced by 'cell'.
	 * @param cell Cell to insert.
	 */
	public void insert(Rect rect, Cell cell)
	{
		if (root == null)
		{
			root = new Leaf(rect, cell);
		}
		else
		{
			root = root.insert(rect, cell, new Stack<Rect>());
		}
	}
}
