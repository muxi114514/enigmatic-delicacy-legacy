package net.mx.edelicacy.curse.enchant;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.mx.edelicacy.EnigmaticDelicacy;
import net.mx.edelicacy.registry.DelicacyItems;

/**
 * 噬魂诅咒：诅咒、最高 I 级、极稀有（权重 1），附魔能力 25~50。
 * 只能附在咒魂晶刃 / 咒魂晶坠上（1.21 的 enchantable/soul_devouring_curse 标签；晶戒、晶符是未实装的占位物品）；
 * 1.21 不在附魔台出现（不在 in_enchanting_table 标签里），这里同样只能通过附魔书获得。
 */
public class EnchantmentSoulDevouring extends Enchantment {

    public EnchantmentSoulDevouring() {
        super(Rarity.VERY_RARE, EnumEnchantmentType.ALL, EntityEquipmentSlot.values());
        setRegistryName(EnigmaticDelicacy.MODID, "soul_devouring_curse");
        setName(EnigmaticDelicacy.MODID + ".soul_devouring_curse");
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public int getMinEnchantability(int level) {
        return 25;
    }

    @Override
    public int getMaxEnchantability(int level) {
        return 50;
    }

    @Override
    public boolean isCurse() {
        return true;
    }

    @Override
    public boolean isTreasureEnchantment() {
        return true;
    }

    @Override
    public boolean canApply(ItemStack stack) {
        Item item = stack.getItem();
        return item != null && (item == DelicacyItems.CURSE_CRYSTAL_KNIFE || item == DelicacyItems.CURSE_CRYSTAL_PENDANT);
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return false;
    }
}
