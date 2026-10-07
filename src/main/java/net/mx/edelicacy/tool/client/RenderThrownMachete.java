package net.mx.edelicacy.tool.client;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mx.edelicacy.registry.DelicacyItems;
import net.mx.edelicacy.tool.machete.ThrownEtheriumMachete;

/** 投出的以太砍刀：按飞行朝向画物品模型，刀尖朝前 */
@SideOnly(Side.CLIENT)
public class RenderThrownMachete extends Render<ThrownEtheriumMachete> {

    public RenderThrownMachete(RenderManager manager) {
        super(manager);
        this.shadowSize = 0.2F;
    }

    @Override
    public void doRender(ThrownEtheriumMachete entity, double x, double y, double z, float entityYaw, float partialTicks) {
        ItemStack stack = entity.getWeapon();
        if (stack.isEmpty()) {
            if (DelicacyItems.ETHERIUM_MACHETE == null) {
                return;
            }
            stack = new ItemStack(DelicacyItems.ETHERIUM_MACHETE);
        }
        float yaw = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks;
        float pitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y + 0.1D, z);
        GlStateManager.rotate(yaw - 90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(pitch, 0.0F, 0.0F, 1.0F);
        // 物品贴图的刀身沿对角线，转 -45° 让刀尖对准飞行方向
        GlStateManager.rotate(-45.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.scale(0.75F, 0.75F, 0.75F);
        GlStateManager.enableRescaleNormal();
        RenderHelper.enableStandardItemLighting();
        bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        Minecraft.getMinecraft().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.FIXED);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.popMatrix();
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(ThrownEtheriumMachete entity) {
        return TextureMap.LOCATION_BLOCKS_TEXTURE;
    }
}
