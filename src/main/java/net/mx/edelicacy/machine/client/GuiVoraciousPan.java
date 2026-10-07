package net.mx.edelicacy.machine.client;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mx.edelicacy.EnigmaticDelicacy;
import net.mx.edelicacy.machine.pan.ContainerVoraciousPan;
import net.mx.edelicacy.machine.pan.TileVoraciousPan;

/** 饕餮之锅禁忌转化界面：禁忌值条（满 50 点为满格）、有产物时显示转化箭头、输入槽外框画在物品之上 */
@SideOnly(Side.CLIENT)
public class GuiVoraciousPan extends GuiContainer {

    private static final ResourceLocation TEXTURE = new ResourceLocation(EnigmaticDelicacy.MODID, "textures/gui/voracious_pan.png");

    private final InventoryPlayer playerInv;
    private final ContainerVoraciousPan container;

    public GuiVoraciousPan(InventoryPlayer playerInv, TileVoraciousPan pan) {
        this(playerInv, new ContainerVoraciousPan(playerInv, pan));
    }

    private GuiVoraciousPan(InventoryPlayer playerInv, ContainerVoraciousPan container) {
        super(container);
        this.playerInv = playerInv;
        this.container = container;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        renderHoveredToolTip(mouseX, mouseY);
        if (mc.player.inventory.getItemStack().isEmpty() && isPointInRegion(8, 28, 16, 4, mouseX, mouseY)) {
            int points = container.getClientPoints();
            String number = (points > 0 ? TextFormatting.GOLD : TextFormatting.RED) + String.valueOf(points) + TextFormatting.LIGHT_PURPLE;
            drawHoveringText(TextFormatting.LIGHT_PURPLE + I18n.format("gui.enigmaticdelicacy.voracious_pan.forbidden_point", number), mouseX, mouseY);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRenderer.drawString(playerInv.getDisplayName().getUnformattedText(), 8, ySize - 96 + 2, 0x404040);
        // 输入槽外框盖在槽内物品上（槽内物品约 z=200，手持物品约 z=300）
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0F, 0.0F, 250.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        mc.getTextureManager().bindTexture(TEXTURE);
        drawTexturedModalRect(41, 37, 176, 21, 22, 22);
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(TEXTURE);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
        if (!container.getSlot(ContainerVoraciousPan.SLOT_RESULT).getStack().isEmpty()) {
            drawTexturedModalRect(guiLeft + 67, guiTop + 42, 176, 0, 42, 12);
        }
        int width = (int) Math.min(16L, (long) container.getClientPoints() * 16 / 50);
        if (width > 0) {
            drawTexturedModalRect(guiLeft + 8, guiTop + 28, 176, 16, width - 1, 4);
            drawTexturedModalRect(guiLeft + 7 + width, guiTop + 28, 191, 16, 1, 4);
        }
    }
}
