package net.mx.edelicacy.food.item;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;

/** 1.21 蜂蜜瓶的替代判定：矿辞 dropHoney（潘马斯）、未来版蜂蜜瓶、兜底白糖 */
public final class HoneyHelper {

    private static final ResourceLocation FUTURE_HONEY_BOTTLE = new ResourceLocation("futuremc", "honey_bottle");
    private static final String HONEY_ORE = "dropHoney";

    private HoneyHelper() {
    }

    public static boolean isHoney(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (stack.getItem() == Items.SUGAR || FUTURE_HONEY_BOTTLE.equals(stack.getItem().getRegistryName())) {
            return true;
        }
        if (!OreDictionary.doesOreNameExist(HONEY_ORE)) {
            return false;
        }
        int honeyId = OreDictionary.getOreID(HONEY_ORE);
        for (int id : OreDictionary.getOreIDs(stack)) {
            if (id == honeyId) {
                return true;
            }
        }
        return false;
    }
}
