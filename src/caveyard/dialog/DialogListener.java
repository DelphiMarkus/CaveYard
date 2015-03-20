package caveyard.dialog;

import java.util.List;

/**
 * @author Maximilian Timmerkamp
 */
public interface DialogListener
{
	public void displayText(String text, DialogPerson person);
	public void showMenu(List<String> options);
	//public void showChoice(String... options);
	public void dialogEnded();

	public void loadScript(String filename);
	public void evaluateScript(String script);
	public boolean evaluateCondition(String condition);
	public void initVariableValue(String var, String expr, boolean isPersistent);
}
