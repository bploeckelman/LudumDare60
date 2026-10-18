package lando.systems.ld60.game;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ScreenUtils;
import lando.systems.ld60.game.components.Television;

import java.util.function.Consumer;

/**
 * The picture every {@link Television} shows: the world as the player sees it, drawn into a small
 * framebuffer once per frame by {@link #capture}. The screen calls it from {@code renderOffscreenBuffers}
 * before the world view's own pass, so a television shows the frame it is part of.
 * <p>
 * Two buffers alternate: the capture draws into the spare one while televisions inside it sample
 * {@link #region()}, the previous capture. That keeps a television out of its own render target (a GL
 * feedback loop) and gives a television showing a television the classic one-frame-late regress.
 * <p>
 * The buffer is smaller than the view: the camera's projection does the downscale for free, and the
 * {@code Nearest} filter keeps the result chunky like the old set it is drawn on.
 */
public class TelevisionFeed implements Disposable {

    /** What the picture shows where nothing is drawn; set to the world view's clear color to match. */
    public final Color clearColor = Color.BLACK.cpy();

    private final FrameBuffer[] buffers = new FrameBuffer[2];
    private final TextureRegion[] regions = new TextureRegion[2];
    private int current = 0;

    public TelevisionFeed(int width, int height) {
        this(width, height, Texture.TextureFilter.Nearest);
    }

    public TelevisionFeed(int width, int height, Texture.TextureFilter filter) {
        for (int i = 0; i < buffers.length; i++) {
            buffers[i] = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
            var texture = buffers[i].getColorBufferTexture();
            texture.setFilter(filter, filter);
            regions[i] = new TextureRegion(texture);
            regions[i].flip(false, true);
            // A fresh texture holds whatever the driver left there; start on a blank picture
            buffers[i].begin();
            ScreenUtils.clear(clearColor);
            buffers[i].end();
        }
    }

    /** The latest capture, y-flipped for the batch; what a {@link Television} draws. */
    public TextureRegion region() {
        return regions[current];
    }

    /**
     * Draws the world through {@code camera} into the spare buffer and makes it the current picture.
     * {@code drawWorld} gets the batch with its projection set and does its own {@code begin}/{@code end}.
     * Call only while no other framebuffer is bound: {@code FrameBuffer.end} rebinds the default one.
     */
    public void capture(Batch batch, Camera camera, Consumer<Batch> drawWorld) {
        int next = 1 - current;
        buffers[next].begin();
        {
            ScreenUtils.clear(clearColor);
            batch.setProjectionMatrix(camera.combined);
            drawWorld.accept(batch);
        }
        buffers[next].end();
        current = next;
    }

    @Override
    public void dispose() {
        for (var buffer : buffers) {
            buffer.dispose();
        }
    }
}
