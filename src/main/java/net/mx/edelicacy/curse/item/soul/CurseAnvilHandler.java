package net.mx.edelicacy.curse.item.soul;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.mx.edelicacy.curse.util.ContainerPlayers;
import net.mx.edelicacy.curse.util.CurseRefs;
import net.mx.edelicacy.registry.DelicacyItems;

/**
 * 铁砧：诅咒刃片 + 咒魂晶碎片 → 咒魂晶刃；未见证的护身符（EL 谜之护符 meta 0）+ 碎片 → 咒魂晶坠。
 * 花费与 1.21 相同：max(20, 使用者当前等级)，即至少 20 级、等级更高时吃掉全部等级；消耗 1 个碎片。
 */
public class CurseAnvilHandler {

    @SubscribeEvent
    public void onAnvilUpdate(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();
        if (left.isEmpty() || right.isEmpty() || right.getItem() != DelicacyItems.CURSED_SOUL_FRAGMENT) {
            return;
        }
        Item output = null;
        if (left.getItem() == DelicacyItems.CURSE_BLADE) {
            output = DelicacyItems.CURSE_CRYSTAL_KNIFE;
        } else if (left.getItem() == CurseRefs.enigmaticAmulet() && left.getMetadata() == 0) {
            output = DelicacyItems.CURSE_CRYSTAL_PENDANT;
        }
        if (output == null) {
            return;
        }
        event.setOutput(new ItemStack(output));
        event.setCost(Math.max(20, ContainerPlayers.anvilUserLevel(left)));
        event.setMaterialCost(1);
    }
}
