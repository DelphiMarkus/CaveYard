package caveyard.util;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;

/**
 * This class provides some useful functions when working with vectors.
 *
 * @author Maximilian Timmerkamp
 */
public class VecUtil
{
	/**
	 * Gets the x- and z-coordinates of <code>vec3D</code> and puts them
	 * in a 2d vector.
	 * @param vec3D Vector supplying x- and z-coordinates.
	 * @return 2D vector with x- and z-corrdinates of input vector.
	 */
	public static Vector2f toXZVector(Vector3f vec3D)
	{
		return new Vector2f(vec3D.x, vec3D.z);
	}
}
