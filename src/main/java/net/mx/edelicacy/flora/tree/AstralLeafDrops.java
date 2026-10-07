package net.mx.edelicacy.flora.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.mx.edelicacy.registry.DelicacyItems;

/**
 * 星辰树叶的非精准/非剪刀掉落（1.21 EDBlockLoot）：
 * 树苗单独判定；木棍、叶片（开花树叶另有花瓣）各自按时运表判定，通过的候选里等权随机取一个。
 */
final class AstralLeafDrops {

    private static final float[] SAPLING = {0.05F, 0.0625F, 0.083333336F, 0.1F};
    private static final float[] STICK = {0.02F, 0.022222223F, 0.025F, 0.033333335F, 0.1F};
    private static final float[] PETAL = {0.08F, 0.1F, 0.12F, 0.15F, 0.2F};
    private static final float[] LEAF = {0.1F, 0.125F, 0.15F, 0.2F, 0.25F};

    private AstralLeafDrops() {
    }

    static void roll(NonNullList<ItemStack> drops, Random rand, int fortune, boolean blossoming, Item sapling) {
        if (rand.nextFloat() < chance(SAPLING, fortune)) {
            drops.add(new ItemStack(sapling));
        }
        List<ItemStack> candidates = new ArrayList<>(3);
        if (rand.nextFloat() < chance(STICK, fortune)) {
            candidates.add(new ItemStack(Items.STICK, 1 + rand.nextInt(2)));
        }
        if (blossoming && rand.nextFloat() < chance(PETAL, fortune)) {
            candidates.add(new ItemStack(DelicacyItems.ASTRAL_PETAL, 1 + rand.nextInt(2)));
        }
        if (rand.nextFloat() < chance(LEAF, fortune)) {
            candidates.add(new ItemStack(DelicacyItems.ASTRAL_LEAF, 1 + rand.nextInt(3)));
        }
        if (!candidates.isEmpty()) {
            drops.add(candidates.get(rand.nextInt(candidates.size())));
        }
    }

    private static float chance(float[] table, int fortune) {
        return table[Math.max(0, Math.min(fortune, table.length - 1))];
    }
}
