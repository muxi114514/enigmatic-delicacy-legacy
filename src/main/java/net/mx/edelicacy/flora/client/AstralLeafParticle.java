package net.mx.edelicacy.flora.client;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * 飘落的星辰叶片粒子（1.21 AstralLeafParticle）：重力 0.1、阻力 0.95、寿命 100~125 tick，
 * 贴图取自方块图集里拼好的 particle/*_astral_leaf_N。
 */
@SideOnly(Side.CLIENT)
public class AstralLeafParticle extends Particle {

    private static final double FRICTION = 0.95D;

    public AstralLeafParticle(World world, double x, double y, double z, double vx, double vy, double vz, TextureAtlasSprite sprite) {
        super(world, x, y, z);
        motionX = vx;
        motionY = vy;
        motionZ = vz;
        setSize(0.06F, 0.06F);
        particleGravity = 0.1F;
        particleMaxAge = (int) (25.0D * (Math.random() + 4.0D));
        particleScale *= 0.8F;
        setParticleTexture(sprite);
    }

    /** 1 = 方块图集层 */
    @Override
    public int getFXLayer() {
        return 1;
    }

    /** 与原版相同，只把空气阻力从 0.98 改为 1.21 的 0.95 */
    @Override
    public void onUpdate() {
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;
        if (particleAge++ >= particleMaxAge) {
            setExpired();
            return;
        }
        motionY -= 0.04D * particleGravity;
        move(motionX, motionY, motionZ);
        motionX *= FRICTION;
        motionY *= FRICTION;
        motionZ *= FRICTION;
        if (onGround) {
            motionX *= 0.7D;
            motionZ *= 0.7D;
        }
    }
}
