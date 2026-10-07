package net.mx.edelicacy.machine.client;

import com.wdcftgg.farmersdelightlegacy.common.block.BlockStove;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mx.edelicacy.machine.stove.TileEtheriumStove;

/** 以太炉灶烤架上 6 个物品的渲染（与农夫乐事炉灶相同的摆位） */
@SideOnly(Side.CLIENT)
public class RenderEtheriumStove extends TileEntitySpecialRenderer<TileEtheriumStove> {

    private static final float[][] OFFSETS = {
            {0.3F, 0.2F}, {0.0F, 0.2F}, {-0.3F, 0.2F},
            {0.3F, -0.2F}, {0.0F, -0.2F}, {-0.3F, -0.2F}
    };

    @Override
    public void render(TileEtheriumStove te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (te.getWorld() == null) {
            return;
        }
        IBlockState state = te.getWorld().getBlockState(te.getPos());
        EnumFacing facing = state.getBlock() instanceof BlockStove ? state.getValue(BlockStove.FACING).getOpposite() : EnumFacing.NORTH;
        int light = te.getWorld().getCombinedLight(te.getPos().up(), 0);
        for (int i = 0; i < TileEtheriumStove.GRILL_SLOTS; i++) {
            ItemStack stack = te.getGrillStack(i);
            if (stack.isEmpty()) {
                continue;
            }
            GlStateManager.pushMatrix();
            GlStateManager.translate(x + 0.5D, y + 1.02D, z + 0.5D);
            GlStateManager.rotate(-facing.getHorizontalAngle(), 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.translate(OFFSETS[i][0], OFFSETS[i][1], 0.0D);
            GlStateManager.scale(0.375F, 0.375F, 0.375F);
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, light & 65535, light >> 16);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableRescaleNormal();
            RenderHelper.enableStandardItemLighting();
            Minecraft.getMinecraft().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.FIXED);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableRescaleNormal();
            GlStateManager.popMatrix();
        }
    }
}
