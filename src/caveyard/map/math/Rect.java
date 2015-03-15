package caveyard.map.math;

import com.jme3.math.Vector2f;

import java.util.Arrays;

/**
 * Simple implementation of a rectangle which sides are parallel to the x-axis and y-axis.
 *
 * @author Maximilian Timmerkamp
 */
public class Rect implements Area
{
	private float x1, y1, x2, y2;

	public Rect(float x1, float y1, float x2, float y2)
	{
		this.x1 = Math.min(x1, x2);
		this.y1 = Math.min(y1, y2);
		this.x2 = Math.max(x1, x2);
		this.y2 = Math.max(y1, y2);
	}

	public Rect(Vector2f topleft, Vector2f bottomright)
	{
		this(topleft.x, topleft.y, bottomright.x, bottomright.y);
	}

	public float getX1()
	{
		return x1;
	}

	public float getY1()
	{
		return y1;
	}

	public float getX2()
	{
		return x2;
	}

	public float getY2()
	{
		return y2;
	}

	public float getSizeX()
	{
		return x2-x1;
	}

	public float getSizeY()
	{
		return y2-y1;
	}

	@Override
	public boolean intersectsWith(Area area)
	{
		if (area instanceof Rect)
			return intersectsWith((Rect) area);
		else if (area instanceof Circle)
			return ((Circle) area).intersectsWith(this);
		throw new RuntimeException("Unknown class: " + area.getClass().getName());
	}

	public boolean intersectsWith(Rect other)
	{
		return !(this.y1 > other.y2 || this.y2 < other.y1 ||
				this.x1 > other.x2 || this.x2 < other.x1);
	}

	public boolean intersectsWith(Rect other, boolean strict)
	{
		return this.intersectsWith(other) && (!strict ||
				!(y1 == other.y2 || y2 == other.y1 || x1 == other.x2 || x2 == other.x1));
	}
	public boolean intersectsWith(Circle other)
	{
		return other.intersectsWith(this);
	}
	
	public boolean contains(Rect other)
	{
		return (this.x1 <= other.x1 && this.x2 >= other.x2 &&
				this.y1 <= other.y1 && this.y2 >= other.y2);
	}

	public Rect getIntersection(Rect other)
	{
		float[] xValues = new float[] {this.x1, this.x2, other.x1, other.x2};
		float[] yValues = new float[] {this.y1, this.y2, other.y1, other.y2};

		Arrays.sort(xValues);
		Arrays.sort(yValues);

		final float newX1 = xValues[1];
		final float newX2 = xValues[2];
		final float newY1 = yValues[1];
		final float newY2 = yValues[2];

		return new Rect(newX1, newY1, newX2, newY2);
	}

	public boolean equals(Rect other)
	{
		return (this.x1 == other.x1 && this.x2 == other.x2 &&
				this.y1 == other.y1 && this.y2 == other.y2);
	}

	public boolean hasArea()
	{
		return (x2 - x1) * (y2 - y1) != 0;
	}
}
