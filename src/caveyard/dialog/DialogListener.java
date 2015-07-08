package caveyard.dialog;

import java.util.List;

/**
 * A DialogListener is used by a {@link DialogPlayer} to interface with
 * the game and the player of the game.
 *
 * @author Maximilian Timmerkamp
 */
public interface DialogListener
{
	/**
	 * Displays a simple text said by the specified person.
	 * A call to {@link DialogPlayer#start()} is necessary to continue the dialog.
	 *
	 * @param text text said by person
	 */
	void displayText(DialogText text);

	/**
	 * Shows a menu from which the player can choose an option. A call to
	 * {@link DialogPlayer#doChoice(int)} is required before continuing
	 * the dialog through {@link DialogPlayer#continueDialog()}.
	 *
	 * @param options Options to display.
	 */
	void showMenu(List<String> options);

	/**
	 * This method is called when the dialog ended. It is possible to restart it again.
	 */
	void onDialogEnded();
}
