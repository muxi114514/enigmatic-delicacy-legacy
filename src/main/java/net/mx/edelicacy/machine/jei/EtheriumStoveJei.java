package net.mx.edelicacy.machine.jei;

import mezz.jei.api.IModRegistry;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.mx.edelicacy.machine.client.GuiEtheriumStove;
import net.mx.edelicacy.machine.stove.ContainerEtheriumStove;
import net.mx.edelicacy.registry.DelicacyBlocks;

/**
 * 以太炉灶的 JEI 接入：原版熔炼与农夫乐事营火两个分类的操作台、界面箭头点击区、配方转移、说明页。
 * 由整合的唯一 @JEIPlugin 类调用（本类不带注解）。
 */
public final class EtheriumStoveJei {

    /** 农夫乐事 Legacy 的营火烹饪分类 */
    private static final String FD_CAMPFIRE = "farmersdelight.campfire";

    private EtheriumStoveJei() {
    }

    public static void register(IModRegistry registry) {
        Item item = DelicacyBlocks.ETHERIUM_STOVE == null ? null : Item.getItemFromBlock(DelicacyBlocks.ETHERIUM_STOVE);
        if (item == null || item == net.minecraft.init.Items.AIR) {
            return;
        }
        ItemStack stove = new ItemStack(item);
        registry.addRecipeCatalyst(stove, VanillaRecipeCategoryUid.SMELTING, FD_CAMPFIRE);
        registry.addRecipeClickArea(GuiEtheriumStove.class, 79, 34, 24, 17, VanillaRecipeCategoryUid.SMELTING);
        registry.getRecipeTransferRegistry().addRecipeTransferHandler(ContainerEtheriumStove.class, VanillaRecipeCategoryUid.SMELTING,
                ContainerEtheriumStove.SLOT_INPUT, 1, ContainerEtheriumStove.PLAYER_START, 36);
        registry.addIngredientInfo(stove, VanillaTypes.ITEM,
                "jei.enigmaticdelicacy.etherium_stove.info1", "jei.enigmaticdelicacy.etherium_stove.info2");
    }
}
