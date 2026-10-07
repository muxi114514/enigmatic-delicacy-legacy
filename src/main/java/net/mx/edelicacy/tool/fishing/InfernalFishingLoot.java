package net.mx.edelicacy.tool.fishing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.mx.edelicacy.compat.ModCompat;
import net.mx.edelicacy.registry.DelicacyItems;

/**
 * 熔岩钓鱼的战利品（1.21 的 gameplay/infernal_fishing 三张表），写在代码里以便跳过缺失的可选物品：
 * 哭泣黑曜石 / 远古残骸 / 下界合金碎片只在装了 Netherized 时出现；宝藏只在开阔熔岩出现。
 * 品质随幸运调整权重：杂物 96 − 2×幸运、宝藏 4 + 2×幸运（与原版战利品表的 quality 规则相同）。
 */
final class InfernalFishingLoot {

    private static final int JUNK_WEIGHT = 96;
    private static final int JUNK_QUALITY = -2;
    private static final int TREASURE_WEIGHT = 4;
    private static final int TREASURE_QUALITY = 2;

    private static volatile List<Entry> junk;
    private static volatile List<Entry> treasure;

    private InfernalFishingLoot() {
    }

    /** 抽一次；结果至多一件 */
    static List<ItemStack> roll(Random rand, float luck, boolean openLava) {
        ensureBuilt();
        int junkWeight = Math.max(0, MathHelper.floor(JUNK_WEIGHT + JUNK_QUALITY * luck));
        int treasureWeight = openLava && !treasure.isEmpty() ? Math.max(0, MathHelper.floor(TREASURE_WEIGHT + TREASURE_QUALITY * luck)) : 0;
        int sum = junkWeight + treasureWeight;
        if (sum <= 0) {
            return Collections.emptyList();
        }
        List<Entry> pool = rand.nextInt(sum) < treasureWeight ? treasure : junk;
        Entry entry = pick(pool, rand);
        return entry == null ? Collections.emptyList() : Collections.singletonList(entry.create(rand));
    }

    @Nullable
    private static Entry pick(List<Entry> pool, Random rand) {
        int total = 0;
        for (Entry entry : pool) {
            total += entry.weight;
        }
        if (total <= 0) {
            return null;
        }
        int roll = rand.nextInt(total);
        for (Entry entry : pool) {
            roll -= entry.weight;
            if (roll < 0) {
                return entry;
            }
        }
        return null;
    }

    private static void ensureBuilt() {
        if (junk == null) {
            synchronized (InfernalFishingLoot.class) {
                if (junk == null) {
                    build();
                }
            }
        }
    }

    private static void build() {
        List<Entry> junkList = new ArrayList<>();
        add(junkList, Item.getItemFromBlock(Blocks.NETHERRACK), 27, 1, 1, -1, 0);
        add(junkList, Items.GOLD_NUGGET, 21, 1, 7, -1, 0);
        add(junkList, Items.GOLDEN_BOOTS, 12, 1, 1, 0.8F, 0);
        add(junkList, Items.GUNPOWDER, 11, 1, 2, -1, 0);
        add(junkList, Items.BLAZE_POWDER, 14, 1, 4, -1, 0);
        if (ModCompat.netherized) {
            add(junkList, item("netherized:crying_obsidian"), 7, 1, 1, -1, 0);
        }
        add(junkList, Item.getItemFromBlock(Blocks.OBSIDIAN), 5, 1, 1, -1, 0);
        add(junkList, DelicacyItems.INFERNAL_FISHING_ROD, 3, 1, 1, 0.5F, 0);

        List<Entry> treasureList = new ArrayList<>();
        if (ModCompat.netherized) {
            add(treasureList, item("netherized:ancient_debris"), 24, 1, 1, -1, 0);
            add(treasureList, item("netherized:netherite_scrap"), 21, 1, 2, -1, 0);
        }
        add(treasureList, Items.BOOK, 20, 1, 1, -1, 30);
        add(treasureList, Items.BOOK, 17, 1, 1, -1, 48);
        add(treasureList, item("enigmaticlegacy:magma_heart"), 13, 1, 1, -1, 0);
        Item lantern = item("enigmaticlegacy:illusion_lantern");
        add(treasureList, lantern != null ? lantern : item("eaddons:illusion_lantern"), 5, 1, 1, -1, 0);

        treasure = Collections.unmodifiableList(treasureList);
        junk = Collections.unmodifiableList(junkList);
    }

    @Nullable
    private static Item item(String id) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
        return item == Items.AIR ? null : item;
    }

    private static void add(List<Entry> list, @Nullable Item item, int weight, int min, int max, float maxDurability, int enchantLevels) {
        if (item != null && item != Items.AIR) {
            list.add(new Entry(item, weight, min, max, maxDurability, enchantLevels));
        }
    }

    private static final class Entry {
        private final Item item;
        private final int weight;
        private final int min;
        private final int max;
        /** ≥0 时剩余耐久比例在 [0, 该值] 间随机（原版 set_damage） */
        private final float maxDurability;
        /** >0 时按该等级随机附魔（含宝藏附魔） */
        private final int enchantLevels;

        Entry(Item item, int weight, int min, int max, float maxDurability, int enchantLevels) {
            this.item = item;
            this.weight = weight;
            this.min = min;
            this.max = max;
            this.maxDurability = maxDurability;
            this.enchantLevels = enchantLevels;
        }

        ItemStack create(Random rand) {
            ItemStack stack = new ItemStack(item, MathHelper.getInt(rand, min, max));
            if (maxDurability >= 0 && stack.isItemStackDamageable()) {
                float remaining = rand.nextFloat() * maxDurability;
                stack.setItemDamage(Math.min(stack.getMaxDamage() - 1, MathHelper.floor((1.0F - remaining) * stack.getMaxDamage())));
            }
            if (enchantLevels > 0) {
                stack = EnchantmentHelper.addRandomEnchantment(rand, stack, enchantLevels, true);
            }
            return stack;
        }
    }
}
