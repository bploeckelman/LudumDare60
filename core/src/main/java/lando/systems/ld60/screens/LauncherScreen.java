package lando.systems.ld60.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.utils.Align;
import com.zendogames.zenlib.ZenGame;
import lando.systems.ld60.Main;
import lando.systems.ld60.assets.SoundType;
import lando.systems.ld60.events.AudioEvent;

public class LauncherScreen extends BaseScreen {

    public static float DEBOUNCE = .5f;
    public float charWidth = 16f;
    public float charHeight = 32f;
    Color headerTextColor = new Color(0f, 0f, .8f, 1f);
    BitmapFont dosFont;
    GlyphLayout glyphLayout = new GlyphLayout();
    float accum;

    Color cgaBlue = new Color(0.0f, 0.0f, 0.67f, 1.0f);        // Classic DOS Background
    Color cgaCyan = new Color(0.0f, 0.67f, 0.67f, 1.0f);       // Teal/Cyan
    Color cgaMagenta = new Color(0.67f, 0.0f, 0.67f, 1.0f);    // Purple/Magenta
    Color cgaLightGray = new Color(0.67f, 0.67f, 0.67f, 1.0f); // Standard DOS Text
    Color cgaYellow = new Color(1.0f, 1.0f, 0.33f, 1.0f);

    public LauncherScreen(ZenGame game) {
        super(game, false);
        accum = 0f;
        // This needed to be a bitmap font to get that old dos terminal look,
        // Textra font was fucking with kerning and such.
        dosFont = new BitmapFont(Gdx.files.internal("fonts/dos.fnt"));
        dosFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        dosFont.setUseIntegerPositions(true);
        dosFont.getData().setScale(.5f);
    }

    @Override
    public void update(float dt) {
        super.update(dt);
        accum += dt;
        var isEnterPressed = Gdx.input.isKeyJustPressed(Input.Keys.ENTER);
        var isJustTouched  = Gdx.input.justTouched();
        var isPadPressed   = pad.anyButton();
        if (accum >= DEBOUNCE && (isEnterPressed || isJustTouched || isPadPressed)) {
            if (!transitioning){
                transitioning = true;
                AudioEvent.playSound(SoundType.DEGAUSS);
                game.setScreen(new IntroScreen(game));
            }
        }
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        Gdx.gl.glClearColor(0f, 0, .9f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        batch.setProjectionMatrix(windowCamera.combined);
        batch.setColor(cgaLightGray);
        batch.draw(Main.game.pixel, 0, windowCamera.viewportHeight - charHeight, windowCamera.viewportWidth, charHeight);

        batch.setColor(cgaMagenta);
        batch.draw(Main.game.pixel,  charWidth * 5,  windowCamera.viewportHeight - charHeight * 9, charWidth * 40, charHeight * 5);

        // Black Drop Shadow for Cyan Box
        batch.setColor(0f, 0f, 0f, 1f);;
        batch.draw(Main.game.pixel,  charWidth * 11,  windowCamera.viewportHeight - charHeight * 18, charWidth * 30, charHeight * 6);

        // Cyan Box
        batch.setColor(cgaCyan);
        batch.draw(Main.game.pixel,  charWidth * 10,  windowCamera.viewportHeight - charHeight * 17, charWidth * 30, charHeight * 6);

        // Button/Prompt Shadows
        batch.setColor(0f, 0f, 0f, 1f);
        batch.draw(Main.game.pixel,  charWidth * 14.5f,  windowCamera.viewportHeight - charHeight * 15.5f, charWidth * 20, charHeight * 1);

        // Button/Prompt Body
        batch.setColor(cgaLightGray);
        batch.draw(Main.game.pixel,  charWidth * 14,  windowCamera.viewportHeight - charHeight * 15, charWidth * 20, charHeight * 1);


        batch.setColor(1f, 1f, 1f, 1f);
        drawTextAt("File", 2, 0, cgaBlue);
        drawTextAt("Edit", 8, 0, cgaBlue);
        drawTextAt("View", 14, 0, cgaBlue);
        drawTextAt("Play", 20, 0, cgaBlue);
        drawTextAt("Jam!", 26, 0, cgaBlue);

        drawTextAt("╔═══════════════════════════════════════╗", 6, 4, Color.WHITE);
        drawTextAt("║ The Coalition of Obsolete Technology  ║", 6, 5, Color.WHITE);
        drawTextAt("╠═══════════════════════════════════════╣", 6, 6, Color.WHITE);
        drawTextAt("║     Book II: Return of the Audio      ║", 6, 7, Color.WHITE);
        drawTextAt("╚═══════════════════════════════════════╝", 6, 8, Color.WHITE);


        // The Terminal Prompt (Solid Yellow text with a blinking terminal cursor)
        drawTextAt("► Press Enter", 16, 14, Color.BLACK);

        // Classic DOS underscore cursor blink (toggles every 0.5s)
        if (accum % 1.0f < 0.5f) {
            drawTextAt("█", 29, 14, Color.BLACK); // Drawn right after "Enter"
        }
        batch.end();

        renderUI();
    }


    public void drawTextAt(String text, int charX, int charY, Color color) {
        glyphLayout.setText(dosFont, text, color, 0, Align.left, false);
        dosFont.draw(batch, glyphLayout, charX * charWidth, windowCamera.viewportHeight - (charHeight * charY) + 5);
//        dosFont.markup(text, layout);
//        dosFont.drawGlyphs(batch, layout, charX * charWidth, windowCamera.viewportHeight - (charHeight * charY) - dosFont.descent + 6);
    }
}
