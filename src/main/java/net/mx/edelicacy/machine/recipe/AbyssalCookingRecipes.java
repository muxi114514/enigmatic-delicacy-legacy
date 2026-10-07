package net.mx.edelicacy.machine.recipe;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;

/**
 * 禁忌转化（abyssal cooking）配方表，公开给其他模块与附属模组：
 * 默认配方在配方注册事件（默认优先级）建表，CraftTweaker 在同一事件的 LOWEST 增删。读多写少，用 CopyOnWriteArrayList。
 */
public final class AbyssalCookingRecipes {

    private static final List<AbyssalCookingRecipe> RECIPES = new CopyOnWriteArrayList<>();

    private AbyssalCookingRecipes() {
    }

    /** 追加配方；输入或产物为空时忽略并返回 false */
    public static boolean add(@Nullable Ingredient input, int cost, ItemStack result) {
        if (input == null || input == Ingredient.EMPTY || result.isEmpty()) {
            return false;
        }
        RECIPES.add(new AbyssalCookingRecipe(input, cost, result));
        return true;
    }

    /** 按产物移除，返回移除条数 */
    public static int removeByOutput(ItemStack output) {
        int before = RECIPES.size();
        RECIPES.removeIf(recipe -> recipe.isOutput(output));
        return before - RECIPES.size();
    }

    public static void removeAll() {
        RECIPES.clear();
    }

    /** 只读视图（JEI 展示用） */
    public static List<AbyssalCookingRecipe> all() {
        return Collections.unmodifiableList(RECIPES);
    }

    /** 不看禁忌值，找第一条匹配的配方 */
    @Nullable
    public static AbyssalCookingRecipe find(ItemStack input) {
        for (AbyssalCookingRecipe recipe : RECIPES) {
            if (recipe.matches(input)) {
                return recipe;
            }
        }
        return null;
    }

    /** 找第一条匹配且禁忌值足够的配方（与 1.21 的 matches 一致：点数不够视为不匹配） */
    @Nullable
    public static AbyssalCookingRecipe find(ItemStack input, int points) {
        for (AbyssalCookingRecipe recipe : RECIPES) {
            if (recipe.getCost() <= points && recipe.matches(input)) {
                return recipe;
            }
        }
        return null;
    }
}
