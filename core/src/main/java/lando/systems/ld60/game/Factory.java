package lando.systems.ld60.game;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.zendogames.zenlib.ecs.Entity;
import com.zendogames.zenlib.ecs.World;
import com.zendogames.zenlib.ecs.components.Image;
import com.zendogames.zenlib.ecs.components.Name;
import com.zendogames.zenlib.ecs.components.Position;
import com.zendogames.zenlib.render.RenderLayer;
import lando.systems.ld60.assets.SpriteAtlas;

public class Factory {

    /** Tile size: the unit every tuning is authored in. */
    public static final int UNIT = 16;

    private final World world;

    public Factory(final World world) {
        this.world = world;
    }

    public Entity libgdx(int x, int y, int w, int h) {
        var region = (TextureRegion) SpriteAtlas.SPRITES.get().findRegion("libgdx");
        var image = new Image(region, w, h);
        image.layer = RenderLayer.BACKGROUND;
        return world.createEntity()
                .add(new Name("libgdx"))
                .add(new Position(x, y))
                .add(image);
    }
}
