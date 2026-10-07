package net.mx.edelicacy.charm.client;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** 客户端：护符模块的物品模型与提示里要用的本地玩家 */
@SideOnly(Side.CLIENT)
public final class CharmClient {

    private CharmClient() {
    }

    public static void registerModels(List<Item> items) {
        for (Item item : items) {
            ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
        }
    }

    public static EntityPlayer localPlayer() {
        return Minecraft.getMinecraft().player;
    }
}
