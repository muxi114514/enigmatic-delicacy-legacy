package net.mx.edelicacy.charm.glistening;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.mx.eaddons.item.ItemAntiqueBag;
import net.mx.edelicacy.charm.CharmConfig;
import net.mx.edelicacy.registry.DelicacyItems;
import net.mx.edelicacy.util.EnigmaticBridge;
import net.mx.edelicacy.util.StackNBT;

/**
 * 闪耀之书的护盾值（对应 1.21 GLISTENING_SHIELD 组件），以及在背包 / 古旧书袋里定位那本书。
 * <p>书袋里的物品是从玩家 NBT 读出的副本，改完必须 {@link Book#save()} 写回。
 */
public final class GlisteningShield {

    private static final String KEY = "GlisteningShield";

    private GlisteningShield() {
    }

    public static int get(ItemStack book) {
        return Math.max(0, Math.min(CharmConfig.glisteningBookThreshold, raw(book)));
    }

    public static int raw(ItemStack book) {
        return StackNBT.getInt(book, KEY, 0);
    }

    public static void set(ItemStack book, int value) {
        StackNBT.setInt(book, KEY, Math.max(0, Math.min(CharmConfig.glisteningBookThreshold, value)));
    }

    /** 背包（含副手）优先，其次古旧书袋；都没有返回 null */
    @Nullable
    public static Book locate(EntityPlayer player) {
        Item item = DelicacyItems.THE_GLISTENING;
        if (item == null) {
            return null;
        }
        ItemStack stack = EnigmaticBridge.findInInventory(player, item);
        if (!stack.isEmpty()) {
            return new Book(stack, null, null);
        }
        if (ItemAntiqueBag.hasBag(player)) {
            NonNullList<ItemStack> bag = ItemAntiqueBag.getInventory(player);
            for (ItemStack inBag : bag) {
                if (inBag.getItem() == item) {
                    return new Book(inBag, player, bag);
                }
            }
        }
        return null;
    }

    /** 定位到的书 */
    public static final class Book {
        private final ItemStack stack;
        @Nullable
        private final EntityPlayer bagOwner;
        @Nullable
        private final NonNullList<ItemStack> bag;

        Book(ItemStack stack, @Nullable EntityPlayer bagOwner, @Nullable NonNullList<ItemStack> bag) {
            this.stack = stack;
            this.bagOwner = bagOwner;
            this.bag = bag;
        }

        public ItemStack stack() {
            return stack;
        }

        public int shield() {
            return get(stack);
        }

        public void setShield(int value) {
            set(stack, value);
            save();
        }

        /** 书在书袋里时写回玩家 NBT；在背包里则无需处理 */
        public void save() {
            if (bagOwner != null && bag != null) {
                ItemAntiqueBag.setInventory(bagOwner, bag);
            }
        }
    }
}
