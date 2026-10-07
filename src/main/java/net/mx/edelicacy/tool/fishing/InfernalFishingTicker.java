package net.mx.edelicacy.tool.fishing;

import java.util.List;
import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldServer;
import net.mx.edelicacy.tool.ToolConfig;
import net.mx.edelicacy.util.WorldHelper;

/**
 * 熔岩浮漂的「等鱼—鱼游近—咬钩」计时与收杆结算（服务端），移植自 1.21 InfernalHook#catchingFish / retrieve。
 * 看不到天空时有一半概率本 tick 不推进（下界里会慢一些，与 1.21 相同）。
 */
final class InfernalFishingTicker {

    private InfernalFishingTicker() {
    }

    static void tick(EntityInfernalHook hook, WorldServer world, BlockPos pos) {
        Random rand = hook.random();
        FishingTimers t = hook.timers();
        int step = 1;
        if (rand.nextFloat() < 0.5F && !world.canSeeSky(pos.up())) {
            --step;
        }
        if (t.catchable > 0) {
            t.catchable--;
            if (t.catchable <= 0) {
                t.caughtDelay = 0;
                t.catchableDelay = 0;
            } else {
                hook.motionY -= 0.1D * rand.nextFloat() * rand.nextFloat();
            }
        } else if (t.catchableDelay > 0) {
            t.catchableDelay -= step;
            if (t.catchableDelay > 0) {
                approachParticles(hook, world, rand);
            } else {
                bite(hook, world, rand);
            }
        } else if (t.caughtDelay > 0) {
            t.caughtDelay -= step;
            lureParticles(hook, world, rand);
            if (t.caughtDelay <= 0) {
                t.approachAngle = MathHelper.nextFloat(rand, 0.0F, 360.0F);
                t.catchableDelay = MathHelper.getInt(rand, 20, 80);
            }
        } else {
            // 饵钓每级缩短 5 秒（原版 lureSpeed × 100 tick）
            t.caughtDelay = MathHelper.getInt(rand, 100, 600) - t.lureSpeed * 100;
        }
    }

    private static boolean isLava(WorldServer world, double x, double y, double z) {
        IBlockState state = WorldHelper.getBlockStateIfLoaded(world, new BlockPos(x, y, z));
        return state != null && state.getMaterial() == Material.LAVA;
    }

    private static void approachParticles(EntityInfernalHook hook, WorldServer world, Random rand) {
        FishingTimers t = hook.timers();
        t.approachAngle += (float) (rand.nextGaussian() * 4.0D);
        float angle = t.approachAngle * 0.017453292F;
        float sin = MathHelper.sin(angle);
        float cos = MathHelper.cos(angle);
        double x = hook.posX + sin * t.catchableDelay * 0.1F;
        double y = MathHelper.floor(hook.getEntityBoundingBox().minY) + 1.0D;
        double z = hook.posZ + cos * t.catchableDelay * 0.1F;
        if (!isLava(world, x, y - 1.0D, z)) {
            return;
        }
        if (rand.nextFloat() < 0.15F) {
            world.spawnParticle(EnumParticleTypes.LAVA, x, y - 0.1D, z, 1, sin, 0.1D, cos, 0.0D);
        }
        float dx = sin * 0.04F;
        float dz = cos * 0.04F;
        world.spawnParticle(EnumParticleTypes.FLAME, x, y, z, 0, dz, 0.01D, -dx, 1.0D);
        world.spawnParticle(EnumParticleTypes.FLAME, x, y, z, 0, -dz, 0.01D, dx, 1.0D);
    }

