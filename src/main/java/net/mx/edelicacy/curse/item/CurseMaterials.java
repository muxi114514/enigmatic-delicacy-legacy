package net.mx.edelicacy.curse.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.oredict.OreDictionary;
import net.mx.edelicacy.curse.util.RegistryItem;
import net.mx.edelicacy.registry.DelicacyItems;

/** 三把刀的工具材料（1.21 的 SimpleTier：挖掘等级、耐久、效率、伤害、附魔能力）与修复材料 */
public final class CurseMaterials {

    /** 金级挖掘、1276 耐久 */
    public static final Item.ToolMaterial CURSE_BLADE = EnumHelper.addToolMaterial("ENIGMATICDELICACY_CURSE_BLADE", 0, 1276, 5.0F, 3.0F, 19);
    /** 钻石级挖掘、1692 耐久 */
    public static final Item.ToolMaterial CURSE_CRYSTAL = EnumHelper.addToolMaterial("ENIGMATICDELICACY_CURSE_CRYSTAL", 3, 1692, 6.0F, 3.0F, 26);
    /** 钻石级挖掘、1487 耐久 */
    public static final Item.ToolMaterial FORBIDDEN = EnumHelper.addToolMaterial("ENIGMATICDELICACY_FORBIDDEN", 3, 1487, 6.0F, 3.5F, 21);

    private static final RegistryItem NETHERITE_SCRAP = new RegistryItem("netherized:netherite_scrap");

    private CurseMaterials() {
    }

    /** 下界合金碎片（矿辞 scrapNetherite，回落 ingotNetherite）修复，对应 1.21 的 netherite_scrap */
    public static boolean isNetheriteScrap(ItemStack repair) {
        return (!repair.isEmpty() && repair.getItem() == NETHERITE_SCRAP.get())
                || hasOre(repair, "scrapNetherite") || hasOre(repair, "ingotNetherite");
    }

    /** 禁忌肉团修复 */
    public static boolean isForbiddenMeat(ItemStack repair) {
        return !repair.isEmpty() && repair.getItem() == DelicacyItems.FORBIDDEN_MEAT;
    }

    private static boolean hasOre(ItemStack stack, String ore) {
        if (stack.isEmpty() || !OreDictionary.doesOreNameExist(ore)) {
            return false;
        }
        int id = OreDictionary.getOreID(ore);
        for (int oreId : OreDictionary.getOreIDs(stack)) {
            if (oreId == id) {
                return true;
            }
        }
        return false;
    }
}
