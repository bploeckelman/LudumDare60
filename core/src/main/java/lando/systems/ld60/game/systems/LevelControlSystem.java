package lando.systems.ld60.game.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.PolylineMapObject;
import com.badlogic.gdx.math.Polyline;
import com.badlogic.gdx.utils.Array;
import com.zendogames.zenlib.Log;
import com.zendogames.zenlib.ecs.*;
import com.zendogames.zenlib.ecs.components.*;
import com.zendogames.zenlib.render.RenderLayer;
import com.zendogames.zenlib.tilemap.TiledProps;
import lando.systems.ld60.assets.EmitterType;
import lando.systems.ld60.events.ParticleEvent;
import lando.systems.ld60.game.GameTags;
import lando.systems.ld60.game.components.GameInput;
import lando.systems.ld60.particles.effects.SpawnCloudEffect;
import lando.systems.ld60.screens.GameScreen;

public class LevelControlSystem extends EntitySystem {

    private static final String TAG = LevelControlSystem.class.getSimpleName();
    private static final int PRIORITY = SystemOrder.SENSE - 1;

    public enum SequenceState {
        PLAYING,
        BOSS_DIALOGUE, // Place to run your custom script/dialogue
        PLAYER_EXIT,   // Auto-walk player to bottom right
        DONE           // Finished, waiting for transition
    }

    private final GameScreen gameScreen;
    private final EnemySpawner enemySpawner;
    private final Runnable onLevelComplete;

    private Family enemyFamily;
    private Family bossFamily;
    private int currentStage = 1;
    private final Array<MapObject> unspawnedEnemies = new Array<>();
    private final Array<PolylineMapObject> stageBounds = new Array<>();
    private Tilemap tilemap;

    private float currentStageStartX;
    private float currentStageEndX;
    private float activeLeftBound;
    private float currentCameraRightBound;

    private boolean stageClear = false;
    private boolean bossSpawned = false;
    private SequenceState sequenceState = SequenceState.PLAYING;

    public interface EnemySpawner {
        Entity spawn(MapObject obj);
    }

    public LevelControlSystem(GameScreen gameScreen, EnemySpawner enemySpawner, Runnable onLevelComplete) {
        super(PRIORITY);
        this.gameScreen = gameScreen;
        this.enemySpawner = enemySpawner;
        this.onLevelComplete = onLevelComplete;
    }

    @Override
    public void addedToWorld(World world) {
        this.enemyFamily = world.family(Family.all(GameTags.ENEMY));
        this.bossFamily = world.family(Family.all(GameTags.BOSS));
    }

    public void startLevel(Tilemap tilemap) {
        this.tilemap = tilemap;
        this.currentStage = 1;
        this.stageClear = false;
        this.bossSpawned = false;
        this.sequenceState = SequenceState.PLAYING;
        this.activeLeftBound = 0f;
        this.currentCameraRightBound = 0f;
        this.unspawnedEnemies.clear();

        this.stageBounds.clear();
        if (tilemap != null) {
            for (MapObject obj : tilemap.objects) {
                if ("stage".equals(TiledProps.key(obj)) && obj instanceof PolylineMapObject) {
                    stageBounds.add((PolylineMapObject) obj);
                }
            }
            stageBounds.sort((o1, o2) -> {
                Object valX1 = o1.getProperties().get("x");
                Object valX2 = o2.getProperties().get("x");
                float x1 = valX1 instanceof Number ? ((Number) valX1).floatValue() : 0f;
                float x2 = valX2 instanceof Number ? ((Number) valX2).floatValue() : 0f;
                return Float.compare(x1, x2);
            });
        }

        loadStageBounds(currentStage);
    }

    public void queueEnemy(MapObject obj) {
        unspawnedEnemies.add(obj);
    }

    public void finishSpawning() {
        spawnEnemiesForStage(currentStage);
    }

