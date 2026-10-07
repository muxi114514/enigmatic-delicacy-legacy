package net.mx.edelicacy.integration.jei;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.IVanillaRecipeFactory;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.api.ingredients.VanillaTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.mx.edelicacy.machine.jei.AbyssalCookingJei;
import net.mx.edelicacy.machine.jei.EtheriumStoveJei;
import net.mx.edelicacy.registry.DelicacyItems;

/**
 * 本模组唯一的 JEI 插件：深渊烹饪分类、以太炉催化剂（各自的入口在 machine.jei）、
 * 两条铁砧配方、附魔复制书的动态配方说明。只在装了 JEI 时由 JEI 加载。
 */
@JEIPlugin
public class DelicacyJeiPlugin implements IModPlugin {

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        AbyssalCookingJei.registerCategories(registry);
    }

    @Override
    public void register(IModRegistry registry) {
        AbyssalCookingJei.register(registry);
        EtheriumStoveJei.register(registry);
        registerAnvilRecipes(registry);
        if (DelicacyItems.ENCHANTMENT_DUPLICATOR != null) {
            registry.addIngredientInfo(new ItemStack(DelicacyItems.ENCHANTMENT_DUPLICATOR), VanillaTypes.ITEM,
                    "jei.enigmaticdelicacy.enchantment_duplicator.info");
        }
        // 消逝玻璃在原版里就不进创造栏，JEI 里也隐藏
        if (DelicacyItems.FADING_GLASS != null) {
            registry.getJeiHelpers().getIngredientBlacklist().addIngredientToBlacklist(new ItemStack(DelicacyItems.FADING_GLASS));
        }
    }

    /** 诅咒之刃 + 诅咒灵魂碎片 → 诅咒水晶刀；未见证的护符 + 碎片 → 诅咒水晶吊坠 */
    private static void registerAnvilRecipes(IModRegistry registry) {
        Item fragment = DelicacyItems.CURSED_SOUL_FRAGMENT;
        if (fragment == null) {
            return;
        }
        IVanillaRecipeFactory factory = registry.getJeiHelpers().getVanillaRecipeFactory();
        List<IRecipeWrapper> recipes = new ArrayList<>();
        if (DelicacyItems.CURSE_BLADE != null && DelicacyItems.CURSE_CRYSTAL_KNIFE != null) {
            recipes.add(factory.createAnvilRecipe(new ItemStack(DelicacyItems.CURSE_BLADE),
                    Collections.singletonList(new ItemStack(fragment)),
                    Collections.singletonList(new ItemStack(DelicacyItems.CURSE_CRYSTAL_KNIFE))));
        }
        Item amulet = ForgeRegistries.ITEMS.getValue(new ResourceLocation("enigmaticlegacy", "enigmatic_amulet"));
        if (amulet != null && DelicacyItems.CURSE_CRYSTAL_PENDANT != null) {
            recipes.add(factory.createAnvilRecipe(new ItemStack(amulet, 1, 0),
                    Collections.singletonList(new ItemStack(fragment)),
                    Collections.singletonList(new ItemStack(DelicacyItems.CURSE_CRYSTAL_PENDANT))));
        }
        registry.addRecipes(recipes, VanillaRecipeCategoryUid.ANVIL);
    }
}
