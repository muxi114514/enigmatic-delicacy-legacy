package net.mx.edelicacy.curse;

import net.minecraftforge.common.config.Configuration;

/**
 * 诅咒 / 禁忌 / 深渊线的配置（分类沿用 1.21 的小节名，拼写错误的 fobiddenItems 已改正）。
 * <p>1.21 有几项默认值落在自己的范围之外（诅咒刃片持续时间、深渊乱炖三项），这里都改成范围内的合理值。
 */
public final class CurseConfig {

    // 诅咒刃片
    public static int curseBladeAmplifier = 4;
    public static int curseBladeDuration = 600;
    public static int healthCurseAmplifier = 3;
    // 咒魂晶刃
    public static int crystalKnifeProbability = 75;
    public static int crystalKnifeCooldown = 100;
    // 咒魂晶坠
    public static int pendantHealingModifier = 25;
    public static int pendantRecoveryRate = 24;
    // 禁忌华盏
    public static int forbiddenGrailDuration = 120;
    // 无尽贪食徽记
    public static double gluttonyAttackDamage = 0.4;
    public static double gluttonyCritModifier = 1.25;
    public static double gluttonyMovementSpeed = 0.25;
    public static double gluttonyDamageResistance = 0.5;
    // 深渊乱炖
    public static int abyssalStewDuration = 180;
    public static int abyssalStewDamageBoost = 30;
    public static int abyssalStewDamageResistance = 25;
    public static int abyssBoostRate = 5;
    // 神圣果派
    public static int divinePieCharmSlot = 3;
    public static int divinePieFallbackHealth = 4;

    private CurseConfig() {
    }

    public static void load(Configuration config) {
        String blade = "else.curseBlade";
        curseBladeAmplifier = config.getInt("amplifier", blade, 4, 0, 4,
                "Level of the Health Curse applied by a charged strike; the self damage / target max health damage is (level + 1) * 5%.");
        curseBladeDuration = config.getInt("duration", blade, 600, 20, 3600,
                "Duration (ticks) of the Health Curse applied by a charged strike.");
        healthCurseAmplifier = config.getInt("healthCurseAmplifier", blade, 3, 0, 10,
                "Level of the permanent Health Curse gained after cutting off the Ring of the Seven Curses.");

        String knife = "else.curseCrystalKnife";
        crystalKnifeProbability = config.getInt("probability", knife, 75, 0, 100,
                "Chance (%) for the Cursed Soul Crystal Knife to apply or stack the Health Curse on hit.");
        crystalKnifeCooldown = config.getInt("cooldown", knife, 100, 20, 200,
                "Cooldown (ticks) after turning a creature into a baby with the knife.");

        String pendant = "else.curseCrystalPendant";
        pendantHealingModifier = config.getInt("healingModifier", pendant, 25, -100, 300,
                "Healing received while wearing the pendant is multiplied by (1 + value / 100). "
                        + "The 1.21 code multiplied healing by 25% although its tooltip says +25%; set -75 to get that behavior.");
        pendantRecoveryRate = config.getInt("recoveryRate", pendant, 24, 0, 100,
                "Percent of each damage taken that is healed back right after the hit.");

        forbiddenGrailDuration = config.getInt("duration", "forbiddenItems.forbiddenGrail", 120, 10, 1200,
                "Duration (seconds) of the Forbidden Grail state.");

        String gluttony = "cursedItems.gluttonyCharm";
        gluttonyAttackDamage = config.get(gluttony, "attackDamage", 0.4, "Attack damage bonus at 100% missing food (1.0 = +100%).", 0.0, 10.0).getDouble();
        gluttonyCritModifier = config.get(gluttony, "critModifier", 1.25, "Critical damage multiplier bonus at 100% missing food.", 0.0, 10.0).getDouble();
        gluttonyMovementSpeed = config.get(gluttony, "movementSpeed", 0.25, "Movement speed bonus at 100% missing food.", 0.0, 10.0).getDouble();
        gluttonyDamageResistance = config.get(gluttony, "damageResistance", 0.5, "Damage resistance at 100% missing food.", 0.0, 1.0).getDouble();

        String stew = "abyssItems.abyssalStew";
        abyssalStewDuration = config.getInt("duration", stew, 180, 10, 1200, "Duration (seconds) of the abyssal state.");
        abyssalStewDamageBoost = config.getInt("specialDamageBoost", stew, 30, 0, 100, "Damage dealt bonus (%) during the abyssal state.");
        abyssalStewDamageResistance = config.getInt("specialDamageResistance", stew, 25, 0, 100, "Damage taken reduction (%) during the abyssal state.");
        abyssBoostRate = config.getInt("abyssBoostRate", stew, 5, 0, 100,
                "Extra damage bonus / reduction (%) per eldritch item worn (armor, hands, baubles).");

        String pie = "else.divineFruitPie";
        divinePieCharmSlot = config.getInt("charmSlotIndex", pie, 3, 0, 99,
                "BaubleVault extra CHARM slot index unlocked once per player by eating the Divine Fruit Pie "
                        + "(BaubleVault needs a rule for it, e.g. 'CHARM:3|level|2147483647').");
        divinePieFallbackHealth = config.getInt("fallbackMaxHealth", pie, 4, 0, 40,
                "Permanent max health granted once instead of slots when neither BaubleVault nor BaublesEX is installed.");
    }
}
