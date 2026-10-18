package lando.systems.ld60.game.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.zendogames.zenlib.ecs.ComponentType;
import com.zendogames.zenlib.ecs.Entity;
import com.zendogames.zenlib.ecs.components.Renderable;
import com.zendogames.zenlib.utils.Draw;
import lando.systems.ld60.Main;
import lando.systems.ld60.assets.GameShader;
import lando.systems.ld60.game.TelevisionFeed;

import java.awt.*;

/**
 * A screen in the world showing the {@link TelevisionFeed}: the player watching themselves play.
 * A {@link Renderable} like {@code Image}, so the {@code RenderSystem} draws it in depth order with
 * everything else; the feed is captured by the screen, this only draws the latest picture.
 */
public class Television extends Renderable {

    public static final ComponentType<Television> TYPE = ComponentType.register(Television.class);
    public static Television get(Entity e) { return TYPE.get(e); }
    public static boolean    has(Entity e) { return TYPE.has(e); }

    private final TelevisionFeed feed;


    public Television(TelevisionFeed feed, int width, int height) {
        this.feed = feed;
        this.size.set(width, height);
    }

    @Override
    public void render(Batch batch, float x, float y) {
        var shader = GameShader.BROKEN_CRT.get();
        var prevShader = batch.getShader();

        var rect = rect(x, y);

        batch.setShader(shader);
        shader.setUniformf("u_time", feed.time);
        shader.setUniformf("u_resolution", rect.width, rect.height);

        batch.setColor(Color.WHITE);
        batch.draw(feed.region(), rect.x, rect.y, rect.width, rect.height);

        batch.setShader(prevShader);
    }
}
