package net.mx.edelicacy.tool.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mx.edelicacy.EnigmaticDelicacy;
import net.mx.edelicacy.tool.fishing.EntityInfernalHook;
import net.mx.edelicacy.tool.fishing.ItemInfernalFishingRod;

/** 熔岩浮漂与金色钓线，照原版 RenderFish 改写 */
@SideOnly(Side.CLIENT)
public class RenderInfernalHook extends Render<EntityInfernalHook> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(EnigmaticDelicacy.MODID, "textures/entity/infernal_fishing_hook.png");
    /** 钓线颜色 0xE9B115 */
    private static final int LINE_R = 0xE9;
    private static final int LINE_G = 0xB1;
    private static final int LINE_B = 0x15;

    public RenderInfernalHook(RenderManager manager) {
        super(manager);
    }

    @Override
    public void doRender(EntityInfernalHook hook, double x, double y, double z, float entityYaw, float partialTicks) {
        EntityPlayer angler = hook.getAngler();
        if (angler == null || renderOutlines) {
            return;
        }
        drawBobber(hook, x, y, z);
        drawLine(hook, angler, x, y, z, partialTicks);
        super.doRender(hook, x, y, z, entityYaw, partialTicks);
    }

    private void drawBobber(EntityInfernalHook hook, double x, double y, double z) {
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) y, (float) z);
        GlStateManager.enableRescaleNormal();
        GlStateManager.scale(0.5F, 0.5F, 0.5F);
        bindEntityTexture(hook);
        GlStateManager.rotate(180.0F - renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate((renderManager.options.thirdPersonView == 2 ? -1 : 1) * -renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX_NORMAL);
        buffer.pos(-0.5D, -0.5D, 0.0D).tex(0.0D, 1.0D).normal(0.0F, 1.0F, 0.0F).endVertex();
        buffer.pos(0.5D, -0.5D, 0.0D).tex(1.0D, 1.0D).normal(0.0F, 1.0F, 0.0F).endVertex();
        buffer.pos(0.5D, 0.5D, 0.0D).tex(1.0D, 0.0D).normal(0.0F, 1.0F, 0.0F).endVertex();
        buffer.pos(-0.5D, 0.5D, 0.0D).tex(0.0D, 0.0D).normal(0.0F, 1.0F, 0.0F).endVertex();
        tessellator.draw();
        GlStateManager.disableRescaleNormal();
        GlStateManager.popMatrix();
    }

    private void drawLine(EntityInfernalHook hook, EntityPlayer angler, double x, double y, double z, float partialTicks) {
        int side = angler.getPrimaryHand() == EnumHandSide.RIGHT ? 1 : -1;
        if (!(angler.getHeldItemMainhand().getItem() instanceof ItemInfernalFishingRod)) {
            side = -side;
        }
        float swing = MathHelper.sin(MathHelper.sqrt(angler.getSwingProgress(partialTicks)) * (float) Math.PI);
        float bodyYaw = (angler.prevRenderYawOffset + (angler.renderYawOffset - angler.prevRenderYawOffset) * partialTicks) * 0.017453292F;
        double sin = MathHelper.sin(bodyYaw);
        double cos = MathHelper.cos(bodyYaw);
        double handOffset = side * 0.35D;
        double handX;
        double handY;
        double handZ;
        double eyeOffset;
        if ((renderManager.options == null || renderManager.options.thirdPersonView <= 0) && angler == Minecraft.getMinecraft().player) {
            float fov = renderManager.options.fovSetting / 100.0F;
            Vec3d vec = new Vec3d(side * -0.36D * fov, -0.045D * fov, 0.4D);
            vec = vec.rotatePitch(-(angler.prevRotationPitch + (angler.rotationPitch - angler.prevRotationPitch) * partialTicks) * 0.017453292F);
            vec = vec.rotateYaw(-(angler.prevRotationYaw + (angler.rotationYaw - angler.prevRotationYaw) * partialTicks) * 0.017453292F);
            vec = vec.rotateYaw(swing * 0.5F);
            vec = vec.rotatePitch(-swing * 0.7F);
            handX = angler.prevPosX + (angler.posX - angler.prevPosX) * partialTicks + vec.x;
            handY = angler.prevPosY + (angler.posY - angler.prevPosY) * partialTicks + vec.y;
            handZ = angler.prevPosZ + (angler.posZ - angler.prevPosZ) * partialTicks + vec.z;
            eyeOffset = angler.getEyeHeight();
        } else {
            handX = angler.prevPosX + (angler.posX - angler.prevPosX) * partialTicks - cos * handOffset - sin * 0.8D;
            handY = angler.prevPosY + angler.getEyeHeight() + (angler.posY - angler.prevPosY) * partialTicks - 0.45D;
            handZ = angler.prevPosZ + (angler.posZ - angler.prevPosZ) * partialTicks - sin * handOffset + cos * 0.8D;
            eyeOffset = angler.isSneaking() ? -0.1875D : 0.0D;
        }
        double hookX = hook.prevPosX + (hook.posX - hook.prevPosX) * partialTicks;
        double hookY = hook.prevPosY + (hook.posY - hook.prevPosY) * partialTicks + 0.25D;
        double hookZ = hook.prevPosZ + (hook.posZ - hook.prevPosZ) * partialTicks;
        double dx = (float) (handX - hookX);
        double dy = (float) (handY - hookY) + eyeOffset;
        double dz = (float) (handZ - hookZ);
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(3, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i <= 16; i++) {
            float t = i / 16.0F;
            buffer.pos(x + dx * t, y + dy * (t * t + t) * 0.5D + 0.25D, z + dz * t).color(LINE_R, LINE_G, LINE_B, 255).endVertex();
        }
        tessellator.draw();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityInfernalHook entity) {
        return TEXTURE;
    }
}
