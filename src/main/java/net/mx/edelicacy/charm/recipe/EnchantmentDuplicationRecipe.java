package net.mx.edelicacy.charm.recipe;

import javax.annotation.Nullable;

import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.mx.edelicacy.EnigmaticDelicacy;
import net.mx.edelicacy.registry.DelicacyItems;

/**
 * 全知之书 + 一件附魔物品 → 附魔书（复制物品的全部附魔），物品原样留在合成格，书被消耗。
 * <p>同 1.21：只认物品自身的附魔（ench），附魔书的存储附魔不可复制，避免无限复制附魔书。
 * 直接拷贝 NBT 列表，未注册的模组附魔也不会丢。动态配方，不进配方书。
 */
public class EnchantmentDuplicationRecipe extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

    public EnchantmentDuplicationRecipe() {
        setRegistryName(new ResourceLocation(EnigmaticDelicacy.MODID, "enchantment_duplication"));
    }

    @Override
    public boolean matches(InventoryCrafting inv, World world) {
        return findTarget(inv) >= 0;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        int slot = findTarget(inv);
        if (slot < 0) {
            return ItemStack.EMPTY;
        }
        ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag("StoredEnchantments", inv.getStackInSlot(slot).getEnchantmentTagList().copy());
        book.setTagCompound(tag);
        return book;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
        int target = findTarget(inv);
        for (int i = 0; i < remaining.size(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (i == target) {
                ItemStack kept = stack.copy();
                kept.setCount(1);
                remaining.set(i, kept);
            } else {
                remaining.set(i, ForgeHooks.getContainerItem(stack));
            }
        }
        return remaining;
    }

    /** 恰好一本全知之书 + 恰好一件带附魔的非附魔书物品时返回该物品的格子，否则 -1 */
    private static int findTarget(InventoryCrafting inv) {
        Item duplicator = DelicacyItems.ENCHANTMENT_DUPLICATOR;
        if (duplicator == null) {
            return -1;
        }
        int books = 0;
        int target = -1;
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.getItem() == duplicator) {
                books++;
            } else if (target < 0 && isCopyable(stack)) {
                target = i;
            } else {
                return -1;
            }
        }
        return books == 1 ? target : -1;
    }

    private static boolean isCopyable(@Nullable ItemStack stack) {
        return stack != null && stack.getItem() != Items.ENCHANTED_BOOK && stack.isItemEnchanted();
    }

    @Override
    public boolean canFit(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isDynamic() {
        return true;
    }
}
