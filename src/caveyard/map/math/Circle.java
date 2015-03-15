package caveyard.map.math;

import com.jme3.math.FastMath;

/**
 * @author Maximilian Timmerkamp
 */
public class Circle implements Area
{
	protected float x;
	protected float y;

	protected float r;

	public Circle(float x, float y, float r)
	{
		this.x = x;
		this.y = y;
		this.r = r;
	}

	@Override
	public boolean intersectsWith(Area area)
	{
		if (area instanceof Rect)
			return intersectsWith((Rect) area);
		else if (area instanceof Circle)
			return this.intersectsWith((Circle) area);
		throw new RuntimeException("Unknown class: " + area.getClass().getName());
	}

	public boolean intersectsWith(Circle other)
	{
		return FastMath.sqr(this.x - other.x) + FastMath.sqr(this.y - other.y) <= FastMath.sqr(this.r + other.r);
	}

	public boolean intersectsWith(Rect other)
	{
		if (other.getX1() <= this.x && this.x <= other.getX2())
		{
			return other.getY1() <= this.y && this.y <= other.getY2() ||
					FastMath.abs(this.y - other.getY1()) <= this.r || FastMath.abs(this.y - other.getY2()) <= this.r;
		}
		else if (other.getY1() <= this.y && this.y <= other.getY2())
		{
			return FastMath.abs(this.x - other.getX1()) <= this.r || FastMath.abs(this.x - other.getX2()) <= this.r;
		}
		else
		{
			float dx, dy;

			if (other.getX1() > this.x) dx = other.getX1() - this.x;
			else dx = other.getX2() - this.x;
			if (other.getY1() > this.y) dy = other.getY1() - this.y;
			else dy = other.getY2() - this.y;

			return FastMath.sqr(dx) + FastMath.sqr(dy) <= FastMath.sqr(this.r);
		}
	}
}
