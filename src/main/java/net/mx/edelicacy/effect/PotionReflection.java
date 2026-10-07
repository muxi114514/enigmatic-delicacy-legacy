package net.mx.edelicacy.effect;

import java.lang.reflect.Field;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 原版药水的两个私有字段（字段已缓存，调用开销只有一次 Field 读写）：
 * <ul>
 *   <li>EntityLivingBase.potionsNeedUpdate：置 true 后下一次 updatePotionEffects 重算粒子颜色</li>
 *   <li>Potion.beneficial：1.12 的 isBeneficial() 只在客户端存在，服务端判断「有益效果」只能读字段</li>
 * </ul>
 */
public final class PotionReflection {

    private static final Logger LOG = LogManager.getLogger("enigmaticdelicacy");
    @Nullable
    private static final Field POTIONS_NEED_UPDATE = find(EntityLivingBase.class, "field_70752_e");
    @Nullable
    private static final Field BENEFICIAL = find(Potion.class, "field_188415_h");

    private PotionReflection() {
    }

    @Nullable
    private static Field find(Class<?> owner, String srgName) {
        try {
            return ObfuscationReflectionHelper.findField(owner, srgName);
        } catch (RuntimeException e) {
            LOG.warn("Field {} of {} not found", srgName, owner.getName(), e);
            return null;
        }
    }

    /** 让实体在本 tick 重算药水粒子颜色 */
    public static void markPotionsDirty(EntityLivingBase entity) {
        if (POTIONS_NEED_UPDATE != null) {
            try {
                POTIONS_NEED_UPDATE.setBoolean(entity, true);
            } catch (IllegalAccessException ignored) {
                // findField 已 setAccessible，不会发生
            }
        }
    }

    /** 有益效果；字段读不到时退回「非负面」 */
    public static boolean isBeneficial(Potion potion) {
        if (BENEFICIAL != null) {
            try {
                return BENEFICIAL.getBoolean(potion);
            } catch (IllegalAccessException ignored) {
                // 同上
            }
        }
        return !potion.isBadEffect();
    }
}
