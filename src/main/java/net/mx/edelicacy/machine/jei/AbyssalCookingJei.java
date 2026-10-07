package net.mx.edelicacy.machine.jei;

import java.util.ArrayList;
import java.util.List;

import keletu.enigmaticlegacy.EnigmaticLegacy;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import net.minecraft.item.ItemStack;
import net.mx.edelicacy.machine.client.GuiVoraciousPan;
import net.mx.edelicacy.machine.pan.ContainerVoraciousPan;
import net.mx.edelicacy.machine.recipe.AbyssalCookingRecipe;
import net.mx.edelicacy.machine.recipe.AbyssalCookingRecipes;

/**
 * 禁忌转化的 JEI 接入。由整合的唯一 @JEIPlugin 类调用（本类不带注解），JEI 不在时不会被加载。
 */
public final class AbyssalCookingJei {

    private AbyssalCookingJei() {
    }

    public static void registerCategories(IRecipeCategoryRegistration registry) {
        registry.addRecipeCategories(new AbyssalCookingCategory(registry.getJeiHelpers().getGuiHelper()));
    }

    public static void register(IModRegistry registry) {
        List<AbyssalCookingWrapper> wrappers = new ArrayList<>();
        for (AbyssalCookingRecipe recipe : AbyssalCookingRecipes.all()) {
            wrappers.add(new AbyssalCookingWrapper(recipe));
        }
        registry.addRecipes(wrappers, AbyssalCookingCategory.UID);
        registry.addRecipeCatalyst(new ItemStack(EnigmaticLegacy.eldritchPan), AbyssalCookingCategory.UID);
        registry.addRecipeClickArea(GuiVoraciousPan.class, 68, 42, 40, 12, AbyssalCookingCategory.UID);
        registry.getRecipeTransferRegistry().addRecipeTransferHandler(ContainerVoraciousPan.class, AbyssalCookingCategory.UID,
                ContainerVoraciousPan.SLOT_INPUT, 1, ContainerVoraciousPan.PLAYER_START, 36);
    }
}
