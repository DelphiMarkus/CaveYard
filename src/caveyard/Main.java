package caveyard;

import caveyard.assets.MapKey;
import caveyard.assets.ScriptLoader;
import caveyard.map.Map;
import caveyard.map.MapControl;
import caveyard.assets.MapLoader;
import caveyard.map.MapManager;
import com.jme3.app.SimpleApplication;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Node;
import com.jme3.scene.control.CameraControl;
import com.jme3.scene.control.LightControl;
import com.jme3.shadow.PointLightShadowFilter;
import com.jme3.shadow.PointLightShadowRenderer;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 *
 * @author Maximilian Timmerkamp
 */
public class Main extends SimpleApplication
{
	protected static Logger logger = Logger.getLogger(Main.class.getName());

	protected MapManager mapManager;
	protected Map currentMap;

	protected DirectionalLight sun;
	protected PointLight playerLight;
	protected Node player;

	public static void main(String[] args)
	{
		Main app = new Main();
		app.start();

		//ConsoleDialogListenerTest.main(args);
	}

	private void initLoaders()
	{
		assetManager.registerLoader(ScriptLoader.class, "js");
		assetManager.registerLoader(MapLoader.class, "map.xml");
	}

	@Override
	public void simpleInitApp()
	{
		Logger rootLogger = Logger.getLogger("caveyard");
		rootLogger.setLevel(Level.FINER);
		rootLogger.setUseParentHandlers(false);
		ConsoleHandler consoleHandler = new ConsoleHandler();
		consoleHandler.setLevel(Level.FINER);
		rootLogger.addHandler(consoleHandler);


		logger.fine("TEST!");

		initLoaders();
		mapManager = MapManager.getInstance(assetManager);

		initTestMap();
	}

	private void initTestMap()
	{
		logger.info("Loading map...");
		currentMap = mapManager.loadMap("test_map1");
		//currentMap = (Map) assetManager.loadAsset(new MapKey("Data/map/test.map.xml"));
		logger.fine("Map loaded!");
		currentMap.attachTo(rootNode);

		player = new Node("Player");
		rootNode.attachChild(player);

		CameraControl playerCamControl = new CameraControl(cam, CameraControl.ControlDirection.CameraToSpatial);
		player.addControl(playerCamControl);

		// add light
		playerLight = new PointLight();
		playerLight.setRadius(20);
		rootNode.addLight(playerLight);

		// create a node which position is copied to the light's position
		Node lightNode = new Node();
		lightNode.setLocalTranslation(0, 0, 3);
		player.attachChild(lightNode);

		LightControl playerLightControl = new LightControl(playerLight, LightControl.ControlDirection.SpatialToLight);
		lightNode.addControl(playerLightControl);


		MapControl mapControl = new MapControl(currentMap, 15, 0.25f);
		player.addControl(mapControl);


		this.cam.lookAt(new Vector3f(0, 0, 0), Vector3f.UNIT_Y);
		this.flyCam.setMoveSpeed(5);

		/* Drop shadows */
		final int SHADOW_MAP_SIZE = 1024;
		PointLightShadowRenderer plsr = new PointLightShadowRenderer(assetManager, SHADOW_MAP_SIZE);
		plsr.setLight(playerLight);
		viewPort.addProcessor(plsr);

		PointLightShadowFilter plsf = new PointLightShadowFilter(assetManager, SHADOW_MAP_SIZE);
		plsf.setLight(playerLight);
		plsf.setEnabled(true);
		FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
		fpp.addFilter(plsf);
		viewPort.addProcessor(fpp);
	}

	@Override
	public void simpleUpdate(float tpf)
	{

	}

	@Override
	public void simpleRender(RenderManager rm)
	{
		//TODO: add render code
	}
}
