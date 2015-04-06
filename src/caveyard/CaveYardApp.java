package caveyard;

import caveyard.assets.MapLoader;
import caveyard.assets.ScriptLoader;
import caveyard.map.*;
import caveyard.states.PlayerControlAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.control.LightControl;
import com.jme3.scene.shape.Cylinder;
import com.jme3.shadow.PointLightShadowFilter;
import com.jme3.shadow.PointLightShadowRenderer;

import java.util.logging.Logger;

/**
 *
 *
 * @author Maximilian Timmerkamp
 */
public class CaveYardApp extends SimpleApplication
{
	protected static Logger LOGGER = Logger.getLogger(CaveYardApp.class.getName());

	protected BulletAppState bulletAppState;
	protected PlayerControlAppState playerAppState;

	protected MapManager mapManager;
	protected Map currentMap;
	protected MapNode mapNode;

	protected DirectionalLight sun;
	protected PointLight playerLight;

	protected Node playerNode;
	protected BetterCharacterControl playerControl;

	public static void main(String[] args)
	{
		CaveYardApp app = new CaveYardApp();
		app.start();
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
		inputManager.addListener(new ActionListener()
		{
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
		bulletAppState = new BulletAppState();
		bulletAppState.setThreadingType(BulletAppState.ThreadingType.PARALLEL);
		stateManager.attach(bulletAppState);
		//bulletAppState.setDebugEnabled(true);

		// create cameraTarget node
		Node cameraTarget = new Node("cameraTarget");
		cameraTarget.setLocalTranslation(0, 2, 0);
		playerAppState = new PlayerControlAppState(cameraTarget);
		playerNode = playerAppState.getPlayerNode();
		stateManager.attach(playerAppState);

		playerControl = new BetterCharacterControl(0.25f, 2, 75);
		playerControl.setGravity(new Vector3f(0, -9.81f, 0));
		playerControl.setJumpForce(new Vector3f(0, 335, 0));
		bulletAppState.getPhysicsSpace().add(playerControl);
		playerAppState.setPlayerControl(playerControl);
		playerControl.warp(new Vector3f(0, 10, 0));


		// create cylinder which represents the player
		Geometry cylinder = new Geometry("playerBox");
		Cylinder mesh = new Cylinder(10, 10, 0.25f, 2, true);
		Material material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		material.setColor("Color", ColorRGBA.Blue);
		material.getAdditionalRenderState().setWireframe(true);
		cylinder.setMaterial(material);
		cylinder.setMesh(mesh);
		cylinder.rotate(FastMath.HALF_PI, 0, 0);
		cylinder.setLocalTranslation(0, mesh.getHeight()/2, 0);
		playerNode.attachChild(cylinder);

		// load map
		LOGGER.info("Loading map...");
		currentMap = mapManager.loadMap("test_map1");
		LOGGER.fine("Map loaded!");

		mapNode = currentMap.getMapNode();

		// create a map control to update visible cells
		MapLODControl mapLODControl = new MapLODControl(playerNode, 20, 5);
		mapNode.addControl(mapLODControl);

		MapTerrainPhysicsControl mapPhysics = new MapTerrainPhysicsControl(bulletAppState.getPhysicsSpace());
		mapNode.addControl(mapPhysics);

		MapObjectsPhysicsControl objectsPhysics = new MapObjectsPhysicsControl(playerNode, 10, 0.5f, bulletAppState.getPhysicsSpace());
		mapNode.addControl(objectsPhysics);

		rootNode.attachChild(mapNode);

		// add light
		playerLight = new PointLight();
		playerLight.setRadius(20);
		rootNode.addLight(playerLight);

		// create a node which position is copied to the light's position
		Node lightNode = new Node();
		lightNode.setLocalTranslation(0, 2, 0);
		LightControl playerLightControl = new LightControl(playerLight, LightControl.ControlDirection.SpatialToLight);
		lightNode.addControl(playerLightControl);
		playerNode.attachChild(lightNode);

		/* Drop shadows */
		final int SHADOW_MAP_SIZE = 512;
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
	}

	public MapNode getMapNode()
	{
		return mapNode;
	}

	public Node getPlayerNode()
	{
		return playerNode;
	}
}
