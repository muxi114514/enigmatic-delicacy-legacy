package net.mx.edelicacy.curse.potion;

import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** 禁忌之印：护甲结算后的伤害与所有治疗都按 (等级+2)×5% 减少；虚空等「技术性」伤害不减 */
public class ForbiddenImprintHandler {

    @SubscribeEvent
    public void onDamage(LivingDamageEvent event) {
        if (CursePotions.FORBIDDEN_IMPRINT == null || event.getSource() == DamageSource.OUT_OF_WORLD) {
            return;
        }
        PotionEffect effect = event.getEntityLiving().getActivePotionEffect(CursePotions.FORBIDDEN_IMPRINT);
        if (effect != null) {
            event.setAmount(event.getAmount() * (1.0F - PotionForbiddenImprint.reduction(effect.getAmplifier())));
        }
    }

    @SubscribeEvent
    public void onHeal(LivingHealEvent event) {
        if (CursePotions.FORBIDDEN_IMPRINT == null) {
            return;
        }
        PotionEffect effect = event.getEntityLiving().getActivePotionEffect(CursePotions.FORBIDDEN_IMPRINT);
        if (effect != null) {
            event.setAmount(event.getAmount() * (1.0F - PotionForbiddenImprint.reduction(effect.getAmplifier())));
        }
    }
}
