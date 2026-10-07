package net.mx.edelicacy.curse.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mx.edelicacy.EnigmaticDelicacy;
import net.mx.edelicacy.curse.CurseConfig;
import net.mx.edelicacy.data.DelicacyData;

/**
 * 深渊状态剩余时间条，画在经验条上（1.21 的 hud/abyss_bar_*）。
 * 服务端每秒同步一次剩余 tick，这里在两次同步之间按客户端时间递减插值，条形图仍然平滑。
 */
@SideOnly(Side.CLIENT)
public class AbyssHudRenderer {

    private static final ResourceLocation BACKGROUND = new ResourceLocation(EnigmaticDelicacy.MODID, "textures/gui/sprites/hud/abyss_bar_background.png");
    private static final ResourceLocation PROGRESS = new ResourceLocation(EnigmaticDelicacy.MODID, "textures/gui/sprites/hud/abyss_bar_progress.png");
    private static final int WIDTH = 182;
    private static final int HEIGHT = 3;

    private int lastValue;
    private long lastChangeTime;

    @SubscribeEvent(receiveCanceled = true)
    public void onOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.EXPERIENCE) {
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.player;
        if (player == null || mc.world == null || player.isCreative() || player.isSpectator()) {
            return;
        }
        int tick = DelicacyData.get(player).getAbyssStewTick();
        long now = mc.world.getTotalWorldTime();
        if (tick != lastValue) {
            lastValue = tick;
            lastChangeTime = now;
        }
        if (tick <= 0) {
            return;
        }
        float elapsed = now - lastChangeTime + event.getPartialTicks();
        float display = Math.max(tick - 20, tick - elapsed);
        float percent = Math.min(1.0F, display / Math.max(1, CurseConfig.abyssalStewDuration * 20));
        if (percent <= 0.0F) {
            return;
        }
        ScaledResolution resolution = event.getResolution();
        int x = resolution.getScaledWidth() / 2 - 91;
        int y = resolution.getScaledHeight() - 32 + 3 + 4;
        int length = (int) (percent * WIDTH);
        GlStateManager.enableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(BACKGROUND);
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, WIDTH, HEIGHT, WIDTH, HEIGHT);
        if (length > 0) {
            mc.getTextureManager().bindTexture(PROGRESS);
            Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, length, HEIGHT, WIDTH, HEIGHT);
        }
        GlStateManager.disableBlend();
        mc.getTextureManager().bindTexture(Gui.ICONS);
    }
}
