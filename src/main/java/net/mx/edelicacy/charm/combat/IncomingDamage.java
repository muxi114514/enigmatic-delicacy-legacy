package net.mx.edelicacy.charm.combat;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.util.DamageSource;

/**
 * LivingAttackEvent 早于原版的各种免疫判定：在这里「消耗」护盾/失色前先判断这次伤害本来就会被忽略，
 * 避免受击无敌、火焰抗性、创造模式等情况白白吃掉一次免伤。
 */
public final class IncomingDamage {

    private IncomingDamage() {
    }

    public static boolean wouldBeIgnored(EntityLivingBase entity, DamageSource source, float amount) {
        if (amount <= 0 || entity.getHealth() <= 0 || entity.isEntityInvulnerable(source)) {
            return true;
        }
        if (source.isFireDamage() && entity.isPotionActive(MobEffects.FIRE_RESISTANCE)) {
            return true;
        }
        if (entity instanceof EntityPlayer && ((EntityPlayer) entity).capabilities.disableDamage && !source.canHarmInCreative()) {
            return true;
        }
        return entity.hurtResistantTime > entity.maxHurtResistantTime / 2.0F
                && amount <= LivingDamageAccess.getLastDamage(entity);
    }
}
