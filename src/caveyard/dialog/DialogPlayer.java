package caveyard.dialog;

import caveyard.xml.dialog.*;

import java.util.*;
import java.util.logging.Logger;

/**
 * @author Maximilian Timmerkamp
 */
public class DialogPlayer
{
	private enum PlayerState
	{
		STOPPED, WAIT_SEQUENCE_RESPONSE, WAIT_CHOICE_RESPONSE, RUNNING;
	}

	public final String COND_ELSE = "__ELSE__";

	protected static final Logger LOGGER = Logger.getLogger(DialogPlayer.class.getName());

	protected DialogType dialog;
	protected Map<String, DialogPartType> dialogParts;
	protected Map<String, DialogPerson> persons;

	protected Deque<Object> stack;
	protected PlayerState state;

	protected MenuType currentMenu;
	protected List<OptionType> currentMenuOptions;

	protected DialogListener listener;

	public DialogPlayer(DialogType dialog)
	{
		this.dialog = dialog;

		this.dialogParts = new HashMap<>();
		generatePartsMap();
		this.persons = new HashMap<>();
		generatePersonsMap();

		this.stack = new ArrayDeque<>();

		this.state = PlayerState.STOPPED;
		currentMenu = null;
	}

	public DialogListener getListener()
	{
		return listener;
	}

	public void setListener(DialogListener listener)
	{
		this.listener = listener;
	}

	protected void generatePartsMap()
	{
		for (DialogPartType part: dialog.getSpeech().getSequenceOrMenuOrChoice())
		{
			if (dialogParts.containsKey(part.getId()))
			{
				LOGGER.warning("Dialog " + dialog.getId() + " has multiple occurences " +
						"of ID '" + part.getId() + "'.");
			}
			dialogParts.put(part.getId(), part);
		}
	}

	protected void generatePersonsMap()
	{
		for (PersonType personType: dialog.getPersons().getPerson())
		{
			DialogPerson person = new DialogPerson(personType.getId(), personType.getExtId());
			persons.put(personType.getId(), person);
		}
	}

	protected DialogPartType getDialogPart(String id)
	{
		DialogPartType part = dialogParts.get(id);

		if (part == null)
		{
			LOGGER.severe("Unknown id was requested: " + dialog.getId());
			throw new RuntimeException("Unknown id was requested.");
		}
		return part;
	}

	public void start()
	{
		String partID = dialog.getStart();
		DialogPartType start = dialogParts.get(partID);

		if (start == null)
		{
			LOGGER.severe("Dialog " + dialog.getId() + " has no valid START-ID!");
			throw new RuntimeException("Dialog has no valid start id!");
		}
		else if (listener == null)
		{
			throw new RuntimeException("No listener added! Cannot perform dialog!");
		}
		else
		{
			if (dialog.getLoad().length() > 0)
			{
				listener.loadScript(dialog.getLoad());
			}
			for (VarType varType: dialog.getVariables().getVar())
			{
				listener.initVariableValue(varType.getName(), varType.getDefault(), varType.isPersistent());
			}

			stack.clear();
			stack.addLast(start);
			state = PlayerState.RUNNING;
			//interpretNext();
		}
	}

	protected void interpretNext()
	{
		while (state == PlayerState.RUNNING)
		{
			if (!stack.isEmpty())
			{
				Object currentPart = stack.pollFirst();

				interpret(currentPart);
			}
			else
			{
				state = PlayerState.STOPPED;
				listener.dialogEnded();
				break;
			}
		}
	}

	protected void interpret(Object currentPart)
	{
		if (currentPart instanceof ActionType)
		{
			String nextID = ((ActionType) currentPart).getNext();
			DialogPartType nextPart = getDialogPart(nextID);
			stack.addFirst(nextPart);
		}
		else if (currentPart instanceof TextType)
		{
			state = PlayerState.WAIT_SEQUENCE_RESPONSE;

			String personID = ((TextType) currentPart).getBy();
			DialogPerson person = persons.get(personID);
			listener.displayText(((TextType) currentPart).getValue(), person);
		}
		else if (currentPart instanceof ScriptType)
		{
			listener.evaluateScript(((ScriptType) currentPart).getValue());
		}
		else if (currentPart instanceof SequenceType)
		{
			List<Object> seqParts = ((SequenceType) currentPart).getTextOrScriptOrAction();
			for (int i = seqParts.size()-1; i >= 0; i--)
			{
				stack.addFirst(seqParts.get(i));
			}
		}
		else if (currentPart instanceof MenuType)
		{
			state = PlayerState.WAIT_CHOICE_RESPONSE;
			currentMenu = (MenuType) currentPart;

			// Evaluate all conditions
			List<OptionType> options = new ArrayList<>();
			for (OptionType option: ((MenuType) currentPart).getOption())
			{
				String conditionString = option.getCondition();

				boolean condition = conditionString == null || conditionString.length() == 0 ||
						conditionString.equals(COND_ELSE) || listener.evaluateCondition(conditionString);
				if (condition)
				{
					options.add(option);
				}
			}
			currentMenuOptions = options;

			if (((MenuType) currentPart).isAuto() && options.size() > 0)
			{
				// do choice the first option
				doInternalChoice(0);
			}
			else
			{
				List<String> strOptions = new ArrayList<>(options.size());
				for (OptionType option: options)
				{
					strOptions.add(option.getValue());
				}

				listener.showMenu(strOptions);
			}
		}
		else
		{
			state = PlayerState.STOPPED;
			throw new RuntimeException("Unknown class. Cannot interpret " + currentPart.getClass().getName());
		}
	}


//	public void continueSequence()
//	{
//		state = PlayerState.RUNNING;
//		interpretNext();
//	}

	protected void doInternalChoice(int index)
	{
		boolean exit = false;
		if (currentMenu instanceof ChoiceType) exit = true;

		OptionType choice = currentMenuOptions.get(index);
		if (choice.isExit()) exit = true;


		if (!exit)
		{
			// return to menu ...
			stack.addFirst(currentMenu);
			// after doing the current selection
		}

		DialogPartType next = getDialogPart(choice.getNext());
		// do the current selection
		stack.addFirst(next);
	}

	public void doChoice(int index)
	{
		doInternalChoice(index);
	}

	public void continueDialog()
	{
		if (state == PlayerState.WAIT_SEQUENCE_RESPONSE || state == PlayerState.WAIT_CHOICE_RESPONSE ||
				state == PlayerState.RUNNING)
		{
			state = PlayerState.RUNNING;
			interpretNext();
		}
	}
}