    private void loadStageBounds(int stageNum) {
        currentStageStartX = 0f;
        currentStageEndX = Float.MAX_VALUE;
        float rawStartX = 0f;

        int stageIndex = stageNum - 1;
        if (stageIndex >= 0 && stageIndex < stageBounds.size) {
            PolylineMapObject stageObj = stageBounds.get(stageIndex);
            Object valX = stageObj.getProperties().get("x");
            rawStartX = valX instanceof Number ? ((Number)valX).floatValue() : 0f;

            Polyline polyline = stageObj.getPolyline();
            float[] vertices = polyline.getVertices();
            if (vertices.length >= 2) {
                currentStageEndX = rawStartX + vertices[vertices.length - 2];
            }
        }

        float overlap = 0f;
        if (stageNum > 1 && gameScreen.cameraFollow != null) {
            overlap = gameScreen.cameraFollow.camera.viewportWidth * (2f / 3f);
        }
        currentStageStartX = rawStartX - overlap;

        activeLeftBound = Math.max(activeLeftBound, currentStageStartX);

        if (gameScreen.player != null) {
            Position pos = Position.get(gameScreen.player);
            if (pos != null && pos.x < activeLeftBound) {
                activeLeftBound = pos.x;
            }
        }

        currentCameraRightBound = currentStageEndX;
    }

    private void spawnEnemiesForStage(int stageNum) {
        Array<MapObject> spawnedThisStage = new Array<>();

        for (MapObject obj : unspawnedEnemies) {
            Object valX = obj.getProperties().get("x");
            float ex = valX instanceof Number ? ((Number)valX).floatValue() : 0f;

            if (ex >= currentStageStartX && ex < currentStageEndX) {
                Entity e = enemySpawner.spawn(obj);
                if (e != null && e.hasTag(GameTags.BOSS)) {
                    bossSpawned = true;
                }
                if (e != null) {
                    var pos = Position.get(e);
                    SpawnCloudEffect.Params params = new SpawnCloudEffect.Params(pos);
                    ParticleEvent.spawn(EmitterType.SPAWN_CLOUD, params, RenderLayer.OVERLAY);
                }
                spawnedThisStage.add(obj);
            }
        }

        Log.info(TAG, "Spawned %s enemies for Stage %d", spawnedThisStage.size, stageNum);
        unspawnedEnemies.removeAll(spawnedThisStage, true);

        if (spawnedThisStage.isEmpty()) {
            stageClear = true;
        }
    }

    private void updateCameraBounds(float dt) {
        if (gameScreen.cameraFollow != null && tilemap != null) {
            float mapEndX = tilemap.width();
            float targetRightBound = stageClear ? mapEndX : currentStageEndX;

            if (currentCameraRightBound == 0f || currentCameraRightBound > targetRightBound) {
                currentCameraRightBound = targetRightBound;
            } else if (currentCameraRightBound < targetRightBound) {
                currentCameraRightBound += 400f * dt;
                if (currentCameraRightBound > targetRightBound) {
                    currentCameraRightBound = targetRightBound;
                }
            }

            float width = Math.max(0, currentCameraRightBound - activeLeftBound);
            gameScreen.cameraFollow.bounds.set(activeLeftBound, 0, width, tilemap.height());
        }
    }

    static void hold(Entity e) {
        var input = Input.get(e);
        if (input != null) {
            input.moveX = 0;
            input.moveY = 0;
            input.actionPressed = false;
        }
        var buttons = GameInput.get(e);
        if (buttons != null) {
            buttons.clearFrameEvents();
        }
    }

    // Add this getter inside LevelControlSystem
    public Entity getBossEntity() {
        if (bossFamily == null || bossFamily.entities().isEmpty()) {
            return null;
        }
        // Return the first boss found
        return bossFamily.entities().get(0);
    }

