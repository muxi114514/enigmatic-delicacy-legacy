package net.mx.edelicacy;

import java.io.File;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.potion.Potion;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.ObjectHolderRegistry;
import net.mx.edelicacy.compat.ModCompat;
import net.mx.edelicacy.data.DelicacyDataModule;
import net.mx.edelicacy.gui.DelicacyGuiHandler;
import net.mx.edelicacy.module.DelicacyModules;
import net.mx.edelicacy.module.IDelicacyModule;
import net.mx.edelicacy.proxy.CommonProxy;

/**
 * 神秘佳肴 1.12.2 移植主类：只负责把各生命周期与注册事件分发给功能模块（见 {@link DelicacyModules}）。
 * 新功能新增模块即可，不往这里堆注册代码。
 */
@Mod(modid = EnigmaticDelicacy.MODID, name = EnigmaticDelicacy.NAME, version = EnigmaticDelicacy.VERSION,
        dependencies = "required-after:fermiumbooter;required-after:baubles;required-after:enigmaticlegacy;"
                + "required-after:eaddons;required-after:farmersdelight;after:jei;after:crafttweaker;"
                + "after:simpledifficulty;after:nethers_delight_legacy;after:waila;after:xat;after:unlockablebauble")
public class EnigmaticDelicacy {

    public static final String MODID = "enigmaticdelicacy";
    public static final String NAME = "Enigmatic Delicacy";
    public static final String VERSION = "1.0.0";

    @SidedProxy(clientSide = "net.mx.edelicacy.proxy.ClientProxy", serverSide = "net.mx.edelicacy.proxy.CommonProxy")
    public static CommonProxy proxy;

    @Mod.Instance(MODID)
    public static EnigmaticDelicacy instance;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ModCompat.init();
        MinecraftForge.EVENT_BUS.register(this);
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new DelicacyGuiHandler());

        Configuration config = new Configuration(new File(event.getModConfigurationDirectory(), MODID + ".cfg"));
        config.load();
        // 玩家数据先于功能模块装配，网络包编号固定在最前
        DelicacyDataModule.preInit();
        for (IDelicacyModule module : DelicacyModules.ALL) {
            module.preInit(config);
        }
        if (config.hasChanged()) {
            config.save();
        }
        proxy.preInit();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        for (IDelicacyModule module : DelicacyModules.ALL) {
            module.init();
        }
        proxy.init();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        for (IDelicacyModule module : DelicacyModules.ALL) {
            module.postInit();
        }
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        for (IDelicacyModule module : DelicacyModules.ALL) {
            module.serverStarting(event);
        }
    }

    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        for (IDelicacyModule module : DelicacyModules.ALL) {
            module.registerBlocks(event.getRegistry());
        }
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        for (IDelicacyModule module : DelicacyModules.ALL) {
            module.registerItems(event.getRegistry());
        }
    }

    @SubscribeEvent
    public void registerPotions(RegistryEvent.Register<Potion> event) {
        for (IDelicacyModule module : DelicacyModules.ALL) {
            module.registerPotions(event.getRegistry());
        }
    }

    /**
     * 附魔推迟到 loadComplete 注册：1.12 新存档按注册顺序分配附魔数字 ID，在注册事件里注册会把其后加载的
     * 模组附魔整体挤后一格，整合包按数字 ID 写的附魔书 / 交易就会错位。loadComplete 晚于所有 postInit、
     * 早于注册表冻结，拿到的是最后的空号；注册后补一次 @ObjectHolder 注入。
     */
    @Mod.EventHandler
    public void loadComplete(FMLLoadCompleteEvent event) {
        for (IDelicacyModule module : DelicacyModules.ALL) {
            module.registerEnchantments(ForgeRegistries.ENCHANTMENTS);
        }
        ObjectHolderRegistry.INSTANCE.applyObjectHolders();
    }

    @SubscribeEvent
    public void registerEntities(RegistryEvent.Register<EntityEntry> event) {
        for (IDelicacyModule module : DelicacyModules.ALL) {
            module.registerEntities(event.getRegistry());
        }
    }

    /** 默认优先级：早于 CraftTweaker（LOWEST），脚本可以删改这里注册的配方 */
    @SubscribeEvent
    public void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        for (IDelicacyModule module : DelicacyModules.ALL) {
            module.registerRecipes(event.getRegistry());
        }
    }
}
