package net.mx.edelicacy.charm;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

/**
 * 判定一次醒来是否「真睡了一觉」（夜晚被跳过），供安宁之戒回血、安眠标记结算使用。
 * <p>醒来事件的参数分不清点「离开床」和 Morpheus 跳夜（两者都是 wakeUpPlayer(false, true, true)），
 * 所以入睡时记下世界时间与总刻数：醒来时世界时间比实际流逝的多，说明期间夜晚被跳过（原版、Morpheus、Comforts 吊床都如此）。
 */
public final class SleepTracker {

    /** 玩家 → {入睡时的世界时间, 入睡时的总刻数}；醒来或下线时移除 */
    private static final Map<UUID, long[]> SLEEP_START = new ConcurrentHashMap<>();

    private SleepTracker() {
    }

    /** 服务端每 tick 调用：玩家在睡且尚未记录时记下入睡时刻 */
    public static void track(EntityPlayer player) {
        if (player.isPlayerSleeping()) {
            World world = player.world;
            SLEEP_START.computeIfAbsent(player.getUniqueID(), id -> new long[]{world.getWorldTime(), world.getTotalWorldTime()});
        }
    }

    /**
     * 醒来时调用并清除记录。
     * @param vanillaSkip 醒来事件的 updateWorld 为 false：原版全员入睡、由 WorldServer.wakeAllPlayers 叫醒
     */
    public static boolean sleptThrough(EntityPlayer player, boolean vanillaSkip) {
        long[] start = SLEEP_START.remove(player.getUniqueID());
        if (vanillaSkip) {
            return true;
        }
        if (start == null) {
            return false;
        }
        World world = player.world;
        long dayTimePassed = world.getWorldTime() - start[0];
        long realTimePassed = world.getTotalWorldTime() - start[1];
        return dayTimePassed > realTimePassed;
    }

    public static void forget(EntityPlayer player) {
        SLEEP_START.remove(player.getUniqueID());
    }
}
