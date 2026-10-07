package net.mx.edelicacy.fading;

import net.minecraftforge.common.config.Configuration;

/** 失色效果与失色卷轴的配置（分类沿用 1.21 的 else.xxx） */
public final class FadingConfig {

    /** 失色被消耗后卷轴重新提供的冷却（tick） */
    public static int scrollCooldown = 60;
    /** 失色状态下下一次近战的增伤（%） */
    public static int specialDamageBoost = 80;

    private FadingConfig() {
    }

    public static void load(Configuration config) {
        scrollCooldown = config.getInt("cooldown", "else.fadingScroll", 60, 10, 120,
                "Ticks before the Scroll of Fading grants Fading again after it was consumed.");
        specialDamageBoost = config.getInt("specialDamageBoost", "else.fading", 80, 0, 200,
                "Extra damage (percent) of the melee hit that consumes Fading.");
    }
}
