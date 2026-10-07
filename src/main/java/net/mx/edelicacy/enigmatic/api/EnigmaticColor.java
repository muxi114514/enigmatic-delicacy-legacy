package net.mx.edelicacy.enigmatic.api;

import java.util.Locale;

/**
 * 谜之食物的七种颜色，对应 EL 1.12 谜之护符 meta 1~7。
 * <p>{@link #dataKey()} 与 1.21 的 DelicacyData 键（"EnigmaticFood" + 颜色名）一致。
 */
public enum EnigmaticColor {
    RED, AQUA, VIOLET, MAGENTA, GREEN, BLACK, BLUE;

    /** 玩家数据里的永久标记键 */
    public String dataKey() {
        return "EnigmaticFood" + name();
    }

    /** 小写名，用于属性来源键 */
    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }
}
