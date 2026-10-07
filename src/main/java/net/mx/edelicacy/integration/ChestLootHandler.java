package net.mx.edelicacy.integration;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.LootEntryEmpty;
import net.minecraft.world.storage.loot.LootEntryItem;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.mx.edelicacy.registry.DelicacyItems;

/**
 * 往主世界宝箱注入神秘种子（对应 1.21 的 overworld_simple_addon 战利品修改器，默认约 16%）。
 * 1.12 没有沉船；掠夺者前哨站来自 Raids Backport。
 */
public class ChestLootHandler {

    private static final Set<String> TABLES = new HashSet<>(Arrays.asList(
            "minecraft:chests/simple_dungeon", "minecraft:chests/abandoned_mineshaft",
            "minecraft:chests/stronghold_corridor", "minecraft:chests/stronghold_crossing",
            "minecraft:chests/desert_pyramid", "minecraft:chests/jungle_temple",
            "minecraft:chests/woodland_mansion", "raids:chests/pillager_outpost"));

    static int seedWeight = 16;
    static int emptyWeight = 84;

    @SubscribeEvent
    public void onLootTableLoad(LootTableLoadEvent event) {
        if (seedWeight <= 0 || DelicacyItems.ENIGMATIC_SEED == null || !TABLES.contains(event.getName().toString())) {
            return;
        }
        LootPool pool = new LootPool(new LootEntry[]{
                new LootEntryItem(DelicacyItems.ENIGMATIC_SEED, seedWeight, 0, new LootFunction[0], new LootCondition[0],
                        "enigmaticdelicacy:enigmatic_seed"),
                new LootEntryEmpty(emptyWeight, 0, new LootCondition[0], "enigmaticdelicacy:empty")
        }, new LootCondition[0], new RandomValueRange(1), new RandomValueRange(0), "enigmaticdelicacy_seed");
        event.getTable().addPool(pool);
    }
}
