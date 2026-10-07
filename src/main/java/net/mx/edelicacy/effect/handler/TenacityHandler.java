package net.mx.edelicacy.effect.handler;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.mx.edelicacy.registry.DelicacyItems;
import net.mx.edelicacy.registry.DelicacyPotions;
import net.mx.edelicacy.util.EnigmaticBridge;

/**
 * 坚韧：每次受到的伤害固定减少 (等级)×0.5，佩戴闪烁护符翻倍；减免量不小于伤害时整次攻击无效。
 * <p>1.21 在 LivingIncomingDamageEvent 里同时做判定与扣减。1.12 拆成两步：
 * LivingAttackEvent 取消整次攻击（没有受击动画与击退，与 1.21 取消一致），LivingHurtEvent 扣减数值。
 */
public final class TenacityHandler {

    private static float resistance(EntityLivingBase victim) {
        Potion tenacity = DelicacyPotions.TENACITY;
        PotionEffect effect = tenacity == null ? null : victim.getActivePotionEffect(tenacity);
        if (effect == null) {
            return 0.0F;
        }
        float resistance = (effect.getAmplifier() + 1) * 0.5F;
        if (victim instanceof EntityPlayer && EnigmaticBridge.hasBauble((EntityPlayer) victim, DelicacyItems.GLISTENING_CHARM)) {
            resistance *= 2.0F;
        }
        return resistance;
    }

    @SubscribeEvent
    public void onAttack(LivingAttackEvent event) {
        EntityLivingBase victim = event.getEntityLiving();
        if (victim.world.isRemote) {
            return;
        }
        float resistance = resistance(victim);
        if (resistance > 0.0F && resistance >= event.getAmount()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onHurt(LivingHurtEvent event) {
        float resistance = resistance(event.getEntityLiving());
        if (resistance > 0.0F) {
            event.setAmount(Math.max(0.0F, event.getAmount() - resistance));
        }
    }
}
