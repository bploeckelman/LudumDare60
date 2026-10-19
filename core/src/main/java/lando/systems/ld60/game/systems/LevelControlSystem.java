package lando.systems.ld60.game.systems;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.PolylineMapObject;
import com.badlogic.gdx.math.Polyline;
import com.badlogic.gdx.utils.Array;
import com.zendogames.zenlib.Log;
import com.zendogames.zenlib.ecs.*;
import com.zendogames.zenlib.ecs.components.Position;
import com.zendogames.zenlib.ecs.components.Tilemap;
import com.zendogames.zenlib.tilemap.TiledProps;
import lando.systems.ld60.game.GameTags;
import lando.systems.ld60.screens.GameScreen;

public class LevelControlSystem extends EntitySystem {

    private static final String TAG = LevelControlSystem.class.getSimpleName();

    private static final int PRIORITY = SystemOrder.SENSE - 1;

    private final GameScreen gameScreen;
    private final EnemySpawner enemySpawner;

    private Family enemyFamily;
    private Family bossFamily;
    private int currentStage = 1;
    private final Array<MapObject> unspawnedEnemies = new Array<>();
    private final Array<PolylineMapObject> stageBounds = new Array<>();
    private Tilemap tilemap;

    private float currentStageStartX;
    private float currentStageEndX;
    private float activeLeftBound;
    private boolean stageClear = false;
    private boolean bossSpawned = false;
    private boolean levelComplete = false;

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
        this.enemyFamily = world.family(Family.all(GameTags.ENEMY));
        this.bossFamily = world.family(Family.all(GameTags.BOSS));
    }

    public void startLevel(Tilemap tilemap) {
        this.tilemap = tilemap;
        this.currentStage = 1;
        this.stageClear = false;
        this.bossSpawned = false;
        this.levelComplete = false;
        this.activeLeftBound = 0f;
        this.unspawnedEnemies.clear();

        // Find all "stage" polylines and sort them from left to right based on their X coordinate
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
                    if (e.hasTag(GameTags.BOSS)) {
                        bossSpawned = true;
                    }
                }
                spawnedThisStage.add(obj);
            }
        }

        Log.info(TAG, "Spawned %s enemies for Stage %d", spawnedThisStage.size, stageNum);
        unspawnedEnemies.removeAll(spawnedThisStage, true);

        if (spawnedThisStage.isEmpty()) {
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
        if (levelComplete) return;

        if (bossSpawned && bossFamily.entities().isEmpty()) {
            levelComplete = true;
            stageClear = true;
            updateCameraBounds();
            Log.info(TAG, "Boss defeated! Level Complete.");
            return;
        }

        if (!stageClear && enemyFamily.entities().isEmpty()) {
            stageClear = true;
            updateCameraBounds();

            if (unspawnedEnemies.isEmpty() && !bossSpawned) {
                levelComplete = true;
                Log.info(TAG, "All enemies defeated! Level Complete.");
            } else {
                Log.info(TAG, "Stage %d clear! Move forward.", currentStage);
            }
        }

        if (gameScreen.player != null) {
            Position pos = Position.get(gameScreen.player);
            if (pos != null) {

                if (pos.x >= currentStageStartX && activeLeftBound < currentStageStartX) {
                    activeLeftBound = Math.min(pos.x, currentStageStartX);
                    updateCameraBounds();
                }

                if (stageClear && pos.x >= currentStageEndX && !levelComplete) {
                    currentStage++;
                    stageClear = false;
                    Log.info(TAG, "Entering stage %d", currentStage);
                    loadStageBounds(currentStage);
                    spawnEnemiesForStage(currentStage);
                }

                if (pos.x < activeLeftBound) {
                    pos.x = (int) activeLeftBound;
                }

                float mapEndX = tilemap != null ? tilemap.width() : Float.MAX_VALUE;
                float activeRightBound = stageClear ? mapEndX : currentStageEndX;

                if (pos.x > activeRightBound) {
                    pos.x = (int) activeRightBound;
                }
            }
        }
    }
}
