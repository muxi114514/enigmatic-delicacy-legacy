package net.mx.edelicacy.curse.potion;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.mx.edelicacy.curse.damage.CurseDamageSources;

/**
 * 深渊腐蚀（移植 EL+ 的 AbyssCorruption，EL 1.12 没有）：每级护甲韧性 -100%、移速 -10%（与 1.21 一样随等级叠乘），
 * 每 32>>(等级/2) tick 受到 2×2^(等级/2) 点深渊伤害且无视无敌帧；无法被移除、死亡时向周围蔓延（见 {@link AbyssCorruptionHandler}）。
 */
public class PotionAbyssCorruption extends CursePotionBase {

    public PotionAbyssCorruption() {
        super("abyss_corruption", true, 0x382C4D, false);
        registerPotionAttributeModifier(SharedMonsterAttributes.ARMOR_TOUGHNESS, "8d0c3a7e-2b6f-4f0e-a3e1-3c7c9a0b5e21", -1.0D, 1);
        registerPotionAttributeModifier(SharedMonsterAttributes.MOVEMENT_SPEED, "1f6b8e44-7a52-4d0b-9b8e-6c2f7d3a9e10", -0.1D, 2);
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        int interval = 32 >> (amplifier / 2);
        return interval == 0 || duration % interval == 0;
    }

    @Override
    public void performEffect(EntityLivingBase entity, int amplifier) {
        if (entity.world.isRemote) {
            return;
        }
        entity.attackEntityFrom(CurseDamageSources.ABYSS, 2.0F * (float) Math.pow(2, amplifier / 2));
        entity.hurtResistantTime = 0;
    }
}
