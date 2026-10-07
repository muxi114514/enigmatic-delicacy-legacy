package net.mx.edelicacy.effect.handler;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.mx.edelicacy.registry.DelicacyPotions;

/**
 * 亡灵凋谢：
 * <ul>
 *   <li>近战攻击亡灵生物：伤害 +(2+2×等级)，再 ×(1+0.05×等级)</li>
 *   <li>免疫凋零：拒绝施加凋零效果（阻断层）+ 取消凋零伤害 + 每 tick 清掉已有的凋零（清理层）</li>
 * </ul>
 */
public final class WitheringSmiteHandler {

    private static boolean hasSmite(EntityLivingBase entity) {
        Potion smite = DelicacyPotions.WITHERING_SMITE;
        return smite != null && entity.isPotionActive(smite);
    }

    private static boolean isWither(DamageSource source) {
        return source == DamageSource.WITHER || "wither".equals(source.getDamageType());
    }

    /** 最先加成，后续减伤按加成后的数值计算（与 1.21 的 HIGHEST 一致） */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onHurt(LivingHurtEvent event) {
        Potion smite = DelicacyPotions.WITHERING_SMITE;
        Entity direct = event.getSource().getImmediateSource();
        if (smite == null || !(direct instanceof EntityLivingBase)
                || event.getEntityLiving().getCreatureAttribute() != EnumCreatureAttribute.UNDEAD) {
            return;
        }
        PotionEffect effect = ((EntityLivingBase) direct).getActivePotionEffect(smite);
        if (effect != null) {
            int amplifier = effect.getAmplifier();
            event.setAmount((event.getAmount() + amplifier * 2 + 2) * (1.0F + amplifier * 0.05F));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onAttack(LivingAttackEvent event) {
        if (isWither(event.getSource()) && hasSmite(event.getEntityLiving())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onApplicable(PotionEvent.PotionApplicableEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        if (!entity.world.isRemote && event.getPotionEffect().getPotion() == MobEffects.WITHER && hasSmite(entity)) {
            event.setResult(Event.Result.DENY);
        }
    }

    /** 获得亡灵凋谢之前已有的凋零：在效果表遍历之前移除 */
    @SubscribeEvent
    public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        if (!entity.world.isRemote && entity.isPotionActive(MobEffects.WITHER) && hasSmite(entity)) {
            entity.removePotionEffect(MobEffects.WITHER);
        }
    }
}
