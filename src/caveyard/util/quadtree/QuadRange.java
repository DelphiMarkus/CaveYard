package caveyard.util.quadtree;

/**
 * @author Maximilian Timmerkamp
 */
public class QuadRange<Key extends Comparable<Key>>
{
	protected final QuadPoint<Key> upLeft, bottomRight;
	protected final QuadPoint<Key> bottomLeft;

	public QuadRange(QuadPoint<Key> p1, QuadPoint<Key> p2)
	{
		switch (p1.relativePosTo(p2))
		{
			case EQUAL:
			case NORTH_WEST:
				this.upLeft = p1;
				this.bottomRight = p2;
				break;
			case NORTH_EAST:
				this.upLeft = new QuadPoint<>(p2.getX(), p1.getY());
				this.bottomRight = new QuadPoint<>(p1.getX(), p2.getY());
				break;
			case SOUTH_EAST:
				this.upLeft = p2;
				this.bottomRight = p1;
				break;
			case SOUTH_WEST:
				this.upLeft = new QuadPoint<>(p1.getX(), p2.getY());
				this.bottomRight = new QuadPoint<>(p2.getX(), p1.getY());
				break;
			default:
				this.upLeft = null;
				this.bottomRight = null;
		}
		if (upLeft != null && bottomRight != null)
		{
			bottomLeft = new QuadPoint<>(upLeft.x, bottomRight.y);
		}
		else
		{
			bottomLeft = null;
		}
	}

	public QuadPoint<Key> getUpLeft()
	{
		return upLeft;
	}

	public QuadPoint<Key> getBottomRight()
	{
		return bottomRight;
	}

	public boolean contains(QuadPoint<Key> p)
	{
		return p.relativePosTo(upLeft) == QuadPoint.CompareResult.SOUTH_EAST &&
				p.relativePosTo(bottomRight) == QuadPoint.CompareResult.NORTH_WEST ||
				p.relativePosTo(upLeft) == QuadPoint.CompareResult.EQUAL ||
				p.relativePosTo(bottomRight) == QuadPoint.CompareResult.EQUAL;
	}

	public boolean intersectsWith(QuadRange<Key> range)
	{
		return range.contains(this.upLeft) || range.contains(this.bottomRight) ||
				this.contains(range.upLeft) || this.contains(range.bottomRight) ||
				range.contains(this.bottomLeft) || this.contains(range.bottomLeft);
	}

	@Override
	public String toString()
	{
		return "([" + upLeft.x + "; " + bottomRight.x + "], [" + bottomRight.y + "; " + upLeft.y + "])";
	}
}
