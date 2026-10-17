package lando.systems.ld60;

import com.zendogames.zenlib.ZenConfig;

public class Config {
    public static final String windowTitle     = "Ludum Dare 60";
    public static final String preferencesName = "lando-systems-ld60-prefs";

    public static final int windowWidth  = 800;
    public static final int windowHeight =  600;

    public static final boolean useVsync = true;

    public static ZenConfig zenConfig() {
        return new ZenConfig(preferencesName, windowWidth, windowHeight);
    }
}
