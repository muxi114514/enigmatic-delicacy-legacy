package net.mx.edelicacy.curse.item.divine;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumActionResult;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.mx.edelicacy.curse.util.CurseUse;
import net.mx.edelicacy.registry.DelicacyItems;

/**
 * 不配之人使用神圣果派即死（1.21 在 use 里 kill）。EL 1.12 会在 HIGH 优先级先把诅咒物品的右键取消掉，
 * 物品自己的右键方法根本收不到，所以在 HIGHEST 抢先处理。
 */
public class DivineFruitPieHandler {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        EntityPlayer player = event.getEntityPlayer();
        if (event.getItemStack().getItem() != DelicacyItems.DIVINE_FRUIT_PIE || CurseUse.canUse(player, event.getItemStack())) {
            return;
        }
        if (!player.world.isRemote) {
            player.onKillCommand();
        }
        event.setCanceled(true);
        event.setCancellationResult(EnumActionResult.FAIL);
    }
}
