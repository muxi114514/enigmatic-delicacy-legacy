package net.mx.edelicacy.machine.client;

import java.util.Arrays;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mx.edelicacy.EnigmaticDelicacy;
import net.mx.edelicacy.machine.stove.ContainerEtheriumStove;
import net.mx.edelicacy.machine.stove.TileEtheriumStove;

/** 以太炉灶熔炉界面（去掉了 1.21 的配方书），悬停燃料图标显示「永久燃烧」说明 */
@SideOnly(Side.CLIENT)
public class GuiEtheriumStove extends GuiContainer {

    private static final ResourceLocation TEXTURE = new ResourceLocation(EnigmaticDelicacy.MODID, "textures/gui/etherium_stove.png");

    private final InventoryPlayer playerInv;
    private final ContainerEtheriumStove container;

    public GuiEtheriumStove(InventoryPlayer playerInv, TileEtheriumStove stove) {
        this(playerInv, new ContainerEtheriumStove(playerInv, stove));
    }

    private GuiEtheriumStove(InventoryPlayer playerInv, ContainerEtheriumStove container) {
        super(container);
        this.playerInv = playerInv;
        this.container = container;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        renderHoveredToolTip(mouseX, mouseY);
        if (mc.player.inventory.getItemStack().isEmpty() && isPointInRegion(56, 53, 16, 16, mouseX, mouseY)) {
            drawHoveringText(Arrays.asList(I18n.format("tooltip.enigmaticdelicacy.etheriumStoveFuel1"),
                    I18n.format("tooltip.enigmaticdelicacy.etheriumStoveFuel2")), mouseX, mouseY);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String title = I18n.format("gui.enigmaticdelicacy.etherium_stove");
        fontRenderer.drawString(title, xSize / 2 - fontRenderer.getStringWidth(title) / 2, 6, 0x404040);
        fontRenderer.drawString(playerInv.getDisplayName().getUnformattedText(), 8, ySize - 96 + 2, 0x404040);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(TEXTURE);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
        int progress = container.getCookProgressScaled(24);
        if (progress > 0) {
            drawTexturedModalRect(guiLeft + 79, guiTop + 34, 176, 0, progress + 1, 17);
        }
    }
}
