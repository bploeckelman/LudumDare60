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
import com.zendogames.zenlib.screens.ZenScreen;
import lando.systems.ld60.Main;

public class LauncherScreen extends BaseScreen {

    private float deBounceTimer = 0f;
    public float charWidth = 16f;
    public float charHeight = 32f;
    Color headerTextColor = new Color(0f, 0f, .8f, 1f);
    BitmapFont dosFont;
    GlyphLayout glyphLayout = new GlyphLayout();


    public LauncherScreen(ZenGame game) {
        super(game, false);
        deBounceTimer = .5f;
        // This needed to be a bitmap font to get that old dos terminal look,
        // Textra font was fucking with kerning and such.
        dosFont = new BitmapFont(Gdx.files.internal("fonts/dos.fnt"));
        dosFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        dosFont.setUseIntegerPositions(true);
        dosFont.getData().setScale(.5f);
    }


    @Override public void update(float dt) {
        super.update(dt);
        deBounceTimer -= dt;
        if (deBounceTimer <= 0 && (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.justTouched())) {
            if (!transitioning){
                transitioning = true;
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
        batch.setColor(1f, 1f, 1f, 1f);
        batch.draw(Main.game.pixel, 0, windowCamera.viewportHeight - charHeight, windowCamera.viewportWidth, charHeight);

        batch.setColor(.8f, .0f, .8f, 1f);
        batch.draw(Main.game.pixel,  charWidth * 5,  windowCamera.viewportHeight - charHeight * 9, charWidth * 40, charHeight * 5);

        batch.setColor(0f, .0f, 0f, 1f);
        batch.draw(Main.game.pixel,  charWidth * 11,  windowCamera.viewportHeight - charHeight * 18, charWidth * 30, charHeight * 6);

        batch.setColor(.0f, .8f, .8f, 1f);
        batch.draw(Main.game.pixel,  charWidth * 10,  windowCamera.viewportHeight - charHeight * 17, charWidth * 30, charHeight * 6);


        batch.setColor(0, .0f, .0f, 1f);
        batch.draw(Main.game.pixel,  charWidth * 14.5f,  windowCamera.viewportHeight - charHeight * 15.5f, charWidth * 20, charHeight * 1);

        batch.setColor(1.0f, 1.0f, 1.0f, 1f);
        batch.draw(Main.game.pixel,  charWidth * 14,  windowCamera.viewportHeight - charHeight * 15, charWidth * 20, charHeight * 1);


        batch.setColor(1f, 1f, 1f, 1f);
        drawTextAt("File", 2, 0, headerTextColor);
        drawTextAt("Edit", 8, 0, headerTextColor);
        drawTextAt("View", 14, 0, headerTextColor);
        drawTextAt("Play", 20, 0, headerTextColor);
        drawTextAt("Jam!", 26, 0, headerTextColor);

        drawTextAt("╔═══════════════════════════════════════╗", 6, 4, Color.WHITE);
        drawTextAt("║ The Coalition of Obsolete Technology  ║", 6, 5, Color.WHITE);
        drawTextAt("╠═══════════════════════════════════════╣", 6, 6, Color.WHITE);
        drawTextAt("║     Book II: Return of the Audio      ║", 6, 7, Color.WHITE);
        drawTextAt("╚═══════════════════════════════════════╝", 6, 8, Color.WHITE);

        drawTextAt("► Press Enter ◄", 17, 14, Color.BLACK);

        batch.end();
    }


    public void drawTextAt(String text, int charX, int charY, Color color) {
        glyphLayout.setText(dosFont, text, color, 0, Align.left, false);
        dosFont.draw(batch, glyphLayout, charX * charWidth, windowCamera.viewportHeight - (charHeight * charY) + 5);
//        dosFont.markup(text, layout);
//        dosFont.drawGlyphs(batch, layout, charX * charWidth, windowCamera.viewportHeight - (charHeight * charY) - dosFont.descent + 6);
    }
}
