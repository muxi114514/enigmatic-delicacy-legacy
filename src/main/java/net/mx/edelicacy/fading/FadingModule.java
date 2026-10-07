package net.mx.edelicacy.fading;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.registries.IForgeRegistry;
import net.mx.edelicacy.fading.client.FadingClient;
import net.mx.edelicacy.module.IDelicacyModule;
import net.mx.edelicacy.network.DelicacyNetwork;

/** 失色模块：失色效果、失色卷轴、实体隐藏同步（网络包 FadingSyncMessage） */
public class FadingModule implements IDelicacyModule {

    private final List<Item> items = new ArrayList<>();

    @Override
    public void preInit(Configuration config) {
        FadingConfig.load(config);
        DelicacyNetwork.register(FadingSyncMessage.Handler.class, FadingSyncMessage.class, Side.CLIENT);
        MinecraftForge.EVENT_BUS.register(new FadingSync());
        MinecraftForge.EVENT_BUS.register(new FadingCombatEvents());
    }

    @Override
    public void registerPotions(IForgeRegistry<Potion> registry) {
        registry.register(new FadingPotion());
    }

    @Override
    public void registerItems(IForgeRegistry<Item> registry) {
        Item scroll = new FadingScrollItem();
        registry.register(scroll);
        items.add(scroll);
    }

    @Override
    public void registerModels() {
        FadingClient.registerModels(items);
    }

    @Override
    public void clientPreInit() {
        FadingClient.init();
    }
}
