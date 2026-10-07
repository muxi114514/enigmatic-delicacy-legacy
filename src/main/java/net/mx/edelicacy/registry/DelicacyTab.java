package net.mx.edelicacy.registry;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** 本模组创造栏，图标为以太炉 */
public final class DelicacyTab {

    public static final CreativeTabs TAB = new CreativeTabs("enigmaticdelicacy") {
        @Override
        @SideOnly(Side.CLIENT)
        public ItemStack getTabIconItem() {
            return DelicacyItems.ETHERIUM_STOVE != null ? new ItemStack(DelicacyItems.ETHERIUM_STOVE) : new ItemStack(Items.CAKE);
        }
    };

    private DelicacyTab() {
    }
}
