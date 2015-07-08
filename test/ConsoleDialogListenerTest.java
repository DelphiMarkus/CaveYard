import caveyard.dialog.DialogListener;
import caveyard.dialog.DialogPlayer;
import caveyard.dialog.DialogText;
import caveyard.xml.dialog.Dialogs;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.xml.bind.JAXB;
import java.io.File;
import java.util.List;
import java.util.Scanner;

/**
 * A simple text interface to test dialogs.
 *
 * @author Maximilian Timmerkamp
 */
public class ConsoleDialogListenerTest implements DialogListener
{
	protected DialogPlayer player;
	protected boolean running = false;

	public ConsoleDialogListenerTest(File file, ScriptEngine engine)
	{
		Dialogs dialogs = JAXB.unmarshal(file, Dialogs.class);

		player = new DialogPlayer(dialogs.getDialog().get(0), engine);
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
	public void displayText(DialogText text)
	{
		System.out.println(text.getSpeaker().getExternalName() + ": " + text.getText());
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
	public void onDialogEnded()
	{
		running = false;
		System.out.println("\nDialog ended.\n.............................................................\n");
	}

	public static void main(String[] args)
	{
		File file = new File("assets/Data/dialog/dialogs_test.dlg.xml");

		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName("JavaScript");

		new ConsoleDialogListenerTest(file, engine).run();
	}
}
