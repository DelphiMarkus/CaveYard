package caveyard.states;

import caveyard.CaveYardApp;
import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.collision.Collidable;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.input.ChaseCamera;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.FastMath;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;

import java.util.logging.Logger;

/**
 * This app state handles inputs from the keyboard and moves a player node
 * according to these inputs. The default keymap is W-S-A-D for
 * forward-backwards-left-right. Currently the keymap cannot be changed.
 * Pressing SPACE will cause the player node to jump. Physics are controlled
 * by an externally supplied {@link BetterCharacterControl}.
 * <br>
 * A {@link ChaseCamera} is added by this state. This Control adds some
 * mouse listeners to the input manager for rotating and zooming the camera.
 * The camera used is the app's camera ({@link Application#cam}).
 *
 * @author Maximilian Timmerkamp
 */
public class PlayerControlAppState extends AbstractAppState implements ActionListener
{
	public static final String MOVE_LEFT = "PLAYER_MOVE_LEFT";
	public static final String MOVE_RIGHT = "PLAYER_MOVE_RIGHT";
	public static final String MOVE_FORWARD = "PLAYER_MOVE_UP";
	public static final String MOVE_BACKWARD = "PLAYER_MOVE_DOWN";
	public static final String PLAYER_JUMP = "PLAYER_JUMP";
	public static final String PLAYER_SPRINT = "PLAYER_SPRINT";

	protected static final Logger LOGGER = Logger.getLogger(PlayerControlAppState.class.getName());

	protected CaveYardApp app;

	protected Node playerNode;
	protected BetterCharacterControl playerControl;
	protected Node cameraTarget;
	protected ChaseCamera chaseCam;

	protected boolean left;
	protected boolean right;
	protected boolean forward;
	protected boolean backward;
	protected boolean sprinting;

	/**
	 * Creates a new AppState which listens to keyboard inputs to move
	 * the player node. A {@link ChaseCamera} will be initialized when this
	 * AppState is attached to an AppStateManager. The chase camera will
	 * follow the {@link #cameraTarget}.
	 *
	 * The {@link #playerNode} will be added to the rootNode of the app
	 * when this state is initialized.
	 *
	 * This constructor sets the {@link #playerControl} to null. This attribute
	 * but be set before this state is attached to an app state manager.
	 * Otherwise NullPointerException will occur.
	 *
	 * @param cameraTarget Target the chase camera will follow.
	 */
	public PlayerControlAppState(Node cameraTarget)
	{
		this(cameraTarget, null);
	}

	/**
	 * Creates a new AppState which listens to keyboard inputs to move
	 * the player node. A {@link ChaseCamera} will be initialized when this
	 * AppState is attached to an AppStateManager. The chase camera will
	 * follow the {@link #cameraTarget}.
	 *
	 * The {@link #playerNode} will be added to the rootNode of the app
	 * when this state is initialized.
	 *
	 * @param cameraTarget Target the chase camera will follow.
	 * @param playerControl Physical control of the player node. Will be
	 *                         added to the player node automatically.
	 */
	public PlayerControlAppState(Node cameraTarget, BetterCharacterControl playerControl)
	{
		this.playerControl = playerControl;
		this.cameraTarget = cameraTarget;

		this.playerNode = new Node("playerNode");
		playerNode.attachChild(cameraTarget);
		if (playerControl != null)
		{
			playerNode.addControl(playerControl);
		}
	}

	@Override
	public void initialize(AppStateManager stateManager, Application app)
	{
		super.initialize(stateManager, app);

		this.app = (CaveYardApp) app;
		this.app.getRootNode().attachChild(playerNode);
		initChaseCam();
		setupCameraCollision();

		setEnabled(true);
	}

	private void initChaseCam()
	{
		app.getFlyByCamera().setEnabled(false);

		chaseCam = new ChaseCamera(app.getCamera(), cameraTarget, app.getInputManager());
		chaseCam.setInvertVerticalAxis(true);
		chaseCam.setSmoothMotion(false);
		chaseCam.setDragToRotate(false);
		chaseCam.setChasingSensitivity(100);

		chaseCam.setMinDistance(2.0f);
		chaseCam.setDefaultDistance(5.0f);
		chaseCam.setMinVerticalRotation(-FastMath.QUARTER_PI);

		chaseCam.setDownRotateOnCloseViewOnly(false);
	}

