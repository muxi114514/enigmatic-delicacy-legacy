package net.mx.edelicacy.flora;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.registries.IForgeRegistry;
import net.mx.edelicacy.flora.client.FloraClient;
import net.mx.edelicacy.flora.event.AxeStrippingHandler;
import net.mx.edelicacy.flora.event.ColonyPlacementHandler;
import net.mx.edelicacy.flora.event.FloraFuelHandler;
import net.mx.edelicacy.flora.event.FlowerPotHandler;
import net.mx.edelicacy.flora.event.InfinisoilHarvestHandler;
import net.mx.edelicacy.flora.soil.ColonyConversion;
import net.mx.edelicacy.module.IDelicacyModule;

/**
 * 植物与土壤模块：神秘灌木与神秘果、无尽沃土、星辰木全套、星辰树与星辰果、褪隐玻璃。
 */
public class FloraModule implements IDelicacyModule {

    private final FloraBlockSet blocks = new FloraBlockSet();
    private final FloraItemSet items = new FloraItemSet();

    @Override
    public void preInit(Configuration config) {
        FloraConfig.load(config);
        MinecraftForge.EVENT_BUS.register(new AxeStrippingHandler());
        MinecraftForge.EVENT_BUS.register(new FlowerPotHandler());
        MinecraftForge.EVENT_BUS.register(new ColonyPlacementHandler());
        MinecraftForge.EVENT_BUS.register(new InfinisoilHarvestHandler());
        MinecraftForge.EVENT_BUS.register(new FloraFuelHandler());
    }

    @Override
    public void init() {
        FloraOreDict.register();
        FloraRecipes.register();
    }

    /** 其他模组的方块此时都已注册，解析菌落转换表 */
    @Override
    public void postInit() {
        ColonyConversion.resolve(FloraConfig.convertList());
    }

    @Override
    public void registerBlocks(IForgeRegistry<Block> registry) {
        blocks.create();
        for (Block block : blocks.all()) {
            registry.register(block);
        }
    }

    @Override
    public void registerItems(IForgeRegistry<Item> registry) {
        items.create(blocks);
        for (Item item : items.all()) {
            registry.register(item);
        }
    }

    @Override
    public void registerModels() {
        FloraClient.registerModels(items.all());
    }

    @Override
    public void clientPreInit() {
        FloraClient.preInit();
    }
}
