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

		for (IncludeType includeType: textsType.getInclude())
		{
			loadTexts(includeType.getFile());
		}
	}

	public static TextManager getInstance(AssetManager assetManager)
	{
		if (instance == null)
		{
			instance = new TextManager(assetManager);
			instance.loadTexts();
		}
		return instance;
	}

	public static TextManager getInstance()
	{
		return instance;
	}

	public String getText(String textID)
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

}
