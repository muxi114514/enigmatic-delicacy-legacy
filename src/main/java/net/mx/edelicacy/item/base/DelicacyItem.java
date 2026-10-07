package net.mx.edelicacy.item.base;

import net.minecraft.block.Block;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.mx.edelicacy.EnigmaticDelicacy;
import net.mx.edelicacy.registry.DelicacyTab;

/**
 * 普通物品基类，并提供统一的命名约定：
 * 注册名 enigmaticdelicacy:名字，翻译键 item.enigmaticdelicacy.名字.name（方块 tile.enigmaticdelicacy.名字.name）。
 */
public class DelicacyItem extends Item {

    private EnumRarity rarity = EnumRarity.COMMON;

    public DelicacyItem(String name) {
        setup(this, name);
    }

    /** 给任意物品套上本模组的注册名、翻译键与创造栏 */
    public static <T extends Item> T setup(T item, String name) {
        item.setRegistryName(EnigmaticDelicacy.MODID, name);
        item.setUnlocalizedName(EnigmaticDelicacy.MODID + "." + name);
        item.setCreativeTab(DelicacyTab.TAB);
        return item;
    }

    /** 给任意方块套上本模组的注册名、翻译键与创造栏 */
    public static <T extends Block> T setup(T block, String name) {
        block.setRegistryName(EnigmaticDelicacy.MODID, name);
        block.setUnlocalizedName(EnigmaticDelicacy.MODID + "." + name);
        block.setCreativeTab(DelicacyTab.TAB);
        return block;
    }

    /** 与方块同名的方块物品 */
    public static ItemBlock blockItem(Block block) {
        ItemBlock item = new ItemBlock(block);
        item.setRegistryName(block.getRegistryName());
        return item;
    }

    public DelicacyItem rarity(EnumRarity rarity) {
        this.rarity = rarity;
        return this;
    }

    public DelicacyItem stackSize(int size) {
        setMaxStackSize(size);
        return this;
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return rarity;
    }
}
