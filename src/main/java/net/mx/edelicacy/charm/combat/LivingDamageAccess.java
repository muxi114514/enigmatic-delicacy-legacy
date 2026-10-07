package net.mx.edelicacy.charm.combat;

import java.lang.reflect.Field;

import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** 读写 EntityLivingBase.lastDamage（受击无敌期间只有超过它的伤害才生效），字段只查找一次 */
public final class LivingDamageAccess {

    private static final Logger LOG = LogManager.getLogger("enigmaticdelicacy");
    private static final Field LAST_DAMAGE = find();

    private LivingDamageAccess() {
    }

    private static Field find() {
        try {
            return ReflectionHelper.findField(EntityLivingBase.class, "lastDamage", "field_110153_bc");
        } catch (RuntimeException e) {
            LOG.error("EntityLivingBase.lastDamage not found, invulnerability tweaks disabled", e);
            return null;
        }
    }

    public static float getLastDamage(EntityLivingBase entity) {
        if (LAST_DAMAGE != null) {
            try {
                return LAST_DAMAGE.getFloat(entity);
            } catch (IllegalAccessException ignored) {
                // findField 已设为可访问，不会发生
            }
        }
        return 0.0F;
    }

    public static void setLastDamage(EntityLivingBase entity, float value) {
        if (LAST_DAMAGE != null) {
            try {
                LAST_DAMAGE.setFloat(entity, value);
            } catch (IllegalAccessException ignored) {
                // 同上
            }
        }
    }
}
