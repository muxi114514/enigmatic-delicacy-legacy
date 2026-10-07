package net.mx.edelicacy.effect;

import net.minecraft.client.Minecraft;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mx.edelicacy.EnigmaticDelicacy;
import net.mx.edelicacy.effect.client.EffectIconRenderer;

/**
 * 本模组药水基类：注册名 enigmaticdelicacy:名字，翻译键 effect.enigmaticdelicacy.名字（沿用 1.21 的语言键）。
 * <p>图标取 textures/mob_effect/名字.png（18×18，竖排多帧即动画），由客户端自绘，不占原版图标表。
 * 效果逻辑都在 handler 包的事件处理器里，药水类只负责外观与属性修饰符。
 */
public class DelicacyPotion extends Potion {

    private final ResourceLocation icon;
    private final int frames;
    private final int frameTime;

    public DelicacyPotion(String name, boolean bad, int color) {
        this(name, bad, color, 1, 1);
    }

    /**
     * @param frames    图标帧数（贴图高 = 18×帧数）
     * @param frameTime 每帧 tick 数（对应 1.21 的 .mcmeta frametime）
     */
    public DelicacyPotion(String name, boolean bad, int color, int frames, int frameTime) {
        super(bad, color);
        setRegistryName(EnigmaticDelicacy.MODID, name);
        setPotionName("effect." + EnigmaticDelicacy.MODID + "." + name);
        this.icon = new ResourceLocation(EnigmaticDelicacy.MODID, "textures/mob_effect/" + name + ".png");
        this.frames = Math.max(1, frames);
        this.frameTime = Math.max(1, frameTime);
    }

    public DelicacyPotion beneficial() {
        setBeneficial();
        return this;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderInventoryEffect(int x, int y, PotionEffect effect, Minecraft mc) {
        EffectIconRenderer.draw(mc, icon, frames, frameTime, x + 6, y + 7, 1.0F);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderHUDEffect(int x, int y, PotionEffect effect, Minecraft mc, float alpha) {
        EffectIconRenderer.draw(mc, icon, frames, frameTime, x + 3, y + 3, alpha);
    }
}
