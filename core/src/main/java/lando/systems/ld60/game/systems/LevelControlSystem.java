package lando.systems.ld60.game.systems;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.PolylineMapObject;
import com.badlogic.gdx.math.Polyline;
import com.badlogic.gdx.utils.Array;
import com.zendogames.zenlib.Log;
import com.zendogames.zenlib.ecs.*;
import com.zendogames.zenlib.ecs.components.Position;
import com.zendogames.zenlib.ecs.components.Tilemap;
import lando.systems.ld60.screens.GameScreen;

public class LevelControlSystem extends EntitySystem {

    private static final String TAG = LevelControlSystem.class.getSimpleName();

    public static final Tag ENEMY_TAG = Tag.register("Enemy");

    private static final int PRIORITY = SystemOrder.SENSE - 1;

    private final GameScreen gameScreen;
    private final EnemySpawner enemySpawner;

    private Family enemyFamily;
    private int currentStage = 1;
    private Array<MapObject> unspawnedEnemies = new Array<>();
    private Tilemap tilemap;

    private float currentStageStartX;
    private float currentStageEndX;
    private float activeLeftBound;
    private boolean stageClear = false;

    public interface EnemySpawner {
        Entity spawn(MapObject obj);
    }

    public LevelControlSystem(GameScreen gameScreen, EnemySpawner enemySpawner) {
        super(PRIORITY);
        this.gameScreen = gameScreen;
        this.enemySpawner = enemySpawner;
    }

    @Override
    public void addedToWorld(World world) {
        this.enemyFamily = world.family(Family.all(ENEMY_TAG));
    }

    public void startLevel(Tilemap tilemap) {
        this.tilemap = tilemap;
        this.currentStage = 1;
        this.stageClear = false;
        this.activeLeftBound = 0f;
        this.unspawnedEnemies.clear();
        loadStageBounds(currentStage);
    }

    public Entity queueEnemy(MapObject obj) {
        unspawnedEnemies.add(obj);
        return null;
    }

    public void finishSpawning() {
        spawnEnemiesForStage(currentStage);
    }

    private void loadStageBounds(int stageNum) {
        currentStageStartX = 0f;
        currentStageEndX = Float.MAX_VALUE;
        float rawStartX = 0f;

        if (tilemap != null) {
            for (MapObject obj : tilemap.objects) {
                if (obj instanceof PolylineMapObject) {
                    Object stageProp = obj.getProperties().get("stage_num");
                    int sNum = stageProp instanceof Number ? ((Number)stageProp).intValue() : -1;

                    if (sNum == stageNum) {
                        Object valX = obj.getProperties().get("x");
                        rawStartX = valX instanceof Number ? ((Number)valX).floatValue() : 0f;

                        Polyline polyline = ((PolylineMapObject) obj).getPolyline();
                        float[] vertices = polyline.getVertices();
                        if (vertices.length >= 2) {
                            // vertices[length - 2] is the X offset of the final point in the polyline
                            currentStageEndX = rawStartX + vertices[vertices.length - 2];
                        }
                        break;
                    }
                }
            }
        }

        // Shift the boundary left by 2/3 of the camera width to prevent camera jumping
        float overlap = 0f;
        if (stageNum > 1 && gameScreen.cameraFollow != null) {
            overlap = gameScreen.cameraFollow.camera.viewportWidth * (2f / 3f);
        }
        currentStageStartX = rawStartX - overlap;

        // Never let the active left bound retreat backwards
        activeLeftBound = Math.max(activeLeftBound, currentStageStartX);

        // Adjust if the player is somehow already further left than the new bound
        if (gameScreen.player != null) {
            Position pos = Position.get(gameScreen.player);
            if (pos != null && pos.x < activeLeftBound) {
                activeLeftBound = pos.x;
            }
        }

        updateCameraBounds();
    }

    private void spawnEnemiesForStage(int stageNum) {
        Array<MapObject> spawnedThisStage = new Array<>();

        for (MapObject obj : unspawnedEnemies) {
            Object valX = obj.getProperties().get("x");
            float ex = valX instanceof Number ? ((Number)valX).floatValue() : 0f;

            if (ex >= currentStageStartX && ex < currentStageEndX) {
                Entity e = enemySpawner.spawn(obj);
                if (e != null) {
                    e.tag(ENEMY_TAG);
                }
                spawnedThisStage.add(obj);
            }
        }

        Log.info(TAG, "Spawned %s enemies for Stage %d", spawnedThisStage.size, stageNum);
        unspawnedEnemies.removeAll(spawnedThisStage, true);

        if (enemyFamily.entities().isEmpty()) {
            stageClear = true;
            updateCameraBounds();
        }
    }

    private void updateCameraBounds() {
        if (gameScreen.cameraFollow != null && tilemap != null) {
            float mapEndX = tilemap.width();
            float rightBound = stageClear ? mapEndX : currentStageEndX;
            float width = Math.max(0, rightBound - activeLeftBound);

            gameScreen.cameraFollow.bounds.set(activeLeftBound, 0, width, tilemap.height());
        }
    }

    @Override
    public void update(float dt) {
        if (!stageClear && unspawnedEnemies.isEmpty() && enemyFamily.entities().isEmpty()) {
            stageClear = true;
            updateCameraBounds();
            Log.info(TAG, "All enemies defeated! Level Complete.");
        } else if (!stageClear && enemyFamily.entities().isEmpty()) {
            stageClear = true;
            updateCameraBounds();
            Log.info(TAG, "Stage %d clear! Move forward.", currentStage);
        }

        if (gameScreen.player != null) {
            Position pos = Position.get(gameScreen.player);
            if (pos != null) {

                // Dynamically lock the camera from retreating based on player position up to the startX limit
                if (pos.x >= currentStageStartX && activeLeftBound < currentStageStartX) {
                    activeLeftBound = currentStageStartX;
                    updateCameraBounds();
                }

                // If stage is clear and player leaves the CURRENT stage's boundary, start next stage
                if (stageClear && pos.x >= currentStageEndX) {
                    currentStage++;
                    stageClear = false;
                    Log.info(TAG, "Entering stage %d", currentStage);
                    loadStageBounds(currentStage);
                    spawnEnemiesForStage(currentStage);
                }

                // Smoothly clamp player to bounds without integer truncation
                if (pos.x < activeLeftBound) {
                    pos.x = (int)activeLeftBound;
                }

                float mapEndX = tilemap != null ? tilemap.width() : Float.MAX_VALUE;
                float activeRightBound = stageClear ? mapEndX : currentStageEndX;

                if (pos.x > activeRightBound) {
                    pos.x = (int)activeRightBound;
                }
            }
        }
    }
}
