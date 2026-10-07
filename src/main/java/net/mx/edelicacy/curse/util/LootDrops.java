package net.mx.edelicacy.curse.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 让生物以「被玩家击杀」的名义掉落一次战利品而不死亡（1.21 的 setLastHurtByPlayer + dropFromLootTable）。
 * EntityLivingBase 的 dropLoot / attackingPlayer / recentlyHit 都是 protected，只能反射；只在服务端主线程调用。
 */
public final class LootDrops {

    private static final Logger LOG = LogManager.getLogger("enigmaticdelicacy");
    @Nullable
    private static Method dropLoot;
    @Nullable
    private static Field attackingPlayer;
    @Nullable
    private static Field recentlyHit;
    private static boolean lookupDone;

    private LootDrops() {
    }

    public static void dropAsPlayerKill(EntityLivingBase target, EntityPlayer player) {
        if (target.world.isRemote) {
            return;
        }
        lookup();
        if (dropLoot == null) {
            return;
        }
        Object oldPlayer = null;
        int oldRecentlyHit = 0;
        boolean swapped = false;
        try {
            if (attackingPlayer != null && recentlyHit != null) {
                // 临时伪装成玩家刚击中过它，让「被玩家击杀」条件的战利品生效，掉落后还原
                oldPlayer = attackingPlayer.get(target);
                oldRecentlyHit = recentlyHit.getInt(target);
                attackingPlayer.set(target, player);
                recentlyHit.setInt(target, 100);
                swapped = true;
            }
            dropLoot.invoke(target, true, 0, DamageSource.causePlayerDamage(player));
        } catch (ReflectiveOperationException | RuntimeException e) {
            LOG.warn("Loot drop failed for {}", target, e);
        } finally {
            if (swapped) {
                try {
                    attackingPlayer.set(target, oldPlayer);
                    recentlyHit.setInt(target, oldRecentlyHit);
                } catch (IllegalAccessException ignored) {
                    // 还原失败只影响该生物下次死亡的经验判定
                }
            }
        }
    }

    private static void lookup() {
        if (lookupDone) {
            return;
        }
        lookupDone = true;
        try {
            dropLoot = ReflectionHelper.findMethod(EntityLivingBase.class, "dropLoot", "func_184610_a",
                    boolean.class, int.class, DamageSource.class);
            attackingPlayer = ReflectionHelper.findField(EntityLivingBase.class, "attackingPlayer", "field_70717_bb");
            recentlyHit = ReflectionHelper.findField(EntityLivingBase.class, "recentlyHit", "field_70718_bc");
        } catch (RuntimeException e) {
            LOG.warn("EntityLivingBase loot members not found, knife loot drops disabled", e);
        }
    }
}
