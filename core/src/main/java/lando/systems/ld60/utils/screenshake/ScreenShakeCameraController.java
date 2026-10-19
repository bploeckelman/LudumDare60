package lando.systems.ld60.utils.screenshake;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.zendogames.zenlib.Log;

public class ScreenShakeCameraController {

    public float maxXOffset = 40f;
    public float maxYOffset = 40f;
    public float maxAngleDegrees = 3f;
    public float maxZoomOffset = 0.1f; //  zoom punch


    public float xOffsetFreq = 25f; // higher = faster shake
    public float yOffsetFreq = 28f;
    public float rotationFreq = 15f;
    public float zoomFreq = 20f;

    public float traumaDecayRate = .9f; // seconds to decay from 1 to 0
    public float traumaPower = 3f; // 2 = squared, 3 = cubed. Higher = snappier

    private final OrthographicCamera worldCamera;
    private final OrthographicCamera viewCamera;
    private final SimplexNoise noise;
    private float trauma;
    private float time;

    // Use different seeds so x/y/rot aren't correlated
    private static final float SEED_X = 1.3f;
    private static final float SEED_Y = 7.9f;
    private static final float SEED_ROT = 15.2f;
    private static final float SEED_ZOOM = 23.7f;

    // Optional debug
    private Texture debugTexture;
    private NinePatch outlineNinePatch;
    private Texture pixelTex;



    public ScreenShakeCameraController(OrthographicCamera worldCamera){
        this.worldCamera = worldCamera;
        viewCamera = new OrthographicCamera(worldCamera.viewportWidth, worldCamera.viewportHeight);
        noise = new SimplexNoise(16, .5f, 2);
        trauma = 0;
        time = 0;
        viewCamera.update();

    }


    /**
     * Called every frame.
     * This will update the shake camera
     * @param dt frame delta
     */
    public void update(float dt) {
        time += dt;

        // Copy world camera state
        viewCamera.viewportWidth = worldCamera.viewportWidth;
        viewCamera.viewportHeight = worldCamera.viewportHeight;
        viewCamera.position.set(worldCamera.position);
        viewCamera.up.set(worldCamera.up);
        viewCamera.zoom = worldCamera.zoom;


        viewCamera.update();


        // Eased shake amount — pow curve feels way better than linear
        float shake = (float) Math.pow(trauma, traumaPower);



        // Different noise sample per axis so it doesn't move in circles
        float offsetX = maxXOffset * shake * worldCamera.zoom *
            (float) noise.getNoise(SEED_X, time * xOffsetFreq);
        float offsetY = maxYOffset * shake * worldCamera.zoom *
            (float) noise.getNoise(SEED_Y, time * yOffsetFreq);
        float angle = maxAngleDegrees * shake *
            (float) noise.getNoise(SEED_ROT, time * rotationFreq);
        float zoomOffset = maxZoomOffset * shake *
            (float) noise.getNoise(SEED_ZOOM, time * zoomFreq);

        // Add raw randomness on top of noise for true chaos at high trauma
        if (shake > 0.7f) {
            float wildness = (shake - 0.7f) / 0.3f; // 0-1 when trauma is 0.7-1.0
            offsetX += MathUtils.random(-8f, 8f) * wildness;
            offsetY += MathUtils.random(-8f, 8f) * wildness;
            angle += MathUtils.random(-1f, 1f) * wildness;
        }

        viewCamera.position.add(offsetX, offsetY, 0);
        viewCamera.rotate(angle);
        viewCamera.zoom += zoomOffset;
        viewCamera.update();

        // Exponential decay feels more natural than linear
        trauma = MathUtils.clamp(trauma - dt * traumaDecayRate, 0f, 1f);
    }



    /**
     * Add trauma. Values between 0.2-0.6 feel good. Stacks additively.
     * @param amount 0-1, clamped. 0.3 = light hit, 0.6 = explosion, 1.0 = screen nuke
     */
    public void addTrauma(float amount) {
        Log.info("ScreenShake", "Adding trauma:  %.2f}", amount);
        trauma = MathUtils.clamp(trauma + amount, 0f, 1f);
    }

    /**
     * Set trauma directly, overriding current. Use for screen-wide effects.
     */
    public void setTrauma(float amount) {
        trauma = MathUtils.clamp(amount, 0f, 1f);
    }

    /**
     * Instantly stop all shake
     */
    public void stop() {
        trauma = 0f;
    }

    public float getTrauma() {
        return trauma;
    }

    public boolean isShaking() {
        return trauma > 0.001f;
    }

    public Matrix4 getCombinedMatrix() {
        return viewCamera.combined;
    }

    public OrthographicCamera getViewCamera() {
        return viewCamera;
    }

    public void renderDebug(SpriteBatch batch, OrthographicCamera screenCamera) {
        if (debugTexture == null) return; // skip if not initialized

        batch.setColor(Color.WHITE);
        batch.draw(debugTexture, screenCamera.viewportWidth - 148, 20);
        float height = screenCamera.viewportHeight - 40;
        batch.setColor(Color.RED);
        batch.draw(pixelTex, 20, 20, 20, height * trauma);
        batch.draw(pixelTex, 45, 20, 20, height * getShakeAmount());

        batch.setColor(Color.WHITE);
        outlineNinePatch.draw(batch, 20, 20, 20, height);
        outlineNinePatch.draw(batch, 45, 20, 20, height);
    }

    private float getShakeAmount() {
        return (float) Math.pow(trauma, traumaPower);
    }
}
