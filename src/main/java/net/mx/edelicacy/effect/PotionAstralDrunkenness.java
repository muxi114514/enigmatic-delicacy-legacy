package net.mx.edelicacy.effect;

import java.util.concurrent.ThreadLocalRandom;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.potion.PotionEffect;

/**
 * 星辉酩酊（中性）：每级攻击速度 +5%（乘基础值）。其余效果见 {@link net.mx.edelicacy.effect.handler.AstralDrunkennessHandler}。
 * <p>粒子颜色：1.12 的药水粒子取所有效果 {@link #getLiquidColor} 的混合色，这里每次求值返回随机色相，
 * 配合处理器定期刷新实体药水元数据，得到 1.21 的彩虹粒子。
 */
public class PotionAstralDrunkenness extends DelicacyPotion {

    public static final int MAX_AMPLIFIER = 4;
    private static final int BASE_COLOR = 0xFFFFFF;

    public PotionAstralDrunkenness() {
        super("astral_drunkenness", false, BASE_COLOR, 6, 3);
        registerPotionAttributeModifier(SharedMonsterAttributes.ATTACK_SPEED,
                "0f2b8a47-6a1e-4f43-9d0c-8e0e5c7a1d21", 0.05D, 1);
    }

    /** 服务端刷新药水元数据时也会调用，不能用客户端专有的 MathHelper.hsvToRGB */
    @Override
    public int getLiquidColor() {
        return rainbow(ThreadLocalRandom.current().nextFloat());
    }

    /** 背包效果列表按颜色排序，随机色会让顺序抖动，排序用固定色 */
    @Override
    public int getGuiSortColor(PotionEffect effect) {
        return BASE_COLOR;
    }

    /** 饱和度、亮度均为 1 的色相 [0, 1) → RGB */
    private static int rainbow(float hue) {
        float h = hue * 6.0F;
        int sector = (int) h % 6;
        int rise = (int) ((h - (int) h) * 255.0F);
        int fall = 255 - rise;
        switch (sector) {
            case 0: return 0xFF0000 | rise << 8;
            case 1: return fall << 16 | 0x00FF00;
            case 2: return 0x00FF00 | rise;
            case 3: return fall << 8 | 0x0000FF;
            case 4: return rise << 16 | 0x0000FF;
            default: return 0xFF0000 | fall;
        }
    }
}
