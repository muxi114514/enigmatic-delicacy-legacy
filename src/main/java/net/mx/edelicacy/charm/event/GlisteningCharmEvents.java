package net.mx.edelicacy.charm.event;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.mx.edelicacy.charm.combat.LivingDamageAccess;
import net.mx.edelicacy.charm.glistening.GlisteningResistance;
import net.mx.edelicacy.registry.DelicacyItems;
import net.mx.edelicacy.util.EnigmaticBridge;

/**
 * 守护护符：按系数减伤（护甲前，最后结算），受击扣 5%（15 tick 内的连击只扣一次）；
 * 受伤后把 lastDamage 拉满，受击无敌期间的后续伤害全部无效（1.21 的 lastHurt = 最大生命 ×10）。
 */
public class GlisteningCharmEvents {

    private static final int HIT_PENALTY = 5;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onHurt(LivingHurtEvent event) {
        ItemStack charm = charmOf(event.getEntityLiving());
        if (charm.isEmpty()) {
            return;
        }
        int ratio = GlisteningResistance.get(charm);
        if (ratio <= 0) {
            return;
        }
        event.setAmount(event.getAmount() * (1.0F - 0.01F * ratio));
        if (GlisteningResistance.markHit(charm, event.getEntityLiving().world.getTotalWorldTime())) {
            GlisteningResistance.add(charm, -HIT_PENALTY);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onDamaged(LivingDamageEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        ItemStack charm = charmOf(entity);
        if (!charm.isEmpty() && GlisteningResistance.get(charm) > 0) {
            LivingDamageAccess.setLastDamage(entity, entity.getMaxHealth() * 10.0F);
        }
    }

    private static ItemStack charmOf(EntityLivingBase entity) {
        if (!(entity instanceof EntityPlayer) || entity.world.isRemote) {
            return ItemStack.EMPTY;
        }
        return EnigmaticBridge.getBauble((EntityPlayer) entity, DelicacyItems.GLISTENING_CHARM);
    }
}
