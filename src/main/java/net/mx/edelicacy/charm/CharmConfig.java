package net.mx.edelicacy.charm;

import net.minecraftforge.common.config.Configuration;

/** 护符、闪耀之书相关配置（分类沿用 1.21 的 else.xxx） */
public final class CharmConfig {

    /** 守护护符减伤上限（%） */
    public static int glisteningCharmThreshold = 75;
    /** 武器大师之荣攻击加成（%） */
    public static int weaponCharmDamageModifier = 8;
    /** 闪耀之书护盾上限 */
    public static int glisteningBookThreshold = 100;
    /** 闪耀之书命中后给攻击者的受击无敌时间（tick） */
    public static int glisteningBookHitInvulnerability = 10;

    private CharmConfig() {
    }

    public static void load(Configuration config) {
        glisteningCharmThreshold = config.getInt("threshold", "else.glisteningCharm", 75, 0, 100,
                "Maximum damage reduction (percent) the Charm of Protection can accumulate.");
        weaponCharmDamageModifier = config.getInt("damageModifier", "else.weaponCharm", 8, 0, 20,
                "Attack damage bonus (percent of total) granted by the Glory of Weapon Master.");
        // 1.21 下限为 0，会让耐久条除以 0
        glisteningBookThreshold = config.getInt("threshold", "else.theGlistening", 100, 1, 200,
                "Maximum shield value of The Glistening.");
        glisteningBookHitInvulnerability = config.getInt("hitInvulnerabilityTicks", "else.theGlistening", 10, 0, 20,
                "Hurt-invulnerability ticks granted to the attacker after landing a melee hit with The Glistening. 0 disables.");
    }
}
