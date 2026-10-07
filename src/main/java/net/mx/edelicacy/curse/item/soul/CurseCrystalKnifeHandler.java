package net.mx.edelicacy.curse.item.soul;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.mx.edelicacy.curse.CurseConfig;
import net.mx.edelicacy.curse.potion.CursePotions;
import net.mx.edelicacy.registry.DelicacyItems;

/**
 * 咒魂晶刃命中（护甲后）：按概率施加 / 延长 / 升级生命诅咒；目标的诅咒已达 V 级时消耗掉它并追加目标最大生命 8% 的伤害。
 * 时长计算改用 long 并封顶，避免无限时长的永久诅咒在 (1200 + 时长) / 2 时溢出成负数。
 */
public class CurseCrystalKnifeHandler {

    private static final int MAX_LEVEL = 4;

    @SubscribeEvent
    public void onDamage(LivingDamageEvent event) {
        if (!(event.getSource().getTrueSource() instanceof EntityLivingBase) || CursePotions.HEALTH_CURSE == null) {
            return;
        }
        EntityLivingBase attacker = (EntityLivingBase) event.getSource().getTrueSource();
        if (attacker.world.isRemote || attacker.getHeldItemMainhand().getItem() != DelicacyItems.CURSE_CRYSTAL_KNIFE) {
            return;
        }
        EntityLivingBase victim = event.getEntityLiving();
        Potion curse = CursePotions.HEALTH_CURSE;
        boolean apply = victim.getRNG().nextInt(100) < CurseConfig.crystalKnifeProbability;
        PotionEffect effect = victim.getActivePotionEffect(curse);
        if (effect == null) {
            if (apply) {
                victim.addPotionEffect(new PotionEffect(curse, 600));
            }
            return;
        }
        int amplifier = effect.getAmplifier();
        int duration = effect.getDuration();
        if (amplifier >= MAX_LEVEL) {
            event.setAmount(event.getAmount() + victim.getMaxHealth() * 0.08F);
            victim.removePotionEffect(curse);
        }
        if (!apply) {
            return;
        }
        if (duration < 900) {
            // 1.21 在 V 级时把时长加在已移除的实例上，等于没有效果；其余等级延长 600 tick
            if (amplifier < MAX_LEVEL) {
                victim.addPotionEffect(new PotionEffect(curse, duration + 600, amplifier, effect.getIsAmbient(), effect.doesShowParticles()));
            }
        } else {
            int newDuration = (int) Math.min(Integer.MAX_VALUE, (1200L + duration) / 2L);
            victim.addPotionEffect(new PotionEffect(curse, newDuration, Math.min(amplifier + 1, MAX_LEVEL)));
        }
    }
}