    private static void bite(EntityInfernalHook hook, WorldServer world, Random rand) {
        hook.motionY = -0.4F * MathHelper.nextFloat(rand, 0.6F, 1.0F);
        hook.playSound(SoundEvents.ENTITY_BOBBER_SPLASH, 0.25F, 1.0F + (rand.nextFloat() - rand.nextFloat()) * 0.4F);
        double y = hook.getEntityBoundingBox().minY + 0.5D;
        int count = (int) (1.0F + hook.width * 20.0F);
        world.spawnParticle(EnumParticleTypes.FLAME, hook.posX, y, hook.posZ, count, hook.width, 0.0D, hook.width, 0.05D);
        world.spawnParticle(EnumParticleTypes.LAVA, hook.posX, y, hook.posZ, count, hook.width, 0.0D, hook.width, 0.2D);
        hook.timers().catchable = MathHelper.getInt(rand, 20, 40);
    }

    private static void lureParticles(EntityInfernalHook hook, WorldServer world, Random rand) {
        int delay = hook.timers().caughtDelay;
        float chance = 0.15F;
        if (delay < 20) {
            chance += (20 - delay) * 0.05F;
        } else if (delay < 40) {
            chance += (40 - delay) * 0.02F;
        } else if (delay < 60) {
            chance += (60 - delay) * 0.01F;
        }
        if (rand.nextFloat() >= chance) {
            return;
        }
        float angle = MathHelper.nextFloat(rand, 0.0F, 360.0F) * 0.017453292F;
        float distance = MathHelper.nextFloat(rand, 25.0F, 60.0F);
        double x = hook.posX + MathHelper.sin(angle) * distance * 0.1F;
        double y = MathHelper.floor(hook.getEntityBoundingBox().minY) + 1.0D;
        double z = hook.posZ + MathHelper.cos(angle) * distance * 0.1F;
        if (isLava(world, x, y - 1.0D, z)) {
            world.spawnParticle(EnumParticleTypes.LAVA, x, y, z, 2 + rand.nextInt(2), 0.1D, 0.0D, 0.1D, 0.0D);
        }
    }

    /** 咬钩时收杆：小概率岩浆怪，否则按战利品表出货并给经验；返回钓竿损耗 */
    static int reelIn(EntityInfernalHook hook, WorldServer world, EntityPlayer angler, int luck) {
        Random rand = hook.random();
        double dx = angler.posX - hook.posX;
        double dy = angler.posY - hook.posY;
        double dz = angler.posZ - hook.posZ;
        double lift = Math.sqrt(Math.sqrt(dx * dx + dy * dy + dz * dz)) * 0.08D;
        if (rand.nextDouble() < ToolConfig.magmaCubeChance && WorldHelper.isAreaLoaded(world, hook.getPosition(), 2)) {
            EntityMagmaCube cube = new EntityMagmaCube(world);
            NBTTagCompound size = new NBTTagCompound();
            size.setInteger("Size", 0);
            cube.readEntityFromNBT(size);
            cube.setHealth(cube.getMaxHealth());
            cube.setLocationAndAngles(hook.posX, hook.posY + 0.9D, hook.posZ, rand.nextFloat() * 360.0F, 0.0F);
            cube.motionX = dx * 0.1D;
            cube.motionY = dy * 0.1D + lift;
            cube.motionZ = dz * 0.1D;
            world.spawnEntity(cube);
            return 3;
        }
        // luck 已含抛竿时钓竿的海之眷顾等级
        float totalLuck = luck + angler.getLuck();
        List<ItemStack> loot = InfernalFishingLoot.roll(rand, totalLuck, hook.isOpenLava());
        for (ItemStack stack : loot) {
            EntityItem item = new EntityItem(world, hook.posX, hook.posY, hook.posZ, stack);
            item.motionX = dx * 0.1D;
            item.motionY = dy * 0.1D + lift;
            item.motionZ = dz * 0.1D;
            // 多 10 点耐久，飞过熔岩时不容易被烧掉（同 1.21）
            item.attackEntityFrom(DamageSource.MAGIC, -10.0F);
            world.spawnEntity(item);
            world.spawnEntity(new EntityXPOrb(world, angler.posX, angler.posY + 0.5D, angler.posZ + 0.5D, rand.nextInt(6) + 1));
        }
        return 1;
    }
}
