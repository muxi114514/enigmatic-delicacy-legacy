package net.mx.edelicacy.machine.client;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mx.edelicacy.gui.DelicacyGuiHandler;
import net.mx.edelicacy.machine.pan.TileVoraciousPan;
import net.mx.edelicacy.machine.stove.TileEtheriumStove;

/** 机器模块的客户端注册：物品模型、烤架渲染、界面、锅的提示 */
@SideOnly(Side.CLIENT)
public final class MachineClient {

    private MachineClient() {
    }

    public static void registerModels(Item stoveItem) {
        if (stoveItem != null) {
            ModelLoader.setCustomModelResourceLocation(stoveItem, 0, new ModelResourceLocation(stoveItem.getRegistryName(), "inventory"));
        }
    }

    public static void preInit() {
        ClientRegistry.bindTileEntitySpecialRenderer(TileEtheriumStove.class, new RenderEtheriumStove());
        DelicacyGuiHandler.registerScreen(DelicacyGuiHandler.ETHERIUM_STOVE, (player, world, pos) -> {
            TileEntity tile = world.getTileEntity(pos);
            return tile instanceof TileEtheriumStove ? new GuiEtheriumStove(player.inventory, (TileEtheriumStove) tile) : null;
        });
        DelicacyGuiHandler.registerScreen(DelicacyGuiHandler.VORACIOUS_PAN, (player, world, pos) -> {
            TileEntity tile = world.getTileEntity(pos);
            return tile instanceof TileVoraciousPan ? new GuiVoraciousPan(player.inventory, (TileVoraciousPan) tile) : null;
        });
        MinecraftForge.EVENT_BUS.register(new PanTooltipHandler());
    }
}
