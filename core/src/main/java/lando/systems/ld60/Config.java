package lando.systems.ld60;

import com.zendogames.zenlib.ZenConfig;

public class Config {
    public static final String windowTitle     = "Ludum Dare 60";
    public static final String preferencesName = "lando-systems-ld60-prefs";

    public static final int windowWidth  = 1280;
    public static final int windowHeight =  720;

    public static final boolean useVsync = true;

    public static ZenConfig zenConfig() {
        return new ZenConfig(preferencesName, windowWidth, windowHeight);
    }
}
