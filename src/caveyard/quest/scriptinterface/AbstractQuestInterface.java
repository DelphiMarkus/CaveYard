package caveyard.quest.scriptinterface;

import caveyard.quest.Quest;
import com.jme3.export.InputCapsule;
import com.jme3.export.OutputCapsule;

/**
 * @author Maximilian Timmerkamp
 */
public abstract class AbstractQuestInterface implements QuestInterface
{
	protected Quest quest;

	public AbstractQuestInterface()
	{
		this.quest = null;
	}


	@Override
	public void saveQuest(OutputCapsule out)
	{
	}

	@Override
	public void loadQuest(InputCapsule in)
	{
	}

	@Override
	public void initQuest()
	{
	}

	@Override
	public Quest getQuest()
	{
		return quest;
	}

	public void setQuest(Quest quest)
	{
		this.quest = quest;
	}
}
