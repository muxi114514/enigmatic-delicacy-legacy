package net.mx.edelicacy.fading;

import net.minecraft.client.gui.Gui;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mx.edelicacy.EnigmaticDelicacy;
import net.mx.edelicacy.fading.client.FadingClient;

/**
 * 失色（有益）：隐身（MixinLivingFadingInvisible）且客户端完全不渲染（FadingSync 同步）；
 * 免疫一次非自身造成的伤害，或让下一次近战增伤，触发后移除（FadingCombatEvents）。
 */
public class FadingPotion extends Potion {

    public FadingPotion() {
        super(false, 0xEEEEEE);
        setPotionName("effect." + EnigmaticDelicacy.MODID + ".fading");
        setRegistryName(EnigmaticDelicacy.MODID, "fading");
        setBeneficial();
    }

    /** 图标是单独的 18×18 贴图，不走原版 inventory.png 图集 */
    @Override
    @SideOnly(Side.CLIENT)
    public void renderInventoryEffect(PotionEffect effect, Gui gui, int x, int y, float z) {
        FadingClient.drawPotionIcon(x + 6, y + 7, 1.0F);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderHUDEffect(PotionEffect effect, Gui gui, int x, int y, float z, float alpha) {
        FadingClient.drawPotionIcon(x + 3, y + 3, alpha);
    }
}
