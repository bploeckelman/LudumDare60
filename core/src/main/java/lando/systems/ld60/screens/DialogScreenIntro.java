package lando.systems.ld60.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
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
import lando.systems.ld60.assets.GameUIAnim;
import lando.systems.ld60.particles.effects.BloodSplatterEffect;

public class DialogScreenIntro extends BaseScreen {

    private static final String TAG = DialogScreenIntro.class.getSimpleName();

    private static final float CHARACTER_SIZE = 160f;
    private static final float BOX_WIDTH = 600f;
    private static final float BOX_HEIGHT = 400f;
    private static final float BOX_PADDING = 70f;

    private enum Side { LEFT, RIGHT }

    private record Actor(GameSpine spine, String animation) {}

    private record Line(Side speaking, Actor left, Actor right, String text) {}

    private static Actor awol(String animation) { return new Actor(GameSpine.AWOL, animation); }
    private static Actor floppyd(String animation) { return new Actor(GameSpine.BRICKPHONE, animation); }

    private static final Line[] LINES = {
        new Line(Side.LEFT, null, null, "This is how the story starts."),
        new Line(Side.LEFT, awol("idle"), floppyd("idle"), "Hey there."),
        new Line(Side.RIGHT, awol("idle"), floppyd("punch"), "Oh, hello! BANG!"),
        new Line(Side.LEFT, awol("punch"), floppyd("jump"), "Rude, Take that!"),
        new Line(Side.RIGHT, awol("idle"), floppyd("death"), "(dying sound)"),
        new Line(Side.LEFT, awol("run"), null, "Good"),
        new Line(Side.LEFT, null, null, "And that is how the story starts"),
    };

    private final Speaker left;
    private final Speaker right;
    private final VisLabel text = new VisLabel();
    private final Stage dialogStage = new Stage(worldView.viewport, batch);
    private int line = 0;

    public DialogScreenIntro(ZenGame game) {
        super(game, false);
        addCommonSystems();
        var dosFont = new BitmapFont(Gdx.files.internal("fonts/dos.fnt"));

        var style = new Label.LabelStyle(text.getStyle());
        style.font = dosFont;
        text.setStyle(style);
        text.setFontScale(.5f);
        var width = Zen.config.framebufferWidth();
        var height = Zen.config.framebufferHeight();
        worldCamera.position.set(width / 2f, height / 2f, 0f);
        worldCamera.update();

        left = new Speaker("left", width * 0.2f, height * 0.2f, 1);
        right = new Speaker("right", width * 0.8f, height * 0.2f, -1);

        text.setWrap(true);
        showLine();

        var box = new VisTable();
        box.setBackground(new TextureRegionDrawable(GameUIAnim.DIALOG.get().getKeyFrame(0)));
        box.add(text).pad(BOX_PADDING).align(Align.center).grow();

        var table = new VisTable();
        table.setFillParent(true);
        table.add().size(CHARACTER_SIZE).expandX();
        table.add(box).size(BOX_WIDTH, BOX_HEIGHT);
        table.add().size(CHARACTER_SIZE).expandX();
        dialogStage.addActor(table);
    }

    @Override
    protected void renderWorld(Batch batch) {
        // uiStage always draws after, so we can't use uiStage.
        dialogStage.draw();
        super.renderWorld(batch);
    }

    @Override
    public void dispose() {
        dialogStage.dispose();
        super.dispose();
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        dialogStage.act(delta);
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
        if (current.left() == null && SpineAnimator.has(left.entity)) {
            left.entity.remove(SpineAnimator.TYPE);
        }
        if (current.right() == null && SpineAnimator.has(right.entity)) {
            right.entity.remove(SpineAnimator.TYPE);
        }
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
            if (actor.animation.equals("death")) {
                anim.play(actor.animation(), false);
                var pos = Position.get(entity);
                var params = new BloodSplatterEffect.Params(pos.x + 50f * -facing, pos.y, 25);
                factory.emitter(EmitterType.BLOOD, params, RenderLayer.OVERLAY);
            } else {
                anim.play(actor.animation());
            }
        }
    }
}
