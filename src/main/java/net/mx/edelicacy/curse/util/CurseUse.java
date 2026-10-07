package net.mx.edelicacy.curse.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.mx.edelicacy.util.EnigmaticBridge;

/**
 * 物品使用资格（对应 EL+ 的 EnigmaticHandler.canUse），规则与 EL 1.12 的 genericEnforce 一致：
 * 诅咒物品需七咒之人、深渊物品需天选之人，祝福物品救赎之人也可用，创造模式不受限。
 */
public final class CurseUse {

    private CurseUse() {
    }

    public static boolean canUse(EntityPlayer player, ItemStack stack) {
        if (player == null) {
            return false;
        }
        if (player.capabilities.isCreativeMode) {
            return true;
        }
        boolean cursed = EnigmaticBridge.isCursedItem(stack);
        boolean eldritch = EnigmaticBridge.isEldritchItem(stack);
        if (!cursed && !eldritch) {
            return true;
        }
        if (EnigmaticBridge.isBlessed(player) && EnigmaticBridge.isBlessedItem(stack)) {
            return true;
        }
        return (!cursed || EnigmaticBridge.isCursed(player)) && (!eldritch || EnigmaticBridge.isWorthy(player));
    }
}
