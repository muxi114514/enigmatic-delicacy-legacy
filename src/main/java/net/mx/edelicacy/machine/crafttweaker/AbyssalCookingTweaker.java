package net.mx.edelicacy.machine.crafttweaker;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.mx.edelicacy.machine.recipe.AbyssalCookingRecipes;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

/**
 * 禁忌转化（饕餮之锅）的 CraftTweaker 接口。只有装了 CraftTweaker 时它才会扫描并加载这个类。
 *
 * <pre>
 * mods.enigmaticdelicacy.AbyssalCooking.addRecipe(&lt;enigmaticlegacy:forbidden_fruit&gt;, &lt;ore:cropApple&gt;, 24);
 * mods.enigmaticdelicacy.AbyssalCooking.remove(&lt;enigmaticdelicacy:forbidden_meat&gt;);
 * mods.enigmaticdelicacy.AbyssalCooking.removeAll();
 * </pre>
 * 默认配方在配方注册事件的默认优先级建表，脚本在同一事件的 LOWEST 执行，所以总是作用在默认配方之后。
 */
@ZenRegister
@ZenClass("mods.enigmaticdelicacy.AbyssalCooking")
public final class AbyssalCookingTweaker {

    private AbyssalCookingTweaker() {
    }

    /** @param cost 消耗的禁忌值 */
    @ZenMethod
    public static void addRecipe(IItemStack output, IIngredient input, int cost) {
        CraftTweakerAPI.apply(new Add(output, input, cost));
    }

    @ZenMethod
    public static void remove(IItemStack output) {
        CraftTweakerAPI.apply(new Remove(output));
    }

    @ZenMethod
    public static void removeAll() {
        CraftTweakerAPI.apply(new RemoveAll());
    }

    private static final class Add implements IAction {
        private final IItemStack output;
        private final IIngredient input;
        private final int cost;

        Add(IItemStack output, IIngredient input, int cost) {
            this.output = output;
            this.input = input;
            this.cost = cost;
        }

        @Override
        public void apply() {
            ItemStack result = CraftTweakerMC.getItemStack(output);
            Ingredient ingredient = input == null ? null : CraftTweakerMC.getIngredient(input);
            if (cost < 0 || !AbyssalCookingRecipes.add(ingredient, cost, result)) {
                CraftTweakerAPI.logError("[enigmaticdelicacy] Invalid abyssal cooking recipe: empty output/input or negative cost");
            }
        }

        @Override
        public String describe() {
            return "Adding abyssal cooking recipe for " + (output == null ? "null" : output.getDisplayName());
        }
    }

    private static final class Remove implements IAction {
        private final IItemStack output;

        Remove(IItemStack output) {
            this.output = output;
        }

        @Override
        public void apply() {
            if (output == null || AbyssalCookingRecipes.removeByOutput(CraftTweakerMC.getItemStack(output)) == 0) {
                CraftTweakerAPI.logWarning("[enigmaticdelicacy] No abyssal cooking recipe found for "
                        + (output == null ? "null" : output.getDisplayName()));
            }
        }

        @Override
        public String describe() {
            return "Removing abyssal cooking recipes for " + (output == null ? "null" : output.getDisplayName());
        }
    }

    private static final class RemoveAll implements IAction {
        @Override
        public void apply() {
            AbyssalCookingRecipes.removeAll();
        }

        @Override
        public String describe() {
            return "Removing all abyssal cooking recipes";
        }
    }
}
