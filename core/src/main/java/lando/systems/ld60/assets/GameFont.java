package lando.systems.ld60.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.github.tommyettinger.textra.Font;
import com.zendogames.zenlib.assets.Asset;
import com.zendogames.zenlib.assets.Assets;
import com.zendogames.zenlib.assets.FontType;

public enum GameFont implements FontType {
    DOS_FONT("fonts/dos.ttf", 24)
    ;

    private final String path;
    private final Asset<Font> asset ;
    private final FreeTypeFontGenerator.FreeTypeFontParameter params;


    GameFont(String path, int size) {
        this.path = path;
        this.asset = new Asset<>(this);
        this.params = new FreeTypeFontGenerator.FreeTypeFontParameter();
        this.params.borderColor = Color.BLACK;
        this.params.borderWidth = 2;
        this.params.color = Color.WHITE;
        this.params.size = size;
        this.params.borderStraight = true;
        this.params.characters = FreeTypeFontGenerator.DEFAULT_CHARS + "╔═╗║╚╝";
        this.params.kerning = false;
        this.params.spaceX = -3;
        this.params.spaceY = -3;
        this.params.mono = true;
    }



    @Override
    public FreeTypeFontGenerator.FreeTypeFontParameter params() {;
        return params;
    }

    @Override public FileHandle file() { return Gdx.files.internal(path); }
    @Override public Asset<Font> asset() {return asset;}

}
