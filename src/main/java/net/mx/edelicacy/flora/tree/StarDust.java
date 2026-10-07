package net.mx.edelicacy.flora.tree;

import java.util.Random;

import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

/**
 * 星尘粒子替代：1.12 的神秘遗物没有 EL+ 的 star dust 粒子，用带颜色的药水漩涡粒子模拟（淡蓝/粉/紫三色）。
 * 只在客户端的 randomDisplayTick 里调用。
 */
final class StarDust {

    private static final double[][] COLORS = {
            {0.56D, 0.84D, 1.0D},
            {1.0D, 0.62D, 0.88D},
            {0.71D, 0.55D, 1.0D}
    };

    private StarDust() {
    }

    static void spawn(World world, Random rand, double x, double y, double z) {
        double[] color = COLORS[rand.nextInt(COLORS.length)];
        world.spawnParticle(EnumParticleTypes.SPELL_MOB, x, y, z, color[0], color[1], color[2]);
    }
}
