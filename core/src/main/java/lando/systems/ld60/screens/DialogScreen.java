package lando.systems.ld60.screens;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.kotcrab.vis.ui.widget.VisImage;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.zendogames.zenlib.Log;
import com.zendogames.zenlib.ZenGame;
import com.zendogames.zenlib.assets.ZenAnimHero;
import com.zendogames.zenlib.screens.transition.ScreenTransitionEffectType;
import lando.systems.ld60.assets.GameUIAnim;

public class DialogScreen extends BaseScreen {

    private static final String TAG = DialogScreen.class.getSimpleName();

    private static final float CHARACTER_SIZE = 160f;
    private static final float BOX_WIDTH      = 600f;
    private static final float BOX_HEIGHT     = 400f;
    private static final float BOX_PADDING    = 50f;

    private record Line(ZenAnimHero left, ZenAnimHero right, String text) {}

    private static final Line[] LINES = {
            new Line(ZenAnimHero.IDLE,   ZenAnimHero.IDLE,   "Left: Hey there."),
            new Line(ZenAnimHero.IDLE,   ZenAnimHero.JUMP,   "Right: Oh, hello!"),
            new Line(ZenAnimHero.ATTACK, ZenAnimHero.HURT,   "Left: Take that!"),
            new Line(ZenAnimHero.RUN,    ZenAnimHero.DEATH,  "Right: (dying sound)"),
    };

    private final TextureRegionDrawable leftFrame = new TextureRegionDrawable(LINES[0].left().get().getKeyFrame(0f));
    private final TextureRegionDrawable rightFrame = new TextureRegionDrawable(LINES[0].right().get().getKeyFrame(0f));
    private VisImage leftImage  = new VisImage(leftFrame);
    private VisImage rightImage = new VisImage(rightFrame);
    private final VisLabel text = new VisLabel(LINES[0].text());
    private int line = 0;
    private float accum = 0f;

    public DialogScreen(ZenGame game) {
        super(game, false);
        addCommonSystems();
        actions.add("back to menu", Input.Keys.ESCAPE, () -> game.setScreen(new IntroScreen(game), ScreenTransitionEffectType.DOORWAY));

        text.setWrap(true);
        text.setAlignment(Align.topLeft);

        var box = new VisTable();
        box.setBackground(new TextureRegionDrawable(GameUIAnim.SETTINGS.get().getKeyFrame(0)));
        box.pad(BOX_PADDING);
        box.add(text).grow();
        box.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                Log.info("Dialog","line: " + line);
                if (line >= LINES.length - 1) {
                    game.setScreen(new IntroScreen(game), ScreenTransitionEffectType.DOORWAY);
                    return;
                }
                line = (line + 1);
                accum = 0f;
                text.setText(LINES[line].text());
            }
        });
        leftImage .setScaling(Scaling.fit);
        rightImage.setScaling(Scaling.fit);
        rightImage.setScaleX(-1f);
        rightImage.setOrigin(CHARACTER_SIZE / 2f, CHARACTER_SIZE / 2f); // cell size is fixed, so the mirror pivots on the center from the first frame
        var table = new VisTable();
        table.setFillParent(true);
        table.add(leftImage).size(CHARACTER_SIZE).expandX();
        table.add(box).size(BOX_WIDTH, BOX_HEIGHT);
        table.add(rightImage).size(CHARACTER_SIZE).expandX();
        uiStage.addActor(table);
    }

    @Override
    public void update(float delta) {
        if (transitioning) return;
        super.update(delta);
        accum += delta;
        leftFrame.setRegion(LINES[line].left().get().getKeyFrame(accum));
        rightFrame.setRegion(LINES[line].right().get().getKeyFrame(accum));
    }
}
