package net.mx.edelicacy.util;

import java.lang.reflect.Field;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 读 EntityPlayer.sleepTimer（入睡计时）。原版 getSleepTimer() 是 @SideOnly(CLIENT)，专用服 jar 里没有，
 * 服务端调用会 NoSuchMethodError；字段双端都在，这里只查找一次再反射读取。
 */
public final class PlayerSleepAccess {

    private static final Logger LOG = LogManager.getLogger("enigmaticdelicacy");
    private static final Field SLEEP_TIMER = find();

    private PlayerSleepAccess() {
    }

    private static Field find() {
        try {
            return ReflectionHelper.findField(EntityPlayer.class, "sleepTimer", "field_71076_b");
        } catch (RuntimeException e) {
            LOG.error("EntityPlayer.sleepTimer not found, sleep tweaks disabled", e);
            return null;
        }
    }

    public static int getSleepTimer(EntityPlayer player) {
        if (SLEEP_TIMER != null) {
            try {
                return SLEEP_TIMER.getInt(player);
            } catch (IllegalAccessException ignored) {
                // findField 已设为可访问，不会发生
            }
        }
        return 0;
    }
}
