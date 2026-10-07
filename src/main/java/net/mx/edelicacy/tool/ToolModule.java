package net.mx.edelicacy.tool;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistry;
import net.mx.edelicacy.EnigmaticDelicacy;
import net.mx.edelicacy.tool.client.ToolClient;
import net.mx.edelicacy.tool.fishing.EntityInfernalHook;
import net.mx.edelicacy.tool.fishing.ItemInfernalFishingRod;
import net.mx.edelicacy.tool.machete.ItemEtheriumMachete;
import net.mx.edelicacy.tool.machete.MacheteCakeHandler;
import net.mx.edelicacy.tool.machete.ThrownEtheriumMachete;
import net.mx.edelicacy.module.IDelicacyModule;

/** 工具模块：以太砍刀（可投掷的农夫乐事刀具）与熔岩钓竿 */
public final class ToolModule implements IDelicacyModule {

    private static final int ID_MACHETE = 1;
    private static final int ID_HOOK = 3;

    private Item machete;
    private Item rod;

    @Override
    public void preInit(Configuration config) {
        ToolConfig.load(config);
        MinecraftForge.EVENT_BUS.register(new MacheteCakeHandler());
    }

    @Override
    public void registerItems(IForgeRegistry<Item> registry) {
        machete = new ItemEtheriumMachete();
        rod = new ItemInfernalFishingRod();
        registry.register(machete);
        registry.register(rod);
    }

    @Override
    public void registerEntities(IForgeRegistry<EntityEntry> registry) {
        registry.register(EntityEntryBuilder.create()
                .entity(ThrownEtheriumMachete.class)
                .id(new ResourceLocation(EnigmaticDelicacy.MODID, "etherium_machete"), ID_MACHETE)
                .name(EnigmaticDelicacy.MODID + ".etherium_machete")
                .tracker(64, 5, true)
                .build());
        registry.register(EntityEntryBuilder.create()
                .entity(EntityInfernalHook.class)
                .id(new ResourceLocation(EnigmaticDelicacy.MODID, "infernal_fishing_bobber"), ID_HOOK)
                .name(EnigmaticDelicacy.MODID + ".infernal_fishing_bobber")
                .tracker(64, 5, true)
                .build());
    }

    @Override
    public void init() {
        OreDictionary.registerOre("toolKnife", new ItemStack(machete, 1, OreDictionary.WILDCARD_VALUE));
    }

    @Override
    public void registerModels() {
        ToolClient.registerModels(machete, rod);
    }

    @Override
    public void clientPreInit() {
        ToolClient.preInit();
    }
}