	private class CameraCollisionControl extends AbstractControl
	{
		@Override
		protected void controlUpdate(float tpf)
		{
			Camera camera = app.getCamera();
			Vector3f camPos = camera.getLocation().subtract(cameraTarget.getWorldTranslation());
			camPos.subtractLocal(chaseCam.getLookAtOffset());
			avoidCamObstruction(cameraTarget, app.getMapNode(), camPos.clone(), camPos);
			camera.setLocation(camPos.addLocal(cameraTarget.getWorldTranslation()).addLocal(chaseCam.getLookAtOffset()));
		}

		@Override
		protected void controlRender(RenderManager rm, ViewPort vp)
		{
		}
	}

	private void setupCameraCollision()
	{
		CameraCollisionControl control = new CameraCollisionControl();
		cameraTarget.addControl(control);
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		super.setEnabled(enabled);

		if (enabled)
		{
			setupKeys();
		}
		else
		{
			disableKeys();
		}
	}

	/**
	 * Gets the player node of this state. This node has the
	 * {@link #cameraTarget} and the {@link #playerControl} attached.
	 * @return The player node of this state.
	 */
	public Node getPlayerNode()
	{
		return playerNode;
	}

	/**
	 * Gets the spatial which is followed by the chase camera. It is attached
	 * to the {@link #playerNode}.
	 * @return Node followed by chase camera.
	 */
	public Node getCameraTarget()
	{
		return cameraTarget;
	}

	/**
	 * Gets the chase camera which follows the {@link #cameraTarget}.
	 * @return This state's chase camera.
	 */
	public ChaseCamera getChaseCam()
	{
		return chaseCam;
	}

	/**
	 * Gets the physical control of the {@link #playerNode}.
	 * @return The character control of the player node.
	 */
	public BetterCharacterControl getPlayerControl()
	{
		return playerControl;
	}

	/**
	 * Adds the passed BetterCharacterControl to the {@link #playerNode}
	 * or removes it when equals null. Beware: a call to {@link #update(float)}
	 * by the state manager will likely cause a NullPointerException if the
	 * playerControl is not set.
	 * @param playerControl Physical control of the player node.
	 */
	public void setPlayerControl(BetterCharacterControl playerControl)
	{
		this.playerControl = playerControl;
		if (playerControl != null)
		{
			playerNode.addControl(playerControl);
		}
		else
		{
			BetterCharacterControl control = playerNode.getControl(BetterCharacterControl.class);
			if (control != null) playerNode.removeControl(control);
		}
	}

	/**
	 * Registers some mappings to the input manager for moving the player.
	 */
	private void setupKeys() {
		InputManager inputManager = app.getInputManager();

		inputManager.addMapping(MOVE_LEFT, new KeyTrigger(KeyInput.KEY_A));
		inputManager.addMapping(MOVE_RIGHT, new KeyTrigger(KeyInput.KEY_D));
		inputManager.addMapping(MOVE_FORWARD, new KeyTrigger(KeyInput.KEY_W));
		inputManager.addMapping(MOVE_BACKWARD, new KeyTrigger(KeyInput.KEY_S));
		inputManager.addMapping(PLAYER_JUMP, new KeyTrigger(KeyInput.KEY_SPACE));
		inputManager.addMapping(PLAYER_SPRINT, new KeyTrigger(KeyInput.KEY_LSHIFT));

		inputManager.addListener(this, MOVE_LEFT);
		inputManager.addListener(this, MOVE_RIGHT);
		inputManager.addListener(this, MOVE_FORWARD);
		inputManager.addListener(this, MOVE_BACKWARD);
		inputManager.addListener(this, PLAYER_JUMP);
		inputManager.addListener(this, PLAYER_SPRINT);
	}

	/**
	 * Removes all mappings from the input manager.
	 */
	private void disableKeys()
	{
		InputManager inputManager = app.getInputManager();

		inputManager.deleteMapping(MOVE_LEFT);
		inputManager.deleteMapping(MOVE_RIGHT);
		inputManager.deleteMapping(MOVE_FORWARD);
		inputManager.deleteMapping(MOVE_BACKWARD);
		inputManager.deleteMapping(PLAYER_JUMP);
		inputManager.deleteMapping(PLAYER_SPRINT);
	}

	@Override
	public void onAction(String name, boolean isPressed, float tpf)
	{

		switch (name)
		{
			case MOVE_LEFT:
				left = isPressed;
				break;
			case MOVE_RIGHT:
				right = isPressed;
				break;
			case MOVE_FORWARD:
				forward = isPressed;
				break;
			case MOVE_BACKWARD:
				backward = isPressed;
				break;
			case PLAYER_JUMP:
				playerControl.jump();
				break;
			case PLAYER_SPRINT:
				sprinting = isPressed;
				break;
		}
	}

