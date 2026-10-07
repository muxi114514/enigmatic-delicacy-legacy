package net.mx.edelicacy.food.entity;

import java.util.List;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mx.edelicacy.registry.DelicacyItems;
import net.mx.edelicacy.util.WorldHelper;

/**
 * 掷出的炽焰肉卷：命中方块或生物时爆燃，半径 2 内的生物受 4 点爆炸伤害并着火 4 秒。
 * 不调用原版爆炸，不破坏方块；作用范围所在区块未全部加载时只消失、不结算。
 */
public class EntityThrownBlazingMeatRoll extends EntityThrowable {

    private static final byte EVENT_BURST = 3;
    private static final double RADIUS = 2.0D;
    private static final float DAMAGE = 4.0F;
    private static final int IGNITE_SECONDS = 4;

    public EntityThrownBlazingMeatRoll(World world) {
        super(world);
    }

    public EntityThrownBlazingMeatRoll(World world, EntityLivingBase thrower) {
        super(world, thrower);
    }

    @Override
    protected void onImpact(RayTraceResult result) {
        if (world.isRemote) {
            return;
        }
        if (WorldHelper.isAreaLoaded(world, new BlockPos(this), (int) Math.ceil(RADIUS) + 1)) {
            burst();
        }
        setDead();
    }

    private void burst() {
        world.setEntityState(this, EVENT_BURST);
        world.playSound(null, posX, posY, posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.AMBIENT, 0.4F, 1.8F);
        EntityLivingBase thrower = getThrower();
        // 记到投掷者名下（1.21 记在弹射物自己身上，击杀不算玩家）
        DamageSource source = thrower != null
                ? new EntityDamageSourceIndirect("explosion.player", this, thrower).setExplosion()
                : new EntityDamageSource("explosion", this).setExplosion();
        List<EntityLivingBase> targets = world.getEntitiesWithinAABB(EntityLivingBase.class,
                getEntityBoundingBox().grow(RADIUS), EntitySelectors.IS_ALIVE);
        for (EntityLivingBase target : targets) {
            target.attackEntityFrom(source, DAMAGE);
            target.setFire(IGNITE_SECONDS);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void handleStatusUpdate(byte id) {
        if (id != EVENT_BURST) {
            super.handleStatusUpdate(id);
            return;
        }
        Item item = DelicacyItems.BLAZING_MEAT_ROLL;
        double x = posX - motionX * 0.5D;
        double y = posY - motionY * 0.5D;
        double z = posZ - motionZ * 0.5D;
        for (int i = 0; i < 8; i++) {
            if (item != null) {
                world.spawnParticle(EnumParticleTypes.ITEM_CRACK, x, y, z, spread(), spread(), spread(), Item.getIdFromItem(item));
            }
            world.spawnParticle(EnumParticleTypes.FLAME, x, y, z, spread(), spread(), spread());
            // 1.21 的绯红孢子：用暗红色红石粉粒子代替（速度参数即颜色）
            world.spawnParticle(EnumParticleTypes.REDSTONE, x + spread(), y + spread(), z + spread(), 0.6D, 0.05D, 0.1D);
        }
        world.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, x, y, z, 0.0D, 0.0D, 0.0D);
    }

    private double spread() {
        return (rand.nextFloat() - 0.5F) * 0.2D;
    }
}
