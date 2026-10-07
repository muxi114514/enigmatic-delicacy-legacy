package net.mx.edelicacy.curse.block;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mx.edelicacy.curse.client.CurseTooltips;
import net.mx.edelicacy.curse.item.forbidden.IForbiddenItem;

/** 两个派方块的物品：混沌肉派（禁忌物品，稀有）与神圣果派（史诗、附魔光效，诅咒 + 祝福由 EL 配置表登记） */
public abstract class ItemCursePieBlock extends ItemBlock {

    protected ItemCursePieBlock(Block block) {
        super(block);
        setRegistryName(block.getRegistryName());
    }

    /** 混沌肉派 */
    public static class Chaotic extends ItemCursePieBlock implements IForbiddenItem {

        public Chaotic(Block block) {
            super(block);
        }

        @Override
        public EnumRarity getRarity(ItemStack stack) {
            return EnumRarity.RARE;
        }

        @Override
        @SideOnly(Side.CLIENT)
        public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
            CurseTooltips.forbiddenOnly(tooltip);
        }
    }

    /** 神圣果派 */
    public static class Divine extends ItemCursePieBlock {

        public Divine(Block block) {
            super(block);
            setMaxStackSize(1);
        }

        @Override
        public EnumRarity getRarity(ItemStack stack) {
            return EnumRarity.EPIC;
        }

        @Override
        @SideOnly(Side.CLIENT)
        public boolean hasEffect(ItemStack stack) {
            return true;
        }

        @Override
        @SideOnly(Side.CLIENT)
        public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
            if (CurseTooltips.shift()) {
                CurseTooltips.line(tooltip, "tooltip.enigmaticdelicacy.divineFruitPieBlock");
            } else {
                CurseTooltips.holdShift(tooltip);
            }
        }
    }
}
