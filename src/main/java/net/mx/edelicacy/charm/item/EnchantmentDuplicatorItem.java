package net.mx.edelicacy.charm.item;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mx.edelicacy.item.base.DelicacyItem;
import net.mx.edelicacy.util.TooltipHelper;

/**
 * 全知之书：与任一附魔物品放进合成格，得到复制了其全部附魔的附魔书，原物品原样返还（EnchantmentDuplicationRecipe）。
 * <p>1.21 是在背包里把书右键到物品上（ItemStackedOnOtherEvent），1.12 没有该事件，改用 EL 附魔转移之书同款的合成方式。
 */
public class EnchantmentDuplicatorItem extends DelicacyItem {

    public EnchantmentDuplicatorItem() {
        super("enchantment_duplicator");
        rarity(EnumRarity.UNCOMMON);
        stackSize(1);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        TooltipHelper.line(tooltip, "tooltip.enigmaticdelicacy.enchantmentDuplicatorCraft1");
        TooltipHelper.line(tooltip, "tooltip.enigmaticdelicacy.enchantmentDuplicatorCraft2");
        TooltipHelper.empty(tooltip);
        TooltipHelper.line(tooltip, "tooltip.enigmaticlegacy.enchantmentTransposer3");
    }
}
