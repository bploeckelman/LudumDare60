package lando.systems.ld60.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.zendogames.zenlib.assets.ZenAnimHero;
import com.zendogames.zenlib.ecs.Entity;
import com.zendogames.zenlib.ecs.World;
import com.zendogames.zenlib.ecs.components.*;
import com.zendogames.zenlib.ecs.movement.platformer.PlatformerMotion;
import com.zendogames.zenlib.ecs.movement.platformer.PlatformerTuning;
import com.zendogames.zenlib.input.InputControls;
import com.zendogames.zenlib.render.RenderLayer;
import lando.systems.ld60.assets.GameAtlas;
import lando.systems.ld60.game.components.Health;
import lando.systems.ld60.game.components.Trigger;

public class Factory {

    /** Tile size: the unit every tuning is authored in. */
    public static final int UNIT = 16;

    private final World world;

    public Factory(final World world) {
        this.world = world;
    }

    public Entity libgdx(int x, int y, int w, int h) {
        var region = (TextureRegion) GameAtlas.SPRITES.get().findRegion("libgdx");
        var image = new Image(region, w, h);
        image.layer = RenderLayer.BACKGROUND;
        return world.createEntity()
                .add(new Name("libgdx"))
                .add(new Position(x, y))
                .add(image);
    }

    public Entity examplePlayer(int x, int y) {
        var ledgeGrab = PlatformerTuning.Abilities.LedgeGrab.PUSH;
        var tuning = new PlatformerTuning(
                UNIT,                   // unit -> pixels, base unit that tuning params are authored in (often tile size)
                6, 0.1f, 0.08f,         // ground: speed (tiles/s), accel time, stop time
                6, 0.2f, 0.2f, 22,      // air: speed, accel time, stop time, max fall speed
                5, 0.38f,               // jump: apex height (tiles), time to peak
                2.5f, 2f,               // gravity multipliers: jump cut, fall
                0.1f, 0.12f,            // jump helpers: buffer time, coyote time
                new PlatformerTuning.Abilities(
                        6, 10, 0.2f,            // wall: slide speed, jump speed x, steer-lock time
                        ledgeGrab, 0.25f, 0.1f, // ledge: grab rule, reach, regrab time
                        20, 0.2f, 0.4f));       // dash: speed, time, cooldown
        var inputControls = InputControls.wasd(com.badlogic.gdx.Input.Keys.SPACE);
        return world.createEntity()
                .add(new Name("player"))
                .add(new Position(x, y))
                .add(new Health(3))
                .add(new Contacts())
                .add(new Animator(ZenAnimHero.IDLE, 8, 0))
                .add(new Behavior(ExamplePlayerState.NORMAL))
                .add(new Mover(new PlatformerMotion(tuning)))
                .add(new Input(inputControls))
                .add(Collider.rect(GameMask.PLAYER, -4, 0, 8, 16)
                        .blockedBy(GameMask.SOLID)
                        .detects(GameMask.ENEMY, GameMask.TRIGGER, GameMask.HAZARD));
    }

    public Entity bouncer(TextureRegion pixel, int x, int y, float height) {
        var image = new Image(pixel, UNIT, UNIT);
        image.tint.set(Color.ORANGE);
        return world.createEntity()
                .add(new Name("bouncer"))
                .add(new Position(x, y))
                .add(new Trigger(new Trigger.Bounce(height)))
                .add(Collider.rect(GameMask.TRIGGER, 0, 0, UNIT, UNIT))
                .add(image);
    }

    public Entity jumpthru(TextureRegion pixel, int x, int y, int w) {
        var image = new Image(pixel, w, 1);
        image.tint.set(Color.SKY);
        return world.createEntity()
                .add(new Name("platform"))
                .add(new Position(x, y))
                .add(Collider.platform(GameMask.SOLID, w))
                .add(image);
    }

    public Entity spike(TextureRegion pixel, int x, int y, String dir) {
        // TODO: use 'dir' to pick the sprite image
        var image = new Image(pixel, UNIT, UNIT);
        image.tint.set(Color.RED);
        return world.createEntity()
                .add(new Name("spike"))
                .add(new Position(x, y))
                .add(Collider.rect(GameMask.HAZARD, 0, 0, UNIT, UNIT))
                .add(image);
    }
}
