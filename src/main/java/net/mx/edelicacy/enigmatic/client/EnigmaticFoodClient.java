package net.mx.edelicacy.enigmatic.client;

import java.util.List;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** 客户端：谜之食物的物品模型 */
@SideOnly(Side.CLIENT)
public final class EnigmaticFoodClient {

    private EnigmaticFoodClient() {
    }

    public static void registerModels(List<Item> items) {
        for (Item item : items) {
            ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
        }
    }
}
