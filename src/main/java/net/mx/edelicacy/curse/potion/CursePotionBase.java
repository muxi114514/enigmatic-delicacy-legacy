package net.mx.edelicacy.curse.potion;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mx.edelicacy.EnigmaticDelicacy;
import net.mx.edelicacy.curse.client.CurseClient;

/**
 * 本线药水效果基类：图标取 textures/mob_effect/名字.png（18×18），翻译键 effect.enigmaticdelicacy.名字。
 * <p>可选「牛奶无效」：不给效果实例挂任何治愈物品。非负面效果即 1.21 的 NEUTRAL（不标记为有益）。
 */
public class CursePotionBase extends Potion {

    private final ResourceLocation icon;
    private final boolean milkCurable;

    protected CursePotionBase(String name, boolean bad, int color, boolean milkCurable) {
        super(bad, color);
        this.milkCurable = milkCurable;
        this.icon = new ResourceLocation(EnigmaticDelicacy.MODID, "textures/mob_effect/" + name + ".png");
        setRegistryName(EnigmaticDelicacy.MODID, name);
        setPotionName("effect." + EnigmaticDelicacy.MODID + "." + name);
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        return milkCurable ? super.getCurativeItems() : new ArrayList<>();
    }

    @Override
    public boolean hasStatusIcon() {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderInventoryEffect(int x, int y, PotionEffect effect, Minecraft mc) {
        CurseClient.drawPotionIcon(icon, x + 6, y + 7, 1.0F);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderHUDEffect(int x, int y, PotionEffect effect, Minecraft mc, float alpha) {
        CurseClient.drawPotionIcon(icon, x + 3, y + 3, alpha);
    }
}
