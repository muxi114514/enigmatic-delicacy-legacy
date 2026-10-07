package net.mx.edelicacy.util;

import java.util.List;

import baubles.api.BaublesApi;
import baubles.api.IBauble;
import baubles.api.cap.IBaublesItemHandler;
import keletu.enigmaticlegacy.EnigmaticConfigs;
import keletu.enigmaticlegacy.api.cap.IForbiddenConsumed;
import keletu.enigmaticlegacy.event.SuperpositionHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.mx.eaddons.item.ItemAntiqueBag;

/**
 * 与神秘遗物 1.12（EL）、神遗拓展交互的唯一入口，对应 1.21 里 EL+ 的 EnigmaticHandler 等工具方法。
 * <ul>
 *   <li>七咒之人 = 佩戴七咒之戒；天选之人（worthy）= 佩戴七咒时间占比 ≥ 99.5%；禁忌之人 = 吃过禁忌之果</li>
 *   <li>物品的「诅咒/深渊/祝福」属性由 EL 的三张配置表决定，本模组物品在 init 时登记</li>
 * </ul>
 */
public final class EnigmaticBridge {

    private EnigmaticBridge() {
    }

    /** 佩戴七咒之戒（EL+ 的 isTheCursedOne） */
    public static boolean isCursed(EntityPlayer player) {
        return player != null && SuperpositionHandler.hasCursed(player);
    }

    /** 佩戴救赎之戒 */
    public static boolean isBlessed(EntityPlayer player) {
        return player != null && SuperpositionHandler.hasBlessed(player);
    }

    /** 七咒之人或救赎之人（EL+ 的 isTheOne） */
    public static boolean isTheOne(EntityPlayer player) {
        return isCursed(player) || isBlessed(player);
    }

    /** 天选之人（EL+ 的 isTheWorthyOne） */
    public static boolean isWorthy(EntityPlayer player) {
        return player != null && SuperpositionHandler.isTheWorthyOne(player);
    }

    /** 吃过禁忌之果（EL+ 的 ForbiddenFruit.isForbiddenCursed） */
    public static boolean isForbidden(EntityPlayer player) {
        if (player == null) {
            return false;
        }
        IForbiddenConsumed consumed = IForbiddenConsumed.get(player);
        return consumed != null && consumed.isConsumed();
    }

    public static boolean isCursedItem(ItemStack stack) {
        return !stack.isEmpty() && SuperpositionHandler.isCursed(stack);
    }

    public static boolean isEldritchItem(ItemStack stack) {
        return !stack.isEmpty() && SuperpositionHandler.isEldritch(stack);
    }

    public static boolean isBlessedItem(ItemStack stack) {
        return !stack.isEmpty() && SuperpositionHandler.isBlessed(stack);
    }

    /** init 阶段登记：诅咒物品（仅七咒之人可用） */
    public static void addCursedItem(ResourceLocation id) {
        addTo(EnigmaticConfigs.cursedItemList, id);
    }

    /** init 阶段登记：深渊物品（仅天选之人可用） */
    public static void addEldritchItem(ResourceLocation id) {
        addTo(EnigmaticConfigs.eldritchItemList, id);
    }

    /** init 阶段登记：祝福物品（救赎之人也可用） */
    public static void addBlessedItem(ResourceLocation id) {
        addTo(EnigmaticConfigs.blessedItemList, id);
    }

    private static void addTo(List<ResourceLocation> list, ResourceLocation id) {
        if (!list.contains(id)) {
            list.add(id);
        }
    }

    // ---------------- 饰品 ----------------

    public static boolean hasBauble(EntityPlayer player, Item item) {
        return player != null && item != null && BaublesApi.isBaubleEquipped(player, item) != -1;
    }

    /** 已佩戴的该饰品；没有时返回 EMPTY */
    public static ItemStack getBauble(EntityPlayer player, Item item) {
        if (player == null || item == null) {
            return ItemStack.EMPTY;
        }
        int slot = BaublesApi.isBaubleEquipped(player, item);
        return slot == -1 ? ItemStack.EMPTY : BaublesApi.getBaublesHandler(player).getStackInSlot(slot);
    }

    /** 摧毁佩戴着的该饰品（无视「无法卸下」），先走卸下回调以撤掉它挂的属性，返回是否摧毁了 */
    public static boolean destroyBauble(EntityPlayer player, Item item) {
        IBaublesItemHandler handler = BaublesApi.getBaublesHandler(player);
        boolean destroyed = false;
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (stack.getItem() == item) {
                if (item instanceof IBauble) {
                    ((IBauble) item).onUnequipped(stack, player);
                }
                handler.setStackInSlot(i, ItemStack.EMPTY);
                destroyed = true;
            }
        }
        return destroyed;
    }

    // ---------------- 背包 ----------------

    /** 背包（含副手）里第一个该物品；没有时返回 EMPTY */
    public static ItemStack findInInventory(EntityPlayer player, Item item) {
        if (player.getHeldItemOffhand().getItem() == item) {
            return player.getHeldItemOffhand();
        }
        for (ItemStack stack : player.inventory.mainInventory) {
            if (stack.getItem() == item) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    /** 背包或古旧书袋里有该物品（EL+ 的 hasItem）。书袋要反序列化，别在无条件的每 tick 判定里调用 */
    public static boolean hasItem(EntityPlayer player, Item item) {
        return !findInInventory(player, item).isEmpty() || ItemAntiqueBag.hasItemInBag(player, item);
    }

    // ---------------- 持久数据（EL 的 PlayerPersisted，死亡后保留） ----------------

    public static boolean getPersistentBoolean(EntityPlayer player, String key) {
        return SuperpositionHandler.getPersistentBoolean(player, key, false);
    }

    public static void setPersistentBoolean(EntityPlayer player, String key, boolean value) {
        SuperpositionHandler.setPersistentBoolean(player, key, value);
    }

    public static int getPersistentInt(EntityPlayer player, String key, int fallback) {
        return SuperpositionHandler.getPersistentInteger(player, key, fallback);
    }

    public static void setPersistentInt(EntityPlayer player, String key, int value) {
        SuperpositionHandler.setPersistentInteger(player, key, value);
    }

    public static void removePersistent(EntityPlayer player, String key) {
        SuperpositionHandler.removePersistentTag(player, key);
    }
}
