package net.mx.edelicacy.curse.item.gluttony;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.mx.edelicacy.curse.CurseConfig;

/**
 * 无尽贪食徽记的战斗效果（均乘缺失饥饿比例）：
 * 攻击加成（护甲前，地狱之刃护符 ×1.2）、减伤（护甲后，地狱之刃护符 ×0.6）、暴击倍率加成（地狱之刃护符且 &gt;0.75 时再 +0.24），免疫饥饿伤害。
 */
public class GluttonyCharmHandler {

    @SubscribeEvent
    public void onAttack(LivingAttackEvent event) {
        if (event.getSource() == DamageSource.STARVE && GluttonyLogic.wearing(event.getEntityLiving())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onHurt(LivingHurtEvent event) {
        if (!(event.getSource().getTrueSource() instanceof EntityLivingBase)) {
            return;
        }
        EntityLivingBase attacker = (EntityLivingBase) event.getSource().getTrueSource();
        if (!GluttonyLogic.wearing(attacker)) {
            return;
        }
        float boost = GluttonyLogic.missingFoodProperty(attacker) * (float) CurseConfig.gluttonyAttackDamage;
        if (GluttonyLogic.hasHellBladeCharm(attacker)) {
            boost *= 1.2F;
        }
        event.setAmount(event.getAmount() * (1.0F + boost));
    }

    @SubscribeEvent
    public void onDamage(LivingDamageEvent event) {
        EntityLivingBase victim = event.getEntityLiving();
        if (!GluttonyLogic.wearing(victim)) {
            return;
        }
        float resistance = GluttonyLogic.missingFoodProperty(victim) * (float) CurseConfig.gluttonyDamageResistance;
        if (GluttonyLogic.hasHellBladeCharm(victim)) {
            resistance *= 0.6F;
        }
        event.setAmount(event.getAmount() * Math.max(0.0F, 1.0F - resistance));
    }

    @SubscribeEvent
    public void onCritical(CriticalHitEvent event) {
        if (!GluttonyLogic.wearing(event.getEntityPlayer())) {
            return;
        }
        float crit = GluttonyLogic.missingFoodProperty(event.getEntityPlayer()) * (float) CurseConfig.gluttonyCritModifier;
        if (GluttonyLogic.hasHellBladeCharm(event.getEntityPlayer()) && crit > 0.75F) {
            crit += 0.24F;
        }
        event.setDamageModifier(event.getDamageModifier() + crit);
    }
}
