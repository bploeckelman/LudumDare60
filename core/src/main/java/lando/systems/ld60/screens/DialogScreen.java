package lando.systems.ld60.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.zendogames.zenlib.Log;
import com.zendogames.zenlib.Zen;
import com.zendogames.zenlib.ZenGame;
import com.zendogames.zenlib.ecs.Entity;
import com.zendogames.zenlib.ecs.components.Name;
import com.zendogames.zenlib.ecs.components.Position;
import com.zendogames.zenlib.ecs.components.SpineAnimator;
import com.zendogames.zenlib.render.RenderLayer;
import com.zendogames.zenlib.screens.transition.ScreenTransitionEffectType;
import lando.systems.ld60.assets.EmitterType;
import lando.systems.ld60.assets.GameSpine;
import lando.systems.ld60.particles.effects.BloodSplatterEffect;

public class DialogScreen extends BaseScreen {

    private static final String TAG = DialogScreen.class.getSimpleName();

    private static final float CHARACTER_SIZE = 160f;
    private static final float BOX_WIDTH = 600f;
    private static final float BOX_HEIGHT = 400f;
    private static final float BOX_PADDING = 50f;

    private enum Side { LEFT, RIGHT }

    private record Actor(GameSpine spine, String animation) {}

    private record Line(Side speaking, Actor left, Actor right, String text) {}

    private static Actor awol(String animation) { return new Actor(GameSpine.AWOL, animation); }
    private static Actor spineboy(String animation) { return new Actor(GameSpine.SPINEBOY, animation); }

    private static final Line[] LINES = {
        new Line(Side.LEFT, awol("idle"), spineboy("idle"), "Hey there."),
        new Line(Side.RIGHT, awol("idle"), spineboy("shoot"), "Oh, hello! BANG!"),
        new Line(Side.LEFT, awol("punch"), spineboy("jump"), "Rude, Take that!"),
        new Line(Side.RIGHT, awol("idle"), spineboy("death"), "(dying sound)"),
        new Line(Side.LEFT, awol("run"), null, "Good"),
    };

    private final Speaker left;
    private final Speaker right;
    private final VisLabel text = new VisLabel();
    private int line = 0;

    public DialogScreen(ZenGame game) {
        super(game, false);
        uiStage.setDebugAll(true);
        addCommonSystems();

        var width = Zen.config.framebufferWidth();
        var height = Zen.config.framebufferHeight();
        worldCamera.position.set(width / 2f, height / 2f, 0f);
        worldCamera.update();

        left = new Speaker("left", width * 0.2f, height * 0.2f, 1);
        right = new Speaker("right", width * 0.8f, height * 0.2f, -1);

        text.setWrap(true);
        showLine();

        var box = new VisTable();
        box.pad(BOX_PADDING);
        box.add(text).align(Align.center).grow();

        var table = new VisTable();
        table.setFillParent(true);
        table.add().size(CHARACTER_SIZE).expandX();
        table.add(box).size(BOX_WIDTH, BOX_HEIGHT);
        table.add().size(CHARACTER_SIZE).expandX();
        uiStage.addActor(table);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            nextLine();
        }
    }

    private void nextLine() {
        Log.info(TAG, "line: " + line);
        if (line >= LINES.length - 1) {
            //TODO: set it to next screen
            game.setScreen(new IntroScreen(game), ScreenTransitionEffectType.DOORWAY);
            return;
        }
        line++;
        showLine();
    }

    private void showLine() {
        var current = LINES[line];
        text.setText(current.text());
        text.setAlignment(current.speaking() == Side.LEFT ? Align.topLeft : Align.topRight);
        left.startAnim(current.left());
        right.startAnim(current.right());
    }

    public class Speaker {
        Entity entity;
        int facing;
        GameSpine spine;

        public Speaker(String name, float x, float y, int facing) {
            this.entity = world.createEntity()
                .add(new Name(name))
                .add(new Position(x, y));
            this.facing = facing;
        }

        private void startAnim(Actor actor) {
            if (actor == null) {
                return;
            }
            if (actor.spine() != spine) {
                spine = actor.spine();
                var anim = new SpineAnimator(spine);
                anim.facing = facing;
                entity.add(anim);
            }
            var anim = SpineAnimator.get(entity);
            anim.play(actor.animation(), !actor.animation.equals("death"));
            if (actor.animation.equals("death")) {
                var pos = Position.get(entity);
                var params = new BloodSplatterEffect.Params(pos.x + 50f * -facing, pos.y, 25);
                factory.emitter(EmitterType.BLOOD, params, RenderLayer.OVERLAY);
            }
        }
    }
}
