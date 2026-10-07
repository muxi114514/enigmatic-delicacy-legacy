package net.mx.edelicacy.effect;

import java.util.concurrent.ThreadLocalRandom;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.MathHelper;

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

    @Override
    public int getLiquidColor() {
        return MathHelper.hsvToRGB(ThreadLocalRandom.current().nextFloat(), 1.0F, 1.0F);
    }

    /** 背包效果列表按颜色排序，随机色会让顺序抖动，排序用固定色 */
    @Override
    public int getGuiSortColor(PotionEffect effect) {
        return BASE_COLOR;
    }
}
