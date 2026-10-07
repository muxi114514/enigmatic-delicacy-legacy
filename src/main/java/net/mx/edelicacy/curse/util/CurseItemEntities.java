package net.mx.edelicacy.curse.util;

import java.lang.reflect.Field;

import javax.annotation.Nullable;

import keletu.enigmaticlegacy.entity.EntityItemIndestructible;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.apache.logging.log4j.LogManager;

/**
 * 掉落物实体：1.21 的 fireResistant() 对应 EL 的防火掉落物；PermanentItemEntity 对应「发光、不消失、悬浮」的防火掉落物。
 */
public final class CurseItemEntities {

    /** 原版「永不消失」的年龄标记：age 为 -32768 时不再增长，也就不会过期、归属保护不会失效 */
    private static final int FROZEN_AGE = -32768;
    @Nullable
    private static Field ageField;
    private static boolean ageLookupDone;

    private CurseItemEntities() {
    }

    /** 给 Item#createEntity 用：把普通掉落物换成防火的，保留速度、丢出者与归属 */
    public static Entity fireproof(World world, Entity location, ItemStack stack) {
        EntityItemIndestructible item = new EntityItemIndestructible(world, location.posX, location.posY, location.posZ, stack);
        item.setDefaultPickupDelay();
        item.motionX = location.motionX;
        item.motionY = location.motionY;
        item.motionZ = location.motionZ;
        if (location instanceof EntityItem) {
            item.setThrower(((EntityItem) location).getThrower());
            item.setOwner(((EntityItem) location).getOwner());
        }
        return item;
    }

    /**
     * 生成一个永久掉落物：发光、不会消失、不受重力；owner 不为空时只有他能捡起。
     */
    public static void spawnPermanent(World world, double x, double y, double z, ItemStack stack,
                                      @Nullable EntityPlayer owner, int pickupDelay) {
        if (world.isRemote || stack.isEmpty()) {
            return;
        }
        EntityItemIndestructible item = new EntityItemIndestructible(world, x, y, z, stack);
        item.motionX = 0.0D;
        item.motionY = 0.0D;
        item.motionZ = 0.0D;
        item.setNoGravity(true);
        item.setGlowing(true);
        freezeAge(item);
        item.setPickupDelay(pickupDelay);
        if (owner != null) {
            item.setOwner(owner.getName());
        }
        world.spawnEntity(item);
    }

    private static void freezeAge(EntityItem item) {
        item.lifespan = Integer.MAX_VALUE;
        if (!ageLookupDone) {
            ageLookupDone = true;
            try {
                ageField = ReflectionHelper.findField(EntityItem.class, "age", "field_70292_b");
            } catch (RuntimeException e) {
                LogManager.getLogger("enigmaticdelicacy").warn("EntityItem age field not found, permanent items use setNoDespawn", e);
            }
        }
        try {
            if (ageField != null) {
                ageField.setInt(item, FROZEN_AGE);
                return;
            }
        } catch (IllegalAccessException ignored) {
            // 回落到原版方法
        }
        item.setNoDespawn();
    }
}
