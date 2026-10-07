package net.mx.edelicacy.machine;

import net.minecraftforge.common.config.Configuration;
import net.mx.edelicacy.machine.stove.StoveRepairRules;

/** 以太炉灶与饕餮之锅的配置（preInit 读取，之后只读） */
public final class MachineConfig {

    private static final String STOVE = "else.etheriumStove";
    private static final String PAN = "else.voraciousPan";

    /** 炉内熔炼每 tick 推进的进度（普通熔炉为 1，一次熔炼 200） */
    public static int furnaceProgressPerTick = 4;
    /** 炉顶烤架每 tick 推进的进度（农夫乐事炉灶为 1） */
    public static int grillProgressPerTick = 1;
    /** 点燃时额外驱动正上方烹饪锅一次（两倍速） */
    public static boolean boostCookingPot = true;
    /** 击杀生物获得的禁忌值 = 最大生命 / 该值（向下取整） */
    public static int forbiddenPointDivisor = 10;

    private static final String[] DEFAULT_REPAIRABLE = {
            "enigmaticlegacy:etherium_helm", "enigmaticlegacy:etherium_chest", "enigmaticlegacy:etherium_legs",
            "enigmaticlegacy:etherium_boots", "enigmaticlegacy:etherium_sword", "enigmaticlegacy:etherium_axe",
            "enigmaticlegacy:etherium_pickaxe", "enigmaticlegacy:etherium_spade", "enigmaticlegacy:astral_breaker",
            "spartanmj:*_etherium", "enigmaticdelicacy:etherium_machete"
    };

    private MachineConfig() {
    }

    static void load(Configuration config) {
        furnaceProgressPerTick = config.getInt("furnaceProgressPerTick", STOVE, 4, 1, 50,
                "Smelting progress per tick inside the Etherium Stove (a vanilla furnace makes 1 per tick, one smelt needs 200).");
        grillProgressPerTick = config.getInt("grillProgressPerTick", STOVE, 1, 1, 16,
                "Cooking progress per tick of the 6 grill slots on top (Farmer's Delight stove = 1).");
        boostCookingPot = config.getBoolean("boostCookingPot", STOVE, true,
                "While lit, tick a Farmer's Delight Cooking Pot directly above one extra time per tick (2x speed).");
        String[] repairable = config.getStringList("repairableItems", STOVE, DEFAULT_REPAIRABLE,
                "Damaged items the stove repairs (input slot -> output slot) instead of smelting. "
                        + "Format modid:name, '*' is a wildcard (e.g. spartanmj:*_etherium). "
                        + "Items implementing IStoveRepairable (Etherium Steak) are always repairable.");
        StoveRepairRules.setPatterns(repairable);
        forbiddenPointDivisor = config.getInt("forbiddenPointDivisor", PAN, 10, 1, 1000,
                "Killing a mob with the Eldritch Pan in the main hand adds floor(maxHealth / this) Forbidden Points to the pan.");
    }
}
