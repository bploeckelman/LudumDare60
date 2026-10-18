package lando.systems.ld60.assets;

import com.badlogic.gdx.assets.loaders.ShaderProgramLoader;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.esotericsoftware.spine.SkeletonData;
import com.zendogames.zenlib.assets.Asset;
import com.zendogames.zenlib.assets.AssetType;
import com.zendogames.zenlib.assets.Assets;

import java.util.Objects;

public enum GameShader implements AssetType<ShaderProgram> {
    CRT( "shaders/crt.frag")
    ;


    public final ShaderProgramLoader.ShaderProgramParameter params;



    private final Asset<ShaderProgram> asset = new Asset<>(this);

    GameShader(String fragPath) {
        this("shaders/default.vert", fragPath);
    }

    GameShader(String vertPath, String fragPath) {
        params = new ShaderProgramLoader.ShaderProgramParameter();
        params.vertexFile = vertPath;
        params.fragmentFile = fragPath;
        params.logOnCompileFailure = true;
//        ShaderProgram.pedantic = false;

    }

    GameShader(ShaderProgramLoader.ShaderProgramParameter params) {
        Objects.requireNonNull(params.vertexFile, name() + " params requires vertexFile value");
        Objects.requireNonNull(params.fragmentFile, name() + " params requires fragmentFile value");
        this.params = params;
    }

    // NOTE:
    //  Loading/getting from asset manager is a little goofy because 'load|get()'
    //  both expect a single file name string and the default state of the loader
    //  is to specify a file path without extension, which assumes both vert and frag
    //  files share the same prefix so it can manually append '.vert|.frag' extensions
    //  at load time.
    //  Here we always populate both file name fields in the params object,
    //  so the string provided to 'load|get()' is likely just ignored internally.
    //  To provide something reasonable we'll just take the fragment path and strip the extension
    //  since most shader programs use the default vertex shader.

    private String fragmentPathNoExt() {
        var index = params.fragmentFile.indexOf(".frag");
        return params.fragmentFile.substring(0, index);
    }

    @Override
    public void load(Assets assets) {
        assets.mgr.load(fragmentPathNoExt(), ShaderProgram.class, params);
    }

    @Override
    public Asset<ShaderProgram> asset() {
        return asset;
    }

    @Override
    public ShaderProgram init(Assets assets) {
        var shader = assets.mgr.get(fragmentPathNoExt(), ShaderProgram.class);

        return shader;
    }
}
