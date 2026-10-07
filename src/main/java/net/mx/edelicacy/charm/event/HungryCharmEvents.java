package net.mx.edelicacy.charm.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.mx.edelicacy.registry.DelicacyItems;
import net.mx.edelicacy.util.EnigmaticBridge;

/** 饥饿护符：吃喝时间缩短 8 tick（最少 8 tick）；两端都触发，客户端动画与服务端一致 */
public class HungryCharmEvents {

    private static final int REDUCTION = 8;

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onUseStart(LivingEntityUseItemEvent.Start event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer)
                || !EnigmaticBridge.hasBauble((EntityPlayer) event.getEntityLiving(), DelicacyItems.HUNGRY_CHARM)) {
            return;
        }
        ItemStack item = event.getItem();
        EnumAction action = item.getItemUseAction();
        // 1.21 对本就不足 8 tick 的物品反而会拉长到 8，这里只缩不拉
        if ((item.getItem() instanceof ItemFood || action == EnumAction.EAT || action == EnumAction.DRINK)
                && event.getDuration() > REDUCTION) {
            event.setDuration(Math.max(event.getDuration() - REDUCTION, REDUCTION));
        }
    }
}
