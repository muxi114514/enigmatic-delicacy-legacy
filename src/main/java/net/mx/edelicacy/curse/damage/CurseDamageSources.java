package net.mx.edelicacy.curse.damage;

import net.minecraft.entity.Entity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;

/**
 * 本线的伤害类型（死亡信息键 death.attack.enigmaticdelicacy.xxx / .player）。
 * <ul>
 *   <li>forbidden_curse：1.21 标签为绕过护甲、绕过效果（抗性）、无击退、环境伤害 → 绕甲 + 绝对伤害，无来源实体即无击退</li>
 *   <li>evil_curse：诅咒刃片的自伤（EL+ 的 EVIL_CURSE）</li>
 *   <li>abyss：深渊腐蚀（EL+ 的 ABYSS）</li>
 * </ul>
 */
public final class CurseDamageSources {

    public static final DamageSource FORBIDDEN_CURSE = new DamageSource("enigmaticdelicacy.forbidden_curse")
            .setDamageBypassesArmor().setDamageIsAbsolute();
    public static final DamageSource EVIL_CURSE = new DamageSource("enigmaticdelicacy.evil_curse")
            .setDamageBypassesArmor().setDamageIsAbsolute();
    public static final DamageSource ABYSS = new DamageSource("enigmaticdelicacy.abyss")
            .setDamageBypassesArmor().setMagicDamage();

    private CurseDamageSources() {
    }

    /** 深渊腐蚀随死亡蔓延时记在死者名下 */
    public static DamageSource abyssFrom(Entity source) {
        return new EntityDamageSource("enigmaticdelicacy.abyss", source).setDamageBypassesArmor().setMagicDamage();
    }
}
