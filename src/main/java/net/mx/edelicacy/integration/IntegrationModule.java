package net.mx.edelicacy.integration;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.mx.edelicacy.curse.item.forbidden.ForbiddenConversion;
import net.mx.edelicacy.machine.recipe.AbyssalCookingRecipe;
import net.mx.edelicacy.machine.recipe.AbyssalCookingRecipes;
import net.minecraftforge.common.config.Configuration;
import net.mx.edelicacy.compat.ModCompat;
import net.mx.edelicacy.module.IDelicacyModule;

/** 与其它模组的联动：箱子战利品、SimpleDifficulty 口渴与温度（JEI、Hwyla 插件由各自的注解自动发现） */
public class IntegrationModule implements IDelicacyModule {

    private static final String CATEGORY = "loot";

    @Override
    public void preInit(Configuration config) {
        ChestLootHandler.seedWeight = config.getInt("EnigmaticSeedWeight", CATEGORY, 16, 0, 1000,
                "Weight of the Enigmatic Seed in overworld chests (0 disables).");
        ChestLootHandler.emptyWeight = config.getInt("EnigmaticSeedEmptyWeight", CATEGORY, 84, 0, 1000,
                "Weight of the empty entry next to the Enigmatic Seed.");
        MinecraftForge.EVENT_BUS.register(new ChestLootHandler());
    }

    @Override
    public void init() {
        if (ModCompat.simpleDifficulty) {
            SimpleDifficultyCompat.register();
        }
        // 禁忌之刃击杀掉落的转化走饕餮之锅的深渊烹饪配方表（含脚本增删），与 1.21 一样按 99 点禁忌值查
        ForbiddenConversion.setConverter((input, world) -> {
            AbyssalCookingRecipe recipe = AbyssalCookingRecipes.find(input, 99);
            return recipe == null ? ItemStack.EMPTY : recipe.getResult();
        });
    }
}
