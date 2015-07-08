package caveyard.util;

import caveyard.xml.text.IncludeType;
import caveyard.xml.text.TextType;
import caveyard.xml.text.Texts;
import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetManager;

import javax.xml.bind.DataBindingException;
import javax.xml.bind.JAXB;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple Singleton which provides access to the global text database. This singleton must be initialized
 * on game start as it needs to locate the main data file ("{@value #TEXTS_ROOT_FILE}") in the assets folder.
 *
 * After initialization texts can be got by {@link #get(String)} or the static method {@link #getText(String)}.
 * This text manager only supports one text database, therefore it is implemented as singleton.
 *
 * @author Maximilian Timmerkamp
 */
public class TextManager
{
	protected static final Logger LOGGER = Logger.getLogger(TextManager.class.getName());
	protected final static String TEXTS_ROOT_FILE = "Data/texts/texts.xml";

	private static TextManager instance;
	protected AssetManager assetManager;

	protected HashMap<String, String> texts;

	private TextManager(AssetManager assetManager)
	{
		this.assetManager = assetManager;
		texts = new HashMap<>();
	}

	private void loadTexts()
	{
		loadTexts(TEXTS_ROOT_FILE);
	}

	private void loadTexts(String filename)
	{
		AssetKey<Texts> key = new AssetKey<>(filename);
		AssetInfo info = assetManager.locateAsset(key);

		if (info == null)
		{
			LOGGER.warning("Cannot load texts recource file\"" + filename + "\": No such file or directory.");
			return;
		}

		Texts textsType;
		try
		{
			textsType = JAXB.unmarshal(info.openStream(), Texts.class);
		}
		catch (DataBindingException e)
		{
			LOGGER.severe("Cannot load texts resource file \"" + filename + "\": Error reading XML.");
			LOGGER.throwing(TextManager.class.getName(), "loadTexts", e);
			return;
		}

		// read all texts, then read all other files.
		for (TextType textType: textsType.getText())
		{
			if (texts.containsKey(textType.getUid()))
			{
				LOGGER.warning("Text UID \"" + textType.getUid() + "\" used more than once!");
			}
			texts.put(textType.getUid(), textType.getValue());
		}
		textsType.getText().clear(); // clear memory as we do not need the texts any more

		// XXX:  loading! Does not detect cyclic includes.
		for (IncludeType includeType: textsType.getInclude())
		{
			loadTexts(includeType.getFile());
		}
	}

	/**
	 * Initializes the test manager and returns the instance. Please note that all texts are loaded into RAM
	 * when initializing the text manager.
	 *
	 * @param assetManager used to locate the main texts data file.
	 * @return the text manager instance.
	 */
	public static TextManager getInstance(AssetManager assetManager)
	{
		if (instance == null)
		{
			instance = new TextManager(assetManager);
			instance.loadTexts();
		}
		return instance;
	}

	/**
	 * Returns the text manager. Returns null if the text manager was not initialized yet.
	 * @return the text manager or null.
	 */
	public static TextManager getInstance()
	{
		return instance;
	}

	/**
	 * Returns a text from the text database to the given {@code textID}. If the text cannot be found,
	 *  {@code "{<textID>}"} is returned.
	 * @param textID id of text to return.
	 * @return text to specified textID.
	 */
	public String get(String textID)
	{
		String text = texts.get(textID);
		if (text == null)
		{
			if (LOGGER.isLoggable(Level.INFO))
			{
				LOGGER.info("textId not found: \"" + textID + "\". Using placeholder.");
			}
			return "{" + textID + "}";
		}
		else
		{
			return text;
		}
	}

	/**
	 * Returns the text to the specified textID. Uses {@link #get(String)} internally.
	 *
	 * @param textID id of text to return.
	 * @return text to specified textID.
	 *
	 * @see #get(String)
	 */
	public static String getText(String textID)
	{
		return instance.get(textID);
	}
}
