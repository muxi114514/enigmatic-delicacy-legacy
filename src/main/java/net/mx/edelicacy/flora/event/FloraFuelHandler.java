package net.mx.edelicacy.flora.event;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.mx.edelicacy.registry.DelicacyItems;

/**
 * 燃料时长：辉光树皮/粉 200 tick（1.21 数据图）；木质小件按 1.21 原版木制品时长修正
 * （1.12 里所有木质方块一律 300）。
 */
public class FloraFuelHandler {

    @SubscribeEvent
    public void onBurnTime(FurnaceFuelBurnTimeEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) {
            return;
        }
        Item item = stack.getItem();
        if (item == DelicacyItems.GLISTENING_BARK || item == DelicacyItems.GLISTENING_POWDER || item == DelicacyItems.ASTRAL_DOOR) {
            event.setBurnTime(200);
        } else if (item == DelicacyItems.ASTRAL_SLAB) {
            event.setBurnTime(150);
        } else if (item == DelicacyItems.ASTRAL_BUTTON || item == DelicacyItems.ASTRAL_SAPLING) {
            event.setBurnTime(100);
        } else if (item == DelicacyItems.ENIGMATIC_FRUIT_CRATE) {
            // 1.21 的果箱不是燃料
            event.setBurnTime(0);
        }
    }
}
