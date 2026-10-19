package lando.systems.ld60.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.github.tommyettinger.textra.TypingLabel;
import lando.systems.ld60.Config;
import lando.systems.ld60.assets.GameFont;
import lando.systems.ld60.assets.GameUIAnim;
import lando.systems.ld60.assets.SoundType;
import lando.systems.ld60.events.AudioEvent;

public class KeepMovingWidget  extends Widget {
    private static float SIZE = 150;
    private static float SHAKEAMOUNT = 30;
    private static float MARGIN = 30;

    public int lastPlayedSoundIndex = 0;

    public enum GoPrompt {
//        GO_RIGHT("GO RIGHT!", SoundType.BELL_HIGH), // Replace with actual voice lines or sfx
//        MOVE_IT("MOVE IT!", SoundType.BELL_LOW),
//        AREA_CLEAR("AREA CLEAR!", SoundType.BELL_HIGH),
//        KEEP_GOING("KEEP GOING!", SoundType.BELL_LOW),
        ONWARD("Onward!", SoundType.ONWARD),
        LETSGO("Let's Go!", SoundType.LETSGO),
        DAMN_SON("Well Done!", SoundType.DAMN_SON),
        FIGHT_MORE("Fight (more)!", SoundType.FIGHT_MORE),
        ;

        public final String text;
        public final SoundType sound;


        GoPrompt(String text, SoundType sound) {
            this.text = text;
            this.sound = sound;
        }

        public static GoPrompt random() {
            return values()[MathUtils.random(values().length - 1)];
        }
    }

    public TextureRegion arrowTexture;
    public float accum = 0;
    private TypingLabel typingLabel;
    private GoPrompt currentPrompt;
    private float waitTimer = 0f;

    public KeepMovingWidget() {
        this.arrowTexture = GameUIAnim.GO_ARROW.get().getKeyFrame(0);
        this.typingLabel = new TypingLabel("", GameFont.DOS_FONT.get());
    }

    @Override
    public void setVisible(boolean visible) {
        boolean wasVisible = isVisible();
        super.setVisible(visible);

        // Only roll a new prompt and play the sound when turning ON
        if (visible && !wasVisible) {
            currentPrompt = GoPrompt.random();
            waitTimer = 0f;

            // Start the typing animation with the new text
            typingLabel.restart("{SPEED=1.5}" + currentPrompt.text);

            if (currentPrompt.sound != null) {
                AudioEvent.playSound(currentPrompt.sound, .25f);
            }
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        accum += delta;


        if (isVisible() && currentPrompt != null) {
            typingLabel.act(delta);

            // When the typing is finished, wait 1 second then restart
            if (typingLabel.hasEnded()) {
                waitTimer += delta;
                if (waitTimer >= 1.0f) {
                    waitTimer = 0f;
                    typingLabel.restart();
                }
            }
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        float x = Config.windowWidth - (SIZE + SHAKEAMOUNT + MARGIN);
        float y = Config.windowHeight / 2 - SIZE / 2f;


        // Calculate the shaken X coordinate once so the text and arrow move perfectly together
        float drawX = x + MathUtils.cos(accum * 6f) * SHAKEAMOUNT;

        batch.setColor(1f, 1f, 1f, parentAlpha);
        batch.draw(arrowTexture, drawX, y, SIZE, SIZE);

        // Sync the label's position to the shaking arrow and draw it
        if (currentPrompt != null) {
            // Position it slightly above the arrow
            typingLabel.setPosition(x, y + SIZE + 10f);
            typingLabel.draw(batch, parentAlpha);
        }
    }
}
