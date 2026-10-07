package net.mx.edelicacy.curse.item.grail;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.mx.eaddons.attribute.AttributeSources;
import net.mx.eaddons.attribute.EnigmaticAttributes;
import net.mx.edelicacy.curse.item.forbidden.ForbiddenCharmHandler;
import net.mx.edelicacy.curse.potion.CursePotions;
import net.mx.edelicacy.data.DelicacyData;

/**
 * 禁忌状态（禁忌华盏）：
 * <ul>
 *   <li>计时每秒结算一次（每次 -20），避免每 tick 改数据导致每 tick 同步</li>
 *   <li>生命窃取 ×1.2（神遗拓展属性来源，乘总值）</li>
 *   <li>受到伤害时消耗一级禁忌之印抵挡这次伤害（无敌帧内的重复判定不消耗）</li>
 *   <li>进食 / 饮用时间减半再减 4 tick</li>
 * </ul>
 */
public class ForbiddenGrailHandler {

    public static boolean isActive(EntityPlayer player) {
        return DelicacyData.get(player).getForbiddenTick() > 0;
    }

    /** preInit 时登记属性来源 */
    public static void registerAttributeSources() {
        AttributeSources.register("enigmaticdelicacy.forbidden_grail", EnigmaticAttributes.LIFESTEAL, 2, 10,
                player -> isActive(player) ? 0.2D : 0.0D);
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.world.isRemote || event.player.ticksExisted % 20 != 0) {
            return;
        }
        DelicacyData data = DelicacyData.get(event.player);
        if (data.getForbiddenTick() > 0) {
            data.setForbiddenTick(data.getForbiddenTick() - 20);
        }
    }

    @SubscribeEvent
    public void onAttack(LivingAttackEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer) || CursePotions.FORBIDDEN_IMPRINT == null) {
            return;
        }
        EntityPlayer player = (EntityPlayer) event.getEntityLiving();
        if (player.world.isRemote || !isActive(player) || player.hurtResistantTime > player.maxHurtResistantTime / 2) {
            return;
        }
        PotionEffect imprint = player.getActivePotionEffect(CursePotions.FORBIDDEN_IMPRINT);
        if (imprint == null) {
            return;
        }
        int amplifier = imprint.getAmplifier();
        int duration = imprint.getDuration();
        player.removePotionEffect(CursePotions.FORBIDDEN_IMPRINT);
        if (amplifier > 0) {
            player.addPotionEffect(new PotionEffect(CursePotions.FORBIDDEN_IMPRINT, duration, amplifier - 1));
        }
        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onUseStart(LivingEntityUseItemEvent.Start event) {
        if (event.getEntityLiving() instanceof EntityPlayer && isActive((EntityPlayer) event.getEntityLiving())
                && ForbiddenCharmHandler.isConsumable(event.getItem())) {
            event.setDuration(Math.max(event.getDuration() / 2 - 4, 4));
        }
    }
}
