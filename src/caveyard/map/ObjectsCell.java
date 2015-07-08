package caveyard.map;

import caveyard.util.MergeImpossibleException;
import caveyard.util.Mergeable;
import com.jme3.math.Vector2f;
import com.jme3.scene.Spatial;

import java.util.HashSet;

/**
 * @author Maximilian Timmerkamp
 */
public class ObjectsCell implements Mergeable<ObjectsCell>
{
	HashSet<Spatial> objects;
	Vector2f pos;
	Vector2f halfSize;

	public ObjectsCell(Vector2f pos, Vector2f halfSize)
	{
		this.pos = pos;
		this.halfSize = halfSize;
		this.objects = new HashSet<>();
	}

	public HashSet<Spatial> getObjects()
	{
		return objects;
	}

	public void setObjects(HashSet<Spatial> objects)
	{
		this.objects = objects;
	}

	public Vector2f getPos()
	{
		return pos;
	}

	public void setPos(Vector2f pos)
	{
		this.pos = pos;
	}

	public Vector2f getHalfSize()
	{
		return halfSize;
	}

	public void setHalfSize(Vector2f halfSize)
	{
		this.halfSize = halfSize;
	}

	public static ObjectsCell fromRect(Vector2f p1, Vector2f p2)
	{
		Vector2f halfSize = new Vector2f(p2.x-p1.x, p2.y-p1.y).divideLocal(2);
		Vector2f center = p1.add(halfSize);
		return new ObjectsCell(center, halfSize);
	}

	@Override
	public ObjectsCell merge(ObjectsCell other)
	{
		if (this.objects.size() != 0)
		{
			throw new MergeImpossibleException("This cell has elements and cannot be merged.");
		}
		ObjectsCell cell = new ObjectsCell(other.pos, other.halfSize);
		cell.objects = other.objects;
		return cell;
	}

	@Override
	public String toString()
	{
		return "C" + pos.toString();
	}
}
