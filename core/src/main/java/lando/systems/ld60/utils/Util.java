package lando.systems.ld60.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;

import java.util.List;

public class Util {

    // ------------------------------------------------------------------------
    // Color related
    // ------------------------------------------------------------------------

    private static final List<Color> colors = List.of(
        /* grayscale */ Color.WHITE, Color.LIGHT_GRAY, Color.GRAY, Color.DARK_GRAY, Color.BLACK,
        /* reds      */ Color.FIREBRICK, Color.RED, Color.SCARLET, Color.CORAL, Color.SALMON,
        /* greens    */ Color.GREEN, Color.CHARTREUSE, Color.LIME, Color.FOREST, Color.OLIVE,
        /* blues     */ Color.BLUE, Color.NAVY, Color.ROYAL, Color.SLATE, Color.SKY, Color.CYAN, Color.TEAL,
        /* yellows   */ Color.YELLOW, Color.GOLD, Color.GOLDENROD, Color.ORANGE, Color.BROWN, Color.TAN,
        /* purples   */ Color.PINK, Color.MAGENTA, Color.PURPLE, Color.VIOLET, Color.MAROON);

    public static Color randomColor() {
        var index = MathUtils.random(colors.size() - 1);
        return colors.get(index);
    }
}
