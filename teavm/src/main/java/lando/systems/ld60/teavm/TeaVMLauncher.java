package lando.systems.ld60.teavm;

import com.github.xpenatan.gdx.teavm.backends.web.WebApplicationConfiguration;
import com.github.xpenatan.gdx.teavm.backends.web.WebApplication;
import lando.systems.ld60.Config;
import lando.systems.ld60.Main;

/**
 * Launches the TeaVM/HTML application.
 */
public class TeaVMLauncher {
    public static void main(String[] args) {
        WebApplicationConfiguration config = new WebApplicationConfiguration("canvas");
        //// If width and height are each greater than 0, then the app will use a fixed size.
        config.width = Config.windowWidth;
        config.height = Config.windowHeight;
        //// If width and height are both 0, then the app will use all available space.
//        config.width = 0;
//        config.height = 0;
        config.useGL30 = true;
        config.preloadListener = assetLoader -> assetLoader.loadScript("freetype.js");

        new WebApplication(new Main(), config);
    }
}
