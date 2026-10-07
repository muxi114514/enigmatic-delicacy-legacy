package net.mx.edelicacy.charm;

import net.minecraft.entity.player.EntityPlayer;
import net.mx.edelicacy.registry.DelicacyItems;
import net.mx.edelicacy.util.EnigmaticBridge;

/**
 * 饥饿护符 / 暴食护符：饱食度低于 6 也能疾跑（只要大于 0）。
 * <p>原版只在客户端 EntityPlayerSP.onLivingUpdate 判断，服务端不拦，由 MixinPlayerSPSprint 调用。
 * 1.21 的口渴判定已去掉（SimpleDifficulty 不阻止疾跑）。
 */
public final class HungerSprint {

    private HungerSprint() {
    }

    public static boolean allowsLowFoodSprint(EntityPlayer player) {
        return EnigmaticBridge.hasBauble(player, DelicacyItems.HUNGRY_CHARM)
                || EnigmaticBridge.hasBauble(player, DelicacyItems.GLUTTONY_CHARM);
    }
}
