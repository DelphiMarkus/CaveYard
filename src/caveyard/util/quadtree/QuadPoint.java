package caveyard.util.quadtree;

import com.jme3.math.Vector2f;

/**
 * @author Maximilian Timmerkamp
 */
public class QuadPoint<Key extends Comparable<Key>>
{
	public enum CompareResult
	{
		EQUAL, NORTH_WEST, NORTH_EAST, SOUTH_EAST, SOUTH_WEST
	}

	protected Key x, y;

	public QuadPoint(Key[] point)
	{
		this(point[0], point[1]);
	}

	public QuadPoint(Key x, Key y)
	{
		this.x = x;
		this.y = y;
	}

	public Key getX()
	{
		return x;
	}

	public Key getY()
	{
		return y;
	}

	/**
	 * Computes how this point lies in relation to other.
	 * So if <code>p1.relativePosTo(p2)</code> evaluates to <code>NORTH_EAST</code>,
	 * it means that <code>p1</code> lies in the north east of <code>p2</code>.
	 *
	 *
	 * @param other Point to compare to.
	 * @return This points direction relative to other.
	 */
	public CompareResult relativePosTo(QuadPoint<Key> other)
	{
		if (eq(this.x, other.x) && eq(this.y, other.y)) return CompareResult.EQUAL;
		else if (lessEq(this.x, other.x) && lessEq(this.y, other.y)) return CompareResult.NORTH_WEST;
		else if (lessEq(this.x, other.x) && !lessEq(this.y, other.y)) return CompareResult.SOUTH_WEST;
		else if (!lessEq(this.x, other.x) && lessEq(this.y, other.y)) return CompareResult.NORTH_EAST;
		else if (!lessEq(this.x, other.x) && !lessEq(this.y, other.y)) return CompareResult.SOUTH_EAST;
		return null;
	}

	public boolean isEastOf(QuadPoint<Key> other)
	{
		return !lessEq(this.x, other.x);
	}

	public boolean isWestOf(QuadPoint<Key> other)
	{
		return lessEq(this.x, other.x) || eq(this.x, other.x);
	}

	public boolean isNorthOf(QuadPoint<Key> other)
	{
		return lessEq(this.y, other.y);
	}

	public boolean isSouthOf(QuadPoint<Key> other)
	{
		return !lessEq(this.y, other.y);
	}

	static <Key extends Comparable<Key>>  boolean lessEq(Key k1, Key k2)
	{
		return k1.compareTo(k2) <= 0;
	}

	static <Key extends Comparable<Key>> boolean eq(Key k1, Key k2)
	{
		return k1.compareTo(k2) == 0;
	}

	@Override
	public String toString()
	{
		return "(" + x + ", " + y + ")";
	}

	public static QuadPoint<Float> fromVector2f(Vector2f vector2f)
	{
		return new QuadPoint<>(vector2f.x, vector2f.y);
	}
}
