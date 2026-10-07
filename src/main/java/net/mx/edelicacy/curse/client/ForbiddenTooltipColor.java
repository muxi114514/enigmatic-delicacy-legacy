package net.mx.edelicacy.curse.client;

import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mx.edelicacy.curse.item.forbidden.IForbiddenItem;
import net.mx.edelicacy.util.EnigmaticBridge;

/** 禁忌物品（及禁忌之果）的紫色提示框；诅咒物品保留 EL 自己的样式 */
@SideOnly(Side.CLIENT)
public class ForbiddenTooltipColor {

    @SubscribeEvent
    public void onTooltipColor(RenderTooltipEvent.Color event) {
        if (IForbiddenItem.isForbiddenStyled(event.getStack()) && !EnigmaticBridge.isCursedItem(event.getStack())) {
            event.setBackground(0xF0161520);
            event.setBorderStart(0xF05F2E78);
            event.setBorderEnd(0x80432A60);
        }
    }
}
