package net.mx.edelicacy.effect.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** 药水图标绘制：GUI 贴图不走图集，.mcmeta 动画不生效，按客户端时间手动取帧 */
@SideOnly(Side.CLIENT)
public final class EffectIconRenderer {

    private static final int SIZE = 18;

    private EffectIconRenderer() {
    }

    public static void draw(Minecraft mc, ResourceLocation icon, int frames, int frameTime, int x, int y, float alpha) {
        int frame = 0;
        if (frames > 1) {
            // 50 ms 一个 tick，与 .mcmeta 的 frametime 对齐
            frame = (int) ((Minecraft.getSystemTime() / 50L / frameTime) % frames);
        }
        mc.getTextureManager().bindTexture(icon);
        GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0, frame * SIZE, SIZE, SIZE, SIZE, SIZE * frames);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
