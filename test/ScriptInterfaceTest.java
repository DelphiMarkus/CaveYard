import caveyard.quest.scriptinterface.QuestInterface;
import caveyard.quest.scriptinterface.StateInterface;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;


public class ScriptInterfaceTest
{
	public static void main(String[] args)
	{
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName("JavaScript");

		try
		{
			engine.eval(new InputStreamReader(new FileInputStream("assets/Scripts/interface_test.js")));
		} catch (ScriptException e)
		{
			e.printStackTrace();
		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}

		QuestInterface questInterface = (QuestInterface) engine.get("interface");
		StateInterface s = questInterface.getStateInterface(1);

		System.out.println(s);
		long start = System.currentTimeMillis();
		for (int i = 0; i < 1; i++)
		{
			s.enterState();
		}
		long end = System.currentTimeMillis();
		System.out.println("time: " + (end-start) + " ms");

		System.out.println(engine.get("QuestInterface"));
	}
}
