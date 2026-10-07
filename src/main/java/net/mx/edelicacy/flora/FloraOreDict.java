package net.mx.edelicacy.flora;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.oredict.OreDictionary;
import net.mx.edelicacy.registry.DelicacyBlocks;
import net.mx.edelicacy.registry.DelicacyItems;

/**
 * 矿物辞典（对应 1.21 的物品标签，供 1.12 的通用配方使用）：
 * logs/planks/wooden_slabs/wooden_stairs/saplings/leaves → 原版矿辞；astral_logs → logAstral；
 * c:foods/fruit、c:seeds → 潘马斯惯用矿辞。
 */
final class FloraOreDict {

    private FloraOreDict() {
    }

    static void register() {
        Block[] logs = {DelicacyBlocks.ASTRAL_LOG, DelicacyBlocks.ASTRAL_WOOD, DelicacyBlocks.STRIPPED_ASTRAL_LOG, DelicacyBlocks.STRIPPED_ASTRAL_WOOD};
        blocks("logWood", logs);
        // 1.21 的 enigmaticdelicacy:astral_logs 标签（星辰木板配方用）
        blocks("logAstral", logs);
        blocks("plankWood", DelicacyBlocks.ASTRAL_PLANKS);
        blocks("slabWood", DelicacyBlocks.ASTRAL_SLAB);
        blocks("stairWood", DelicacyBlocks.ASTRAL_STAIRS);
        blocks("treeSapling", DelicacyBlocks.ASTRAL_SAPLING);
        blocks("treeLeaves", DelicacyBlocks.ASTRAL_LEAVES, DelicacyBlocks.BLOSSOMING_ASTRAL_LEAVES);
        items("listAllfruit", DelicacyItems.ENIGMATIC_FRUIT, DelicacyItems.ENIGMATIC_FRUIT_RED, DelicacyItems.ENIGMATIC_FRUIT_AQUA,
                DelicacyItems.ENIGMATIC_FRUIT_VIOLET, DelicacyItems.ENIGMATIC_FRUIT_MAGENTA, DelicacyItems.ENIGMATIC_FRUIT_GREEN,
                DelicacyItems.ENIGMATIC_FRUIT_BLACK, DelicacyItems.ENIGMATIC_FRUIT_BLUE);
        items("listAllseed", DelicacyItems.ENIGMATIC_SEED);
    }

    private static void blocks(String ore, Block... blocks) {
        for (Block block : blocks) {
            OreDictionary.registerOre(ore, block);
        }
    }

    private static void items(String ore, Item... items) {
        for (Item item : items) {
            OreDictionary.registerOre(ore, item);
        }
    }
}
