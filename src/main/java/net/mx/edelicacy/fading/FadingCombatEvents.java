package net.mx.edelicacy.fading;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.mx.edelicacy.charm.combat.IncomingDamage;
import net.mx.edelicacy.registry.DelicacyItems;
import net.mx.edelicacy.registry.DelicacyPotions;
import net.mx.edelicacy.util.EnigmaticBridge;

/**
 * 失色的两种消耗方式：
 * <ul>
 *   <li>受到非自身直接造成的伤害：整次免疫（LivingAttackEvent 取消）。虚空 / kill 这类无视创造模式的伤害不拦，
 *       1.21 会拦下 /kill，让带卷轴的玩家掉进虚空也死不了</li>
 *   <li>自身近战命中（直接伤害来源是自己）：本次伤害 ×(1 + 加成)</li>
 * </ul>
 * 佩戴失色卷轴时触发后进入卷轴冷却。
 */
public class FadingCombatEvents {

    @SubscribeEvent
    public void onAttacked(LivingAttackEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        Potion fading = DelicacyPotions.FADING;
        if (fading == null || entity.world.isRemote || !entity.isPotionActive(fading)) {
            return;
        }
        DamageSource source = event.getSource();
        if (source.getImmediateSource() == entity || source.canHarmInCreative()
                || IncomingDamage.wouldBeIgnored(entity, source, event.getAmount())) {
            return;
        }
        event.setCanceled(true);
        consume(entity, fading);
    }

    @SubscribeEvent
    public void onDamage(LivingDamageEvent event) {
        Entity direct = event.getSource().getImmediateSource();
        Potion fading = DelicacyPotions.FADING;
        if (fading == null || !(direct instanceof EntityLivingBase) || direct == event.getEntityLiving()) {
            return;
        }
        EntityLivingBase attacker = (EntityLivingBase) direct;
        if (attacker.world.isRemote || !attacker.isPotionActive(fading)) {
            return;
        }
        event.setAmount(event.getAmount() * (1.0F + 0.01F * FadingConfig.specialDamageBoost));
        consume(attacker, fading);
    }

    private static void consume(EntityLivingBase entity, Potion fading) {
        entity.removePotionEffect(fading);
        if (entity instanceof EntityPlayer && DelicacyItems.FADING_SCROLL != null
                && EnigmaticBridge.hasBauble((EntityPlayer) entity, DelicacyItems.FADING_SCROLL)) {
            ((EntityPlayer) entity).getCooldownTracker().setCooldown(DelicacyItems.FADING_SCROLL, FadingConfig.scrollCooldown);
        }
        if (entity.world instanceof WorldServer) {
            ((WorldServer) entity.world).spawnParticle(EnumParticleTypes.CLOUD, entity.posX, entity.posY + entity.getEyeHeight(),
                    entity.posZ, 5, 0.0D, 0.0D, 0.0D, 0.05D);
        }
    }
}
