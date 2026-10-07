package net.mx.edelicacy.food.client;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** 1.21 FD 的闪光粒子：缓慢上浮、出现时放大，行为同原版爱心粒子，贴图走方块图集 */
@SideOnly(Side.CLIENT)
public class ParticleSparkle extends Particle {

    private final float baseScale;

    public ParticleSparkle(World world, double x, double y, double z, TextureAtlasSprite sprite) {
        super(world, x, y, z, 0.0D, 0.0D, 0.0D);
        motionX *= 0.01D;
        motionY = motionY * 0.01D + 0.1D;
        motionZ *= 0.01D;
        particleScale *= 1.125F;
        baseScale = particleScale;
        particleMaxAge = 16;
        canCollide = false;
        setParticleTexture(sprite);
    }

    @Override
    public int getFXLayer() {
        return 1;
    }

    @Override
    public void renderParticle(BufferBuilder buffer, Entity entity, float partialTicks, float rotX, float rotZ,
                               float rotYZ, float rotXY, float rotXZ) {
        particleScale = baseScale * MathHelper.clamp((particleAge + partialTicks) / particleMaxAge * 32.0F, 0.0F, 1.0F);
        super.renderParticle(buffer, entity, partialTicks, rotX, rotZ, rotYZ, rotXY, rotXZ);
    }

    @Override
    public int getBrightnessForRender(float partialTicks) {
        return 0xF000F0;
    }

    @Override
    public void onUpdate() {
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;
        if (particleAge++ >= particleMaxAge) {
            setExpired();
            return;
        }
        move(motionX, motionY, motionZ);
        motionX *= 0.86D;
        motionY *= 0.86D;
        motionZ *= 0.86D;
    }
}
