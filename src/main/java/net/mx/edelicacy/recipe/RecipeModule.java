package net.mx.edelicacy.recipe;

import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.registries.IForgeRegistry;
import net.mx.edelicacy.module.IDelicacyModule;

/** 厨锅与砧板配方（普通合成配方在 assets/enigmaticdelicacy/recipes 下） */
public class RecipeModule implements IDelicacyModule {

    @Override
    public void registerRecipes(IForgeRegistry<IRecipe> registry) {
        FarmersDelightRecipes.registerAll();
    }
}
