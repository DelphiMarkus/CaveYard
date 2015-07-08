package caveyard.quest;

/**
 * @author Maximilian Timmerkamp
 */
public class QuestVariable
{
	public enum Type
	{
		STRING, BOOLEAN, INT, FLOAT, DOUBLE, CUSTOM;
	}

	protected String name;
	protected Type type;
	protected String defaultValue;

	public QuestVariable(String name, Type type)
	{
		this(name, type, null);
	}

	public QuestVariable(String name, Type type, String defaultValue)
	{
		this.name = name;
		this.type = type;
		this.defaultValue = defaultValue;
	}

	public String getName()
	{
		return name;
	}

	public Type getType()
	{
		return type;
	}

	public String getDefaultValue()
	{
		return defaultValue;
	}
}
