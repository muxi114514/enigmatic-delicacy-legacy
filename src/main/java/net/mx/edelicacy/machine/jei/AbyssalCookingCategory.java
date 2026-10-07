package net.mx.edelicacy.machine.jei;

import java.util.List;

import keletu.enigmaticlegacy.EnigmaticLegacy;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.mx.edelicacy.EnigmaticDelicacy;

/** 禁忌转化的 JEI 分类：背景图自带两个槽框，输入在左、产物在右 */
public class AbyssalCookingCategory implements IRecipeCategory<AbyssalCookingWrapper> {

    public static final String UID = EnigmaticDelicacy.MODID + ".abyssal_cooking";
    private static final ResourceLocation BACKGROUND = new ResourceLocation(EnigmaticDelicacy.MODID, "textures/gui/sprites/jei/abyssal_cooking_gui.png");

    private final IDrawable background;
    private final IDrawable icon;

    public AbyssalCookingCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.drawableBuilder(BACKGROUND, 0, 0, 108, 42).setTextureSize(108, 42).build();
        this.icon = guiHelper.createDrawableIngredient(new ItemStack(EnigmaticLegacy.eldritchPan));
    }

    @Override
    public String getUid() {
        return UID;
    }

    @Override
    public String getTitle() {
        return I18n.format("gui.enigmaticdelicacy.jei.abyssal_cooking");
    }

    @Override
    public String getModName() {
        return EnigmaticDelicacy.NAME;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayout layout, AbyssalCookingWrapper wrapper, IIngredients ingredients) {
        IGuiItemStackGroup slots = layout.getItemStacks();
        // 1.12 的 JEI 槽位坐标是槽框左上角，物品画在 +1 处
        slots.init(0, true, 12, 12);
        slots.init(1, false, 78, 12);
        List<List<ItemStack>> inputs = ingredients.getInputs(VanillaTypes.ITEM);
        if (!inputs.isEmpty()) {
            slots.set(0, inputs.get(0));
        }
        List<List<ItemStack>> outputs = ingredients.getOutputs(VanillaTypes.ITEM);
        if (!outputs.isEmpty()) {
            slots.set(1, outputs.get(0));
        }
    }
}
