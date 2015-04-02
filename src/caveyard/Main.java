package caveyard;

import caveyard.assets.MapLoader;
import caveyard.assets.ScriptLoader;
import caveyard.map.*;
import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.input.ChaseCamera;
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
import com.jme3.scene.Mesh;
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
public class Main extends SimpleApplication implements ActionListener
{
	protected static Logger LOGGER = Logger.getLogger(Main.class.getName());

	protected BulletAppState bulletAppState;

	protected MapManager mapManager;
	protected Map currentMap;

	protected DirectionalLight sun;
	protected PointLight playerLight;

	protected Node player;
	protected BetterCharacterControl playerControl;

	//protected CameraNode camNode;

	private Vector3f walkDirection = new Vector3f(0, 0, 0);
	private boolean left;
	private boolean right;
	private boolean forwards;
	private boolean backwards;

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

		setupKeys();


		flyCam.setEnabled(false);

		ChaseCamera chaseCam = new ChaseCamera(cam, player, inputManager);
		chaseCam.setInvertVerticalAxis(true);
		chaseCam.setSmoothMotion(true);
		chaseCam.setDragToRotate(false);

		chaseCam.setMinDistance(2.0f);
		chaseCam.setDownRotateOnCloseViewOnly(false);
	}

	private void initTestMap()
	{
		bulletAppState = new BulletAppState();
		bulletAppState.setThreadingType(BulletAppState.ThreadingType.PARALLEL);
		stateManager.attach(bulletAppState);
		//bulletAppState.setDebugEnabled(true);

		// create player node
		Node playerNode = new Node("PlayerNode");
		player = new Node("Player");
		player.setLocalTranslation(0, 2, 0);
		rootNode.attachChild(playerNode);
		playerNode.attachChild(player);

		// create cylinder which represents the player
		Geometry cylinder = new Geometry("playerBox");
		Mesh boxMesh = new Cylinder(10, 10, 0.5f, 3, true);
		Material material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		material.setColor("Color", ColorRGBA.Blue);
		cylinder.setMaterial(material);
		cylinder.setMesh(boxMesh);
		cylinder.rotate(FastMath.HALF_PI, 0, 0);
		cylinder.setLocalTranslation(0, 1.55f, 0);
		playerNode.attachChild(cylinder);

		// load map
		LOGGER.info("Loading map...");
		currentMap = mapManager.loadMap("test_map1");
		LOGGER.fine("Map loaded!");

		MapNode mapNode = currentMap.getMapNode();

		// create a map control to update visible cells
		MapLODControl mapLODControl = new MapLODControl(player, 20, 5);
		mapNode.addControl(mapLODControl);

		MapTerrainPhysicsControl mapPhysics = new MapTerrainPhysicsControl(bulletAppState.getPhysicsSpace());
		mapNode.addControl(mapPhysics);

		rootNode.attachChild(mapNode);

		// add light
		playerLight = new PointLight();
		playerLight.setRadius(20);
		rootNode.addLight(playerLight);

		// create a node which position is copied to the light's position
		Node lightNode = new Node();
		lightNode.setLocalTranslation(0, 0, 1);
		LightControl playerLightControl = new LightControl(playerLight, LightControl.ControlDirection.SpatialToLight);
		lightNode.addControl(playerLightControl);
		player.attachChild(lightNode);

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


		playerControl = new BetterCharacterControl(0.5f, 3, 10);
		playerControl.setGravity(new Vector3f(0, -9.81f, 0));
		playerControl.setJumpForce(new Vector3f(0, 60, 0));
		playerNode.addControl(playerControl);
		playerControl.warp(new Vector3f(0, 10, 0));

		bulletAppState.getPhysicsSpace().add(playerControl);
	}

	private void setupKeys() {
		inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
		inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
		inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_W));
		inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_S));
		inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
		inputManager.addListener(this, "Left");
		inputManager.addListener(this, "Right");
		inputManager.addListener(this, "Up");
		inputManager.addListener(this, "Down");
		inputManager.addListener(this, "Jump");
	}

	@Override
	public void simpleUpdate(float tpf)
	{
		inputManager.setCursorVisible(false);

		Vector3f modelForwardDir = cam.getDirection();
		Vector3f modelLeftDir = cam.getLeft();

		walkDirection.set(0, 0, 0);
		if (left) {
			walkDirection.addLocal(modelLeftDir);
		} else if (right) {
			walkDirection.addLocal(modelLeftDir.negate());
		}
		if (forwards) {
			walkDirection.addLocal(modelForwardDir);
		} else if (backwards) {
			walkDirection.addLocal(modelForwardDir.negate());
		}
		if (walkDirection.lengthSquared() != 0)
		{
			walkDirection.setY(0);
			walkDirection.divideLocal(walkDirection.length()).multLocal(3);

			//playerControl.setViewDirection(cam.getDirection());
			playerControl.setViewDirection(walkDirection);
		}
		playerControl.setWalkDirection(walkDirection);
	}

	@Override
	public void simpleRender(RenderManager rm)
	{
	}

	@Override
	public void onAction(String name, boolean isPressed, float tpf)
	{
		if (name.equals("Left"))
		{
			left = isPressed;
		}
		else if (name.equals("Right"))
		{
			right = isPressed;
		}
		else if (name.equals("Up"))
		{
			forwards = isPressed;
		}
		else if (name.equals("Down"))
		{
			backwards = isPressed;
		}
		else if (name.equals("Jump"))
		{
			playerControl.jump();
		}
	}
}
