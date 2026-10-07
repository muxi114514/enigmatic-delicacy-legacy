package net.mx.edelicacy.proxy;

import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.mx.edelicacy.module.DelicacyModules;
import net.mx.edelicacy.module.IDelicacyModule;

/** 客户端代理：把客户端回调分发给各模块 */
public class ClientProxy extends CommonProxy {

    @Override
    public void preInit() {
        MinecraftForge.EVENT_BUS.register(this);
        for (IDelicacyModule module : DelicacyModules.ALL) {
            module.clientPreInit();
        }
    }

    @Override
    public void init() {
        for (IDelicacyModule module : DelicacyModules.ALL) {
            module.clientInit();
        }
    }

    @SubscribeEvent
    public void registerModels(ModelRegistryEvent event) {
        for (IDelicacyModule module : DelicacyModules.ALL) {
            module.registerModels();
        }
    }
}
