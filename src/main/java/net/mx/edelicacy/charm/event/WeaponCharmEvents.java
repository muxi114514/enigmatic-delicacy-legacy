package net.mx.edelicacy.charm.event;

import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.mx.edelicacy.registry.DelicacyItems;
import net.mx.edelicacy.util.EnigmaticBridge;

/** 武器大师之荣：慢武器暴击 +50%，快武器近战命中附加 59 tick 虚弱 */
public class WeaponCharmEvents {

    private static final double SLOW_WEAPON = 1.5D;
    private static final double FAST_WEAPON = 1.75D;

    /** 非暴击时 Forge 不采用该倍率，所以无需判断是否暴击 */
    @SubscribeEvent
    public void onCriticalHit(CriticalHitEvent event) {
        EntityPlayer player = event.getEntityPlayer();
        if (EnigmaticBridge.hasBauble(player, DelicacyItems.WEAPON_CHARM) && attackSpeed(player) < SLOW_WEAPON) {
            event.setDamageModifier(event.getDamageModifier() + 0.5F);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onDamage(LivingDamageEvent event) {
        DamageSource source = event.getSource();
        Entity attacker = source.getTrueSource();
        if (!(attacker instanceof EntityPlayer) || event.getAmount() <= 0
                || !("player".equals(source.getDamageType()) || "mob".equals(source.getDamageType()))) {
            return;
        }
        EntityPlayer player = (EntityPlayer) attacker;
        if (EnigmaticBridge.hasBauble(player, DelicacyItems.WEAPON_CHARM) && attackSpeed(player) > FAST_WEAPON) {
            event.getEntityLiving().addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 59));
        }
    }

    private static double attackSpeed(EntityPlayer player) {
        IAttributeInstance speed = player.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED);
        return speed == null ? 4.0D : speed.getAttributeValue();
    }
}
