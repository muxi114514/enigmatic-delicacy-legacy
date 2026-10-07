package net.mx.edelicacy.flora;

import net.minecraft.block.Block;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

/** 带稀有度的方块物品（1.21 registerBlock(..., Rarity)） */
public class FloraBlockItem extends ItemBlock {

    private final EnumRarity rarity;

    public FloraBlockItem(Block block, EnumRarity rarity) {
        super(block);
        this.rarity = rarity;
        setRegistryName(block.getRegistryName());
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return rarity;
    }
}