    @Override
    public void update(float dt) {
        updateCameraBounds(dt);

        if (sequenceState == SequenceState.DONE) return;

        // 1. BOSS DIALOGUE STATE
        if (sequenceState == SequenceState.BOSS_DIALOGUE) {
            if (gameScreen.player != null) {
                hold(gameScreen.player);
            }

            // TODO: Render your boss text bubble here
            // Listen for dismissal (mouse click, enter key, button press, etc.)
            if (Gdx.input.justTouched()) {
                Log.info(TAG, "Dialogue dismissed. Auto-walking player out.");
                sequenceState = SequenceState.PLAYER_EXIT;
            }
            return;
        }

        // 2. PLAYER EXIT STATE (Auto-walk)
        if (sequenceState == SequenceState.PLAYER_EXIT) {
            if (gameScreen.player != null) {
                var input = Input.get(gameScreen.player);
                var pos = Position.get(gameScreen.player);

                // Target bottom right coordinate
                float targetX = tilemap.width() - 32f;
                float targetY = 16f; // Floor level

                int dx = (int) (targetX - pos.x);
                int dy = (int) (targetY - pos.y);

                if (Math.abs(dx) <= 4 && Math.abs(dy) <= 4) {
                    hold(gameScreen.player);
                    sequenceState = SequenceState.DONE;
                    if (onLevelComplete != null) onLevelComplete.run();
                } else {
                    if (input != null) {
                        input.moveX = Integer.signum(dx);
                        input.moveY = Integer.signum(dy);
                        input.actionPressed = false;
                    }
                    var buttons = GameInput.get(gameScreen.player);
                    if (buttons != null) buttons.clearFrameEvents();
                }
            }
            return;
        }

        // 3. NORMAL PLAYING STATE
        if (sequenceState == SequenceState.PLAYING) {

            // Check win condition
            if (bossSpawned && bossFamily.entities().isEmpty() && enemyFamily.entities().isEmpty()) {
                stageClear = true;
                sequenceState = SequenceState.BOSS_DIALOGUE;
                Log.info(TAG, "Boss defeated! Starting dialogue sequence.");
                return;
            }

            // Normal stage clearing
            if (!stageClear && enemyFamily.entities().isEmpty() && bossFamily.entities().isEmpty()) {
                stageClear = true;
                if (unspawnedEnemies.isEmpty() && !bossSpawned) {
                    // Fallback if the map has no boss
                    sequenceState = SequenceState.BOSS_DIALOGUE;
                } else {
                    Log.info(TAG, "Stage %d clear! Move forward.", currentStage);
                }
            }

            // Clamp player within stage bounds
            if (gameScreen.player != null) {
                Position pos = Position.get(gameScreen.player);
                if (pos != null) {

                    float margin = 24f; // Fallback default
                    if (gameScreen.player.has(SpineAnimator.TYPE)) {
                        SpineAnimator anim = SpineAnimator.get(gameScreen.player);
                        // origin.x is usually the center of the entity.
                        // The half-width of the drawn size is size.x / 2.
                        margin = (anim.size.x / 2f) * Math.abs(anim.scale.x);
                        margin += 5f; // a little extra
                    }
                    if (pos.x >= currentStageStartX && activeLeftBound < currentStageStartX) {
                        // Make sure we don't snap the left bound further than the player's permitted margin
                        activeLeftBound = Math.min(pos.x - margin, currentStageStartX);
                        updateCameraBounds(dt);
                    }

                    if (stageClear && pos.x >= currentStageEndX) {
                        currentStage++;
                        stageClear = false;
                        Log.info(TAG, "Entering stage %d", currentStage);
                        loadStageBounds(currentStage);
                        spawnEnemiesForStage(currentStage);
                    }

                    // Apply the margin padding so the player is fully visible
                    if (pos.x < activeLeftBound + margin) {
                        pos.x = (int) (activeLeftBound + margin);
                    }

                    // Bound the player's right side to the ACTIVE animating camera right bound
                    if (pos.x > currentCameraRightBound - margin) {
                        pos.x = (int) (currentCameraRightBound - margin);
                    }
                }
            }
        }
    }
}
