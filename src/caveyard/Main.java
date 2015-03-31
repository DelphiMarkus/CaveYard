package caveyard;

import caveyard.assets.MapLoader;
import caveyard.assets.ScriptLoader;
import caveyard.map.Map;
import caveyard.map.MapControl;
import caveyard.map.MapManager;
import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
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
		// change key to display stats from F5 to F3.
		inputManager.deleteMapping(INPUT_MAPPING_HIDE_STATS);
		inputManager.addMapping(INPUT_MAPPING_HIDE_STATS, new KeyTrigger(KeyInput.KEY_F3));
		inputManager.addListener(new ActionListener() {
			private boolean show = false;

			@Override
			public void onAction(String name, boolean isPressed, float tpf)
			{
				if (isPressed && stateManager.getState(StatsAppState.class) != null)
				{
					show = !show;
					setDisplayStatView(show);
				}
			}
		}, INPUT_MAPPING_HIDE_STATS);
		// hide stats by default
		this.setDisplayStatView(false);

		initLoaders();
		mapManager = MapManager.getInstance(assetManager);

		initTestMap();
	}

	private void initTestMap()
	{
		logger.info("Loading map...");
		currentMap = mapManager.loadMap("test_map1");
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

		// create a map control to update visible cells
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
