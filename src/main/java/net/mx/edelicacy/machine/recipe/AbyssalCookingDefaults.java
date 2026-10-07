package net.mx.edelicacy.machine.recipe;

import keletu.enigmaticlegacy.EnigmaticLegacy;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.oredict.OreIngredient;
import net.mx.edelicacy.registry.DelicacyItems;

/** 1.21 的五条禁忌转化默认配方（原为 JSON），物品注册完后在配方注册事件里建表；缺失的物品整条跳过 */
public final class AbyssalCookingDefaults {

    private AbyssalCookingDefaults() {
    }

    public static void register() {
        AbyssalCookingRecipes.removeAll();
        add(Ingredient.fromItem(Items.APPLE), 24, EnigmaticLegacy.forbiddenFruit);
        add(new OreIngredient("listAllmeatraw"), 16, DelicacyItems.FORBIDDEN_MEAT);
        add(new OreIngredient("toolKnife"), 25, DelicacyItems.FORBIDDEN_KNIFE);
        if (DelicacyItems.HUNGRY_CHARM != null) {
            add(Ingredient.fromItem(DelicacyItems.HUNGRY_CHARM), 20, DelicacyItems.FORBIDDEN_CHARM);
        }
        if (EnigmaticLegacy.unholyGrail != null) {
            add(Ingredient.fromItem(EnigmaticLegacy.unholyGrail), 32, DelicacyItems.FORBIDDEN_GRAIL);
        }
    }

    private static void add(Ingredient input, int cost, Item result) {
        if (result != null) {
            AbyssalCookingRecipes.add(input, cost, new ItemStack(result));
        }
    }
}
