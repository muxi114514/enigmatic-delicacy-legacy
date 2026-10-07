package net.mx.edelicacy.charm;

import keletu.enigmaticlegacy.EnigmaticConfigs;
import net.minecraft.entity.player.EntityPlayer;
import net.mx.edelicacy.util.EnigmaticBridge;

/**
 * 天体安神茶的「安眠」标记：存在 EL 的玩家持久数据里（死亡保留），七咒之人带着它可以正常入睡一次。
 * <p>EL 1.12 的失眠写在 EnigmaticEvents.onPlayerTick（睡眠计时封顶 90），由 MixinElInsomnia 读本标记放行。
 * 持久数据不同步到客户端，客户端恒为 false（只影响入睡黑屏的渐变，不影响跳夜）。
 */
public final class AstralTranquility {

    /** 与 1.21 相同的键 */
    public static final String KEY = "AstralTranquilizing";

    private AstralTranquility() {
    }

    public static boolean isTranquilized(EntityPlayer player) {
        return EnigmaticBridge.getPersistentBoolean(player, KEY);
    }

    /** EL 关闭了失眠时无需标记 */
    public static void grant(EntityPlayer player) {
        if (EnigmaticConfigs.enableInsomnia) {
            EnigmaticBridge.setPersistentBoolean(player, KEY, true);
        }
    }

    public static void clear(EntityPlayer player) {
        EnigmaticBridge.removePersistent(player, KEY);
    }
}