	/**
	 * Move camPos closer to subject to avoid obstruction by the scene.
	 *
	 * @param subject what camera is looking at
	 * @param scene the scene root
	 * @param pov preferred translation of camera relative to subject
	 * @param camPos translation of camera relative to subject
	 */
	private void avoidCamObstruction(Node subject, Node scene, Vector3f pov, Vector3f camPos)
	{
		Ray ray = new Ray();
		ray.getOrigin().set(subject.getWorldTranslation());
		ray.getDirection().set(pov);
		ray.getDirection().normalizeLocal();
		avoidCamObstruction(subject, scene, pov, camPos, pov.length(), ray);


		/*
		Camera cam = app.getCamera();

		ray.getOrigin().set(subject.getWorldTranslation());
		ray.getDirection().set(camPos);
		ray.getDirection().addLocal(cam.getUp().mult(cam.getFrustumTop()));
		ray.getDirection().addLocal(cam.getLeft().mult(cam.getFrustumLeft()));
		ray.getDirection().normalizeLocal();
		avoidCamObstruction(subject, scene, camPos, camPos, camPos.length(), ray);

		ray.getOrigin().set(subject.getWorldTranslation());
		ray.getDirection().set(camPos);
		ray.getDirection().addLocal(cam.getUp().mult(cam.getFrustumTop()));
		ray.getDirection().addLocal(cam.getLeft().mult(cam.getFrustumRight()));
		ray.getDirection().normalizeLocal();
		avoidCamObstruction(subject, scene, camPos, camPos, camPos.length(), ray);

		ray.getOrigin().set(subject.getWorldTranslation());
		ray.getDirection().set(camPos);
		ray.getDirection().addLocal(cam.getUp().mult(cam.getFrustumBottom()));
		ray.getDirection().addLocal(cam.getLeft().mult(cam.getFrustumLeft()));
		ray.getDirection().normalizeLocal();
		avoidCamObstruction(subject, scene, camPos, camPos, camPos.length(), ray);

		ray.getOrigin().set(subject.getWorldTranslation());
		ray.getDirection().set(camPos);
		ray.getDirection().addLocal(cam.getUp().mult(cam.getFrustumBottom()));
		ray.getDirection().addLocal(cam.getLeft().mult(cam.getFrustumRight()));
		ray.getDirection().normalizeLocal();
		avoidCamObstruction(subject, scene, camPos, camPos, camPos.length(), ray);*/
	}

	private void avoidCamObstruction(Node subject, Node scene, Vector3f pov, Vector3f camPos, float maxDist, Collidable shape)
	{
		CollisionResults collisionResults = new CollisionResults();
		scene.collideWith(shape, collisionResults);
		boolean blocked = false;
		for (int p = 0; p < collisionResults.size(); p++) {
			CollisionResult result = collisionResults.getCollision(p);
			Spatial parent = result.getGeometry().getParent();
			if (parent != subject) {
				if (result.getDistance() <= maxDist) {
					camPos.normalizeLocal().multLocal(result.getDistance() - 0.25f);
					blocked = true;
				}
				break;
			}
		}
		if (! blocked) {
			camPos.set(pov);
		}
	}

	@Override
	public void update(float tpf)
	{
		//app.getInputManager().setCursorVisible(false);

		Vector3f modelForwardDir = app.getCamera().getDirection();
		Vector3f modelLeftDir = app.getCamera().getLeft();

		Vector3f walkDirection = new Vector3f();
		if (left) {
			walkDirection.addLocal(modelLeftDir);
		} else if (right) {
			walkDirection.addLocal(modelLeftDir.negate());
		}
		if (forward) {
			walkDirection.addLocal(modelForwardDir);
		} else if (backward) {
			walkDirection.addLocal(modelForwardDir.negate());
		}

		if (walkDirection.lengthSquared() != 0)
		{
			walkDirection.setY(0);
			walkDirection.normalizeLocal();

			if (sprinting)
			{
				walkDirection.multLocal(5);
			}
			else
			{
				walkDirection.multLocal(3);
			}

			//playerControl.setViewDirection(app.getCamera().getDirection());
			playerControl.setViewDirection(walkDirection);
		}
		playerControl.setWalkDirection(walkDirection);
	}

	@Override
	public void cleanup()
	{
		super.cleanup();

		app.getRootNode().detachChild(playerNode);
		chaseCam.setEnabled(false);
		disableKeys();
	}
}
