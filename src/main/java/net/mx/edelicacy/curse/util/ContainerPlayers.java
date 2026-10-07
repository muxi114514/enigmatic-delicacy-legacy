package net.mx.edelicacy.curse.util;

import java.lang.reflect.Field;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerRepair;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 从容器反查玩家：合成配方的 matches 和铁砧事件都拿不到玩家，1.21 能直接拿到。
 * 只在服务端主线程使用。
 */
public final class ContainerPlayers {

    private static final Logger LOG = LogManager.getLogger("enigmaticdelicacy");
    @Nullable
    private static Field eventHandler;
    private static boolean lookupDone;

    private ContainerPlayers() {
    }

    /** 正在使用该合成栏的玩家；自动合成机等找不到时返回 null */
    @Nullable
    public static EntityPlayer craftingPlayer(InventoryCrafting inv) {
        EntityPlayer player = ForgeHooks.getCraftingPlayer();
        if (player != null) {
            return player;
        }
        Container container = container(inv);
        if (container == null) {
            return null;
        }
        for (Slot slot : container.inventorySlots) {
            if (slot.inventory instanceof InventoryPlayer) {
                return ((InventoryPlayer) slot.inventory).player;
            }
        }
        return null;
    }

    /**
     * 正在用铁砧、左槽恰好是这个物品实例的服务端玩家的等级；客户端或找不到时返回 0
     * （客户端的花费随后会被服务端同步覆盖）。
     */
    public static int anvilUserLevel(ItemStack left) {
        if (!FMLCommonHandler.instance().getEffectiveSide().isServer()) {
            return 0;
        }
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server == null) {
            return 0;
        }
        for (EntityPlayerMP player : server.getPlayerList().getPlayers()) {
            if (player.openContainer instanceof ContainerRepair && player.openContainer.inventorySlots.size() > 0
                    && player.openContainer.getSlot(0).getStack() == left) {
                return player.experienceLevel;
            }
        }
        return 0;
    }

    @Nullable
    private static Container container(InventoryCrafting inv) {
        if (!lookupDone) {
            lookupDone = true;
            try {
                eventHandler = ReflectionHelper.findField(InventoryCrafting.class, "eventHandler", "field_70465_c");
            } catch (RuntimeException e) {
                LOG.warn("InventoryCrafting container field not found, cursed recipes need ForgeHooks crafting player", e);
            }
        }
        if (eventHandler == null) {
            return null;
        }
        try {
            return (Container) eventHandler.get(inv);
        } catch (IllegalAccessException | ClassCastException e) {
            return null;
        }
    }
}
