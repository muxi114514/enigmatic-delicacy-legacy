package net.mx.edelicacy.tool;

import net.minecraftforge.common.config.Configuration;

/** 工具模块配置（preInit 读取，之后只读） */
public final class ToolConfig {

    /** 以太砍刀投掷冷却（tick） */
    public static int macheteCooldown = 50;
    /** 熔岩钓竿收杆时钓起岩浆怪的概率 */
    public static double magmaCubeChance = 0.02D;

    private ToolConfig() {
    }

    static void load(Configuration config) {
        macheteCooldown = config.getInt("cooldown", "else.etheriumMachete", 50, 20, 200,
                "Cooldown in ticks after throwing the spectral Etherium Machete (right click).");
        magmaCubeChance = config.get("else.infernalFishingRod", "magmaCubeChance", 0.02D,
                "Chance that reeling in a bite pulls up a small Magma Cube instead of loot.", 0.0D, 1.0D).getDouble(0.02D);
    }
}
