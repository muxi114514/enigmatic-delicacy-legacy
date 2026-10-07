package net.mx.edelicacy.curse.potion;

import net.minecraft.potion.Potion;

/**
 * 本线注册的药水实例。生命诅咒与禁忌之印另有 DelicacyPotions 的 ObjectHolder；
 * 深渊腐蚀是本线新增（EL 1.12 没有），只在这里有引用。注册事件之前为 null。
 */
public final class CursePotions {

    public static Potion HEALTH_CURSE;
    public static Potion FORBIDDEN_IMPRINT;
    public static Potion ABYSS_CORRUPTION;

    private CursePotions() {
    }
}
