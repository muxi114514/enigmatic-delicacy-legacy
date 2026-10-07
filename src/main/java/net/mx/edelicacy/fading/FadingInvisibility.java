package net.mx.edelicacy.fading;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.mx.edelicacy.registry.DelicacyPotions;

/**
 * 失色期间保持隐身标记（影响生物索敌距离、粒子等）。
 * <p>原版 updatePotionMetadata 只按隐身药水设置隐身，由 MixinLivingFadingInvisible 在其中补上失色。
 */
public final class FadingInvisibility {

    private FadingInvisibility() {
    }

    public static boolean hasFading(EntityLivingBase entity) {
        Potion fading = DelicacyPotions.FADING;
        return fading != null && entity.isPotionActive(fading);
    }

    public static void apply(EntityLivingBase entity) {
        if (!entity.isInvisible() && hasFading(entity)) {
            entity.setInvisible(true);
        }
    }
}
