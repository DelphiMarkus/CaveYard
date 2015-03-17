package caveyard.assets;

import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetLoader;
import java.io.IOException;
import java.util.Scanner;

/**
 *
 * @author Maximilian Timmerkamp
 */
public class ScriptLoader implements AssetLoader
{

    public Object load(AssetInfo assetInfo) throws IOException
    {
        Scanner scanner = new Scanner(assetInfo.openStream());
        StringBuilder builder = new StringBuilder();
        
        while (scanner.hasNextLine())
        {
            builder.append(scanner.nextLine());
        }
        
        return builder.toString();
    }
    
}
