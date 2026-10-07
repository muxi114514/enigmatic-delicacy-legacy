package net.mx.edelicacy.food;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDispenser;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.registries.IForgeRegistry;
import net.mx.edelicacy.EnigmaticDelicacy;
import net.mx.edelicacy.food.block.BlockSparklingPastryPlate;
import net.mx.edelicacy.food.block.BlockStickyPie;
import net.mx.edelicacy.food.block.RarityBlockItem;
import net.mx.edelicacy.food.client.FoodClient;
import net.mx.edelicacy.food.entity.EntityThrownBlazingMeatRoll;
import net.mx.edelicacy.food.handler.EtheriumSteakHandler;
import net.mx.edelicacy.food.item.InfinifeedDispenseBehavior;
import net.mx.edelicacy.module.IDelicacyModule;
import net.mx.edelicacy.registry.DelicacyItems;

/**
 * 日常食物模块：谜之果酱/奶油、星辉米饭、星叶粽、星光沙拉、灵液根汤、天体果切片、永恒蛋糕切片、以太牛排、
 * 炽焰肉卷（含投掷实体）、野性滋养精华，一盘闪冰酥与黏糯谜之派两个方块，以及谜之果酿隐身药水。
 */
public class FoodModule implements IDelicacyModule {

    /** 预分配的实体网络编号 */
    private static final int BLAZING_MEAT_ROLL_ENTITY_ID = 2;

    private final List<Block> blocks = new ArrayList<>();
    private final List<Item> items = new ArrayList<>();

    @Override
    public void preInit(Configuration config) {
        MinecraftForge.EVENT_BUS.register(new EtheriumSteakHandler());
        EtheriumSteakHandler.registerAttributeSource();
    }

    @Override
    public void init() {
        // 神秘果酿隐身药水由 flora 模块用原版 PotionHelper 登记（普通/喷溅/滞留都支持）
        Item infinifeed = DelicacyItems.INFINIFEED;
        if (infinifeed != null) {
            BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(infinifeed, new InfinifeedDispenseBehavior());
        }
    }

    @Override
    public void registerBlocks(IForgeRegistry<Block> registry) {
        blocks.add(new BlockSparklingPastryPlate());
        blocks.add(new BlockStickyPie());
        for (Block block : blocks) {
            registry.register(block);
        }
    }

    @Override
    public void registerItems(IForgeRegistry<Item> registry) {
        items.addAll(FoodItems.create());
        for (Block block : blocks) {
            items.add(new RarityBlockItem(block, EnumRarity.UNCOMMON));
        }
        for (Item item : items) {
            registry.register(item);
        }
    }

    @Override
    public void registerEntities(IForgeRegistry<EntityEntry> registry) {
        String name = "blazing_meat_roll";
        registry.register(EntityEntryBuilder.create()
                .entity(EntityThrownBlazingMeatRoll.class)
                .id(new ResourceLocation(EnigmaticDelicacy.MODID, name), BLAZING_MEAT_ROLL_ENTITY_ID)
                .name(EnigmaticDelicacy.MODID + "." + name)
                .tracker(64, 10, true)
                .build());
    }

    @Override
    public void registerModels() {
        FoodClient.registerModels(items);
    }

    @Override
    public void clientPreInit() {
        FoodClient.preInit();
    }
}
