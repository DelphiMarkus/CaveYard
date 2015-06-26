package caveyard.dialog;

import caveyard.xml.dialog.*;

import javax.script.*;
import java.io.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * A DialogPlayer plays (or interprets) a dialog. A {@link DialogListener} is
 * required to communicate with the game to display the dialog or to get input
 * from outside the dialog simulation.
 *
 * This player works like an interpreter. After each communication with the
 * registered listener, a call to {@link #continueDialog()} is required
 * to continue the interpretation. If a choice must be made,
 * {@link #doChoice(int)} is used.
 *
 * @author Maximilian Timmerkamp
 */
public class DialogPlayer
{
	private enum PlayerState
	{
		STOPPED, WAIT_FOR_CONTINUE, WAIT_CHOICE_RESPONSE, RUNNING
	}

	public static final String COND_ELSE = "__ELSE__";

	protected static final Logger LOGGER = Logger.getLogger(DialogPlayer.class.getName());

	/**
	 * Dialog to play / interpret. This is likely to be loaded directly
	 * from a defining xml file.
	 */
	protected DialogType dialog;
	/**
	 * Map of all parts of the dialog. All dialog part ids are mapped
	 * to their parts by this map.
	 */
	protected Map<String, DialogPartType> dialogParts;
	/**
	 * All person ids are mapped to their {@link DialogPerson} objects by this map.
	 */
	protected Map<String, DialogPerson> persons;

	/**
	 * This is used as stack to store all future dialog elements which
	 * need to be interpreted. This stack can shrink or extend every time
	 * {@link #interpret(Object)} is called.
	 */
	protected Deque<Object> stack;
	/**
	 * Current state of the player. Determines if this player is waiting
	 * for any user input, is running or was stopped.
	 */
	protected PlayerState state;

	/**
	 * Last menu which was interpreted.
	 */
	protected MenuType currentMenu;
	/**
	 *  Holds all options of the currently interpreted choice or menu.
	 */
	protected List<OptionType> currentMenuOptions;

	/**
	 * Registered listener of this player. Handles all interfacing to the player of the game.
	 */
	protected DialogListener listener;

	/**
	 * Engine used to evaluate all expressions, statements and conditions.
	 */
	protected ScriptEngine engine;
	/**
	 * The context used when anything needs to be evaluated by the script engine.
	 */
	protected ScriptContext context;
	/**
	 * Bindings used by {@link #context}.
	 */
	protected Bindings engineScope;


	/**
	 * Creates a new player to play or interpret the passed dialog.
	 *
	 * The passed ScriptEngine will not be changed in any way. All
	 * expressions are evaluated using a new <code>ScriptContext</code>
	 * object.
	 *
	 * @param dialog Dialog to play
	 * @param engine engine to use for evaluating expressions
	 */
	public DialogPlayer(DialogType dialog, ScriptEngine engine)
	{
		this.dialog = dialog;

		this.dialogParts = new HashMap<>();
		generatePartsMap();
		this.persons = new HashMap<>();
		generatePersonsMap();

		this.stack = new ArrayDeque<>();

		this.state = PlayerState.STOPPED;
		this.currentMenu = null;

		this.listener = null;
		this.engine = engine;
		engineScope = engine.createBindings();

		context = new SimpleScriptContext();
		context.setBindings(engineScope, ScriptContext.ENGINE_SCOPE);
	}

	/**
	 * Returns the current dialog listener.
	 * @return current listener.
	 */
	public DialogListener getListener()
	{
		return listener;
	}

	/**
	 * Registers a new listener. Without a listener this dialog cannot be started.
	 * @param listener listener to use.
	 *
	 * @see #start()
	 */
	public void setListener(DialogListener listener)
	{
		this.listener = listener;
	}

	/**
	 * Generates a map which maps dialog part ids to DialogParts.
	 */
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

	/**
	 * Generates a map to identify persons in the dialog.
	 */
	protected void generatePersonsMap()
	{
		for (PersonType personType: dialog.getPersons().getPerson())
		{
			DialogPerson person = new DialogPerson(personType.getId(), personType.getExtId());
			persons.put(personType.getId(), person);
		}
	}

	/**
	 * Returns a String for use in exceptions.
	 */
	protected String getExceptionStr()
	{
		return "In Dialog \"" + dialog.getId() + "\": ";
	}

	/**
	 * Returns the requested dialog part or throws an exception if the id
	 * cannot be found.
	 * @param id of the dialog part to search
	 * @return the requested dialog part
	 */
	protected DialogPartType getDialogPart(String id)
	{
		DialogPartType part = dialogParts.get(id);

		if (part == null)
		{
			LOGGER.severe("Unknown id was requested: " + dialog.getId());
			throw new UnknownPartIDException("Unknown id was requested.");
		}
		return part;
	}

	/**
	 * Initialises a variable specified in the dialog definition. Persistant
	 * variables which are already specified are not reinitialised.
	 * @param varType information about the variable
	 */
	protected void initVariable(VarType varType)
	{
		String varName = varType.getName();
		if (!varType.isPersistent() && engineScope.containsKey(varName) || !engineScope.containsKey(varName))
		{
			Object value = null;
			try
			{
				 value = engine.eval(varType.getDefault(), context);
			}
			catch (ScriptException e)
			{
				LOGGER.warning(getExceptionStr() + " Cannot initiate variable \"" + varName + "\" " +
						"(" + e.getMessage() + ")");
				LOGGER.throwing("DialogPlayer", "initVariable", e);
			}
			engineScope.put(varName, value);
		}
	}

	/**
	 * Loads and evaluates a script file.
	 * @param filename script file to load
	 */
	protected void loadScript(String filename)
	{
		try
		{
			Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
			engine.eval(reader, context);
		}
		catch (FileNotFoundException e)
		{
			LOGGER.severe(getExceptionStr() + "Cannot load script file \"" + filename + "\".");
			LOGGER.throwing(this.getClass().getName(), "loadScript", e);
		}
		catch (ScriptException e)
		{
			LOGGER.severe(getExceptionStr() + "Error while evaluating script file \"" + filename + "\".");
			LOGGER.throwing(this.getClass().getName(), "loadScript", e);
		}
	}

	/**
	 * Evaluates a condition for use in menus or choices.
	 * @param condition to evaluate
	 * @return evaluated condition or false if condition could not be evaluated
	 */
	protected boolean evaluateCondition(String condition)
	{
		try
		{
			Object result = engine.eval(condition, context);
			return (Boolean) result;
		}
		catch (ScriptException e)
		{
			LOGGER.severe(getExceptionStr() + "Error while evaluating condition (" + condition + ").");
			LOGGER.throwing(this.getClass().getName(), "evaluateCondition", e);
		}
		return false;
	}

	/**
	 * Evaluates a single statement.
	 * @param script statement to evaluate.
	 */
	protected void evaluateScript(String script)
	{
		try
		{
			engine.eval(script, context);
		}
		catch (ScriptException e)
		{
			LOGGER.severe(getExceptionStr() + "Error while evaluating script line (" + script + ").");
			LOGGER.throwing(this.getClass().getName(), "evaluateScript", e);
		}
	}

	/**
	 * Starts a dialog. All variables defined in the dialog definition are
	 * reset. If specified, the "load" script will be evaluated.
	 *
	 * A DialogListener must be added before calling this method. Otherwise
	 * an Exception is thrown.
	 *
	 * @throws UnknownPartIDException Thrown if start id of dialog is not valid.
	 */
	public void start() throws UnknownPartIDException
	{
		String partID = dialog.getStart();
		DialogPartType start;

		try
		{
			start = dialogParts.get(partID);
		}
		catch (UnknownPartIDException e)
		{
			LOGGER.severe(getExceptionStr() + "No valid START-ID!");
			throw new UnknownPartIDException("Dialog has no valid start id!", e);
		}

		if (listener == null)
		{
			throw new RuntimeException("No listener added! Cannot perform dialog!");
		}
		else
		{
			// load initialisation script (if specified)
			if (dialog.getLoad().length() > 0)
			{
				loadScript(dialog.getLoad());
			}

			// reinit variables
			for (VarType varType: dialog.getVariables().getVar())
			{
				initVariable(varType);
			}

			stack.clear();
			stack.addLast(start);
			state = PlayerState.RUNNING;
		}
	}

	/**
	 * Interprets the next dialog part on stack while the dialog state
	 * is {@link caveyard.dialog.DialogPlayer.PlayerState#RUNNING}.
	 */
	protected void interpretNext()
	{
		while (state == PlayerState.RUNNING)
		{
			if (!stack.isEmpty())
			{
				Object currentPart = stack.pollFirst();

				interpret(currentPart);
			}
			else // stop dialog if stack is empty.
			{
				state = PlayerState.STOPPED;
				listener.onDialogEnded();
				break;
			}
		}
	}

	/**
	 * Interprets one dialog part. This method does most calls to the
	 * listener. The dialog state is likely to be changed by this
	 * method to wait for the listener to react.
	 *
	 * @param currentPart part of the dialog to interpret.
	 */
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
			state = PlayerState.WAIT_FOR_CONTINUE;

			String personID = ((TextType) currentPart).getBy();
			DialogPerson person = persons.get(personID);

			DialogText dialogText = new DialogText(((TextType) currentPart).getValue(), person);

			listener.displayText(dialogText);
		}
		else if (currentPart instanceof ScriptType)
		{
			evaluateScript(((ScriptType) currentPart).getValue());
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
			for (OptionType option: currentMenu.getOption())
			{
				String conditionString = option.getCondition();

				boolean condition = conditionString == null || conditionString.length() == 0 ||
						conditionString.equals(COND_ELSE) || evaluateCondition(conditionString);
				if (condition)
				{
					options.add(option);
				}
			}
			currentMenuOptions = options;

			// do an automatic choice if we are in a Choice and there is one
			// ore more options to make or in a Menu with just one left option.
			// If there is more than one left option, we need the player to decide.
			if (currentMenu.isAuto() && ((currentMenu instanceof ChoiceType) && (options.size() > 0) || options.size() == 1))
			{
				// choice the first option and continue
				doInternalChoice(0);
			}
			else
			{
				// otherwise show all available options to the player

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
			// throw an exception if we don't recognise the dialog part.

			state = PlayerState.STOPPED;
			throw new RuntimeException("Unknown class. Cannot interpret " + currentPart.getClass().getName());
		}
	}

	/**
	 * Does an internal choice of a Menu or Choice. index must be an
	 * index of field {@link #currentMenuOptions}.
	 * @param index item of currentMenu to choice
	 */
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

		state = PlayerState.WAIT_FOR_CONTINUE;
	}

	/**
	 * Tells this player about a choice made by the user in a menu or choice.
	 * This method must be called after each call of
	 * <code>showMenu</code> of the registered <code>DialogListener</code>.
	 * Then {@link #continueDialog()} can be called to continue this dialog.
	 * @param index choice to choose
	 *
	 * @see DialogListener#showMenu(List)
	 * @see #continueDialog()
	 */
	public void doChoice(int index)
	{
		doInternalChoice(index);
	}

	/**
	 * Continues this dialog after <code>showMenu()</code> or <code>displayText()</code>
	 * was called by this player.
	 *
	 * @throws UnknownPartIDException Thrown if an invalid dialog part id is
	 * 		used in the dialog definition.
	 *
	 * @see DialogListener#displayText(DialogText)
	 * @see DialogListener#showMenu(List)
	 */
	public void continueDialog() throws UnknownPartIDException
	{
		if (state == PlayerState.WAIT_FOR_CONTINUE ||
				state == PlayerState.RUNNING)
		{
			state = PlayerState.RUNNING;
			interpretNext();
		}
	}

	// TODO: Implement saving and loading persistent variables to a savegame
}
