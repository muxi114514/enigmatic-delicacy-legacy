package net.mx.edelicacy.tool.fishing;

import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.entity.player.EntityPlayer;

/**
 * 客户端：玩家 → 正在使用的熔岩浮漂，供钓竿的「已抛竿」模型判断（对应原版 EntityPlayer#fishEntity）。
 * 只在客户端主线程读写；弱引用键，玩家实体卸载后自动释放。
 */
final class ClientHookTracker {

    private static final Map<EntityPlayer, EntityInfernalHook> HOOKS = new WeakHashMap<>();

    private ClientHookTracker() {
    }

    static void put(EntityPlayer player, EntityInfernalHook hook) {
        HOOKS.put(player, hook);
    }

    static void remove(EntityPlayer player, EntityInfernalHook hook) {
        if (HOOKS.get(player) == hook) {
            HOOKS.remove(player);
        }
    }

    static boolean hasHook(EntityPlayer player) {
        EntityInfernalHook hook = HOOKS.get(player);
        return hook != null && !hook.isDead;
    }
}
