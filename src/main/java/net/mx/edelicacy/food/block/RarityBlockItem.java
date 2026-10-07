package net.mx.edelicacy.food.block;

import net.minecraft.block.Block;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

/** 带稀有度的方块物品（1.21 的宴席/派方块物品为 UNCOMMON） */
public class RarityBlockItem extends ItemBlock {

    private final EnumRarity rarity;

    public RarityBlockItem(Block block, EnumRarity rarity) {
        super(block);
        this.rarity = rarity;
        setRegistryName(block.getRegistryName());
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return rarity;
    }
}
