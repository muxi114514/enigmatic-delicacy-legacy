package net.mx.edelicacy.machine.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;

/** 禁忌转化配方：1 个输入 + 若干禁忌值 → 产物（不可变） */
public final class AbyssalCookingRecipe {

    private final Ingredient input;
    private final int cost;
    private final ItemStack result;

    public AbyssalCookingRecipe(Ingredient input, int cost, ItemStack result) {
        this.input = input;
        this.cost = Math.max(0, cost);
        this.result = result.copy();
    }

    public Ingredient getInput() {
        return input;
    }

    public int getCost() {
        return cost;
    }

    /** 产物副本 */
    public ItemStack getResult() {
        return result.copy();
    }

    /** 输入匹配；产物本身不算（如禁忌之刃也属于 toolKnife，别把它再转成自己白白耗点） */
    public boolean matches(ItemStack stack) {
        if (stack.isEmpty() || !input.apply(stack)) {
            return false;
        }
        return !(stack.getItem() == result.getItem() && stack.getMetadata() == result.getMetadata());
    }

    public boolean isOutput(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() == result.getItem()
                && (stack.getMetadata() == result.getMetadata() || stack.getMetadata() == net.minecraftforge.oredict.OreDictionary.WILDCARD_VALUE);
    }
}
