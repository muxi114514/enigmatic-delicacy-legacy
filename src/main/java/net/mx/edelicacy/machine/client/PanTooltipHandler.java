package net.mx.edelicacy.machine.client;

import java.util.List;

import keletu.enigmaticlegacy.EnigmaticLegacy;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mx.edelicacy.machine.pan.ForbiddenPoints;
import net.mx.edelicacy.util.TooltipHelper;

/** 在神秘遗物 eldritch_pan 的提示末尾补上饕餮之锅的禁忌值与「潜行放下」说明 */
@SideOnly(Side.CLIENT)
public class PanTooltipHandler {

    @SubscribeEvent
    public void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty() || stack.getItem() != EnigmaticLegacy.eldritchPan) {
            return;
        }
        List<String> tooltip = event.getToolTip();
        int points = ForbiddenPoints.get(stack);
        String number = (points > 0 ? TextFormatting.GOLD : TextFormatting.RED) + String.valueOf(points) + TextFormatting.LIGHT_PURPLE;
        tooltip.add(TextFormatting.LIGHT_PURPLE + I18n.format("gui.enigmaticdelicacy.voracious_pan.forbidden_point", number));
        if (TooltipHelper.shiftDown()) {
            tooltip.add(I18n.format("tooltip.enigmaticdelicacy.voraciousPan5"));
            tooltip.add(I18n.format("tooltip.enigmaticdelicacy.voraciousPan6"));
        }
    }
}
