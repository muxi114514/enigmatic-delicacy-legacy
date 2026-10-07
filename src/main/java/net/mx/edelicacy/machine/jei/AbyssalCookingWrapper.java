package net.mx.edelicacy.machine.jei;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;
import net.mx.edelicacy.machine.recipe.AbyssalCookingRecipe;

/** 一条禁忌转化配方在 JEI 中的数据；悬停箭头显示消耗的禁忌值 */
public class AbyssalCookingWrapper implements IRecipeWrapper {

    private final AbyssalCookingRecipe recipe;

    public AbyssalCookingWrapper(AbyssalCookingRecipe recipe) {
        this.recipe = recipe;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.ITEM, Collections.singletonList(Arrays.asList(recipe.getInput().getMatchingStacks())));
        ingredients.setOutput(VanillaTypes.ITEM, recipe.getResult());
    }

    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY) {
        if (mouseX >= 33 && mouseY >= 15 && mouseX <= 74 && mouseY <= 26) {
            String cost = TextFormatting.GOLD + String.valueOf(recipe.getCost()) + TextFormatting.LIGHT_PURPLE;
            return Collections.singletonList(TextFormatting.LIGHT_PURPLE + I18n.format("gui.enigmaticdelicacy.jei.abyssal_cooking.cost", cost));
        }
        return Collections.emptyList();
    }
}
