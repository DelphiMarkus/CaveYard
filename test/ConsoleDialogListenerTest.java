import caveyard.dialog.DialogListener;
import caveyard.dialog.DialogPerson;
import caveyard.dialog.DialogPlayer;
import caveyard.xml.dialog.Dialogs;

import javax.script.*;
import javax.xml.bind.JAXB;
import java.io.*;
import java.util.List;
import java.util.Scanner;

/**
 * A simple text interface to test dialogs.
 *
 * @author Maximilian Timmerkamp
 */
public class ConsoleDialogListenerTest implements DialogListener
{
	protected ScriptEngine engine;
	protected ScriptContext context;
	protected Bindings engineScope;

	protected DialogPlayer player;
	protected boolean running = false;

	public ConsoleDialogListenerTest(File file, ScriptEngine engine)
	{
		this.engine = engine;

		//engineScope = new SimpleBindings();
		engineScope = engine.createBindings();

		context = new SimpleScriptContext();
		context.setBindings(engineScope, ScriptContext.ENGINE_SCOPE);

		Dialogs dialogs = JAXB.unmarshal(file, Dialogs.class);
		player = new DialogPlayer(dialogs.getDialog().get(0));

		player.setListener(this);
	}
	
	public void run()
	{
		for (int i = 0; i < 2; i++)
		{
			player.start();
			running = true;
			while (running)
			{
				player.continueDialog();
			}
		}
	}

	@Override
	public void displayText(String text, DialogPerson person)
	{
		System.out.println(person.getExternalName() + ": " + text);
	}

	@Override
	public void showMenu(List<String> options)
	{
		System.out.println("\n ==== MENU ==== ");
		for (int i = 0; i < options.size(); i++)
		{
			System.out.println("[" + (i+1) + "]: " + options.get(i));
		}

		Scanner scanner = new Scanner(System.in);
		int choice;
		while (true)
		{
			System.out.print(">>> ");
			String line = scanner.nextLine();
			try
			{
				choice = Integer.parseInt(line) - 1;
				if (choice >= 0 && choice < options.size())
				{
					break;
				}
				else
				{
					System.out.println("Choice out of range!");
				}
			}
			catch (NumberFormatException e)
			{
				System.out.println("Please enter a number!");
			}
		}

		player.doChoice(choice);
	}

	@Override
	public void dialogEnded()
	{
		running = false;
		System.out.println("\nDialog ended.\n.............................................................\n");
	}

	@Override
	public void loadScript(String filename)
	{
		try
		{
			Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
			engine.eval(reader, context);
		}
		catch (FileNotFoundException | ScriptException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void evaluateScript(String script)
	{
		try
		{
			engine.eval(script, context);
		}
		catch (ScriptException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public boolean evaluateCondition(String condition)
	{
		try
		{
			Object result = engine.eval(condition, context);
			return (Boolean) result;
		}
		catch (ScriptException e)
		{
			e.printStackTrace();
			throw new RuntimeException("Cannot evaluate condition \"" + condition + "\".", e);
		}
	}

	@Override
	public void initVariableValue(String var, String expr, boolean isPersistent)
	{
		if (!isPersistent && engineScope.containsKey(var) || !engineScope.containsKey(var))
		{
			try
			{
				Object value = engine.eval(expr, context);
				engineScope.put(var, value);
			}
			catch (ScriptException e)
			{
				e.printStackTrace();
			}
		}
	}


	public static void main(String[] args)
	{
		File file = new File("assets/Data/dialog/dialogs_test.dlg.xml");

		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName("JavaScript");

		new ConsoleDialogListenerTest(file, engine).run();
	}
}
