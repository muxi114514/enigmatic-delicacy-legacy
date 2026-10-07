package net.mx.edelicacy.tool.client;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mx.edelicacy.tool.fishing.EntityInfernalHook;
import net.mx.edelicacy.tool.machete.ThrownEtheriumMachete;

/** 工具模块的客户端注册：物品模型与实体渲染 */
@SideOnly(Side.CLIENT)
public final class ToolClient {

    private ToolClient() {
    }

    public static void registerModels(Item... items) {
        for (Item item : items) {
            if (item != null) {
                ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
            }
        }
    }

    public static void preInit() {
        RenderingRegistry.registerEntityRenderingHandler(ThrownEtheriumMachete.class, RenderThrownMachete::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityInfernalHook.class, RenderInfernalHook::new);
    }
}
