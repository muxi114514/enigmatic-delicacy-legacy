package net.mx.edelicacy.curse.item.gluttony;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.mx.edelicacy.curse.util.CurseRefs;
import net.mx.edelicacy.registry.DelicacyItems;
import net.mx.edelicacy.util.EnigmaticBridge;

/**
 * 无尽贪食徽记的核心数值：缺失饥饿值比例（同时佩戴染血勇气徽记时与缺失生命混合），
 * 以及反过来让 EL 染血勇气徽记的「缺失生命比例」混入缺失饥饿（mixin 调用）。
 */
public final class GluttonyLogic {

    private GluttonyLogic() {
    }

    public static boolean wearing(@Nullable EntityLivingBase entity) {
        return entity instanceof EntityPlayer && EnigmaticBridge.hasBauble((EntityPlayer) entity, DelicacyItems.GLUTTONY_CHARM);
    }

    public static float missingFood(EntityPlayer player) {
        int food = player.getFoodStats().getFoodLevel();
        return (20 - Math.min(food, 20)) / 20.0F;
    }

    /** 1.21 的 getMissingFoodProperty */
    public static float missingFoodProperty(@Nullable EntityLivingBase entity) {
        if (!(entity instanceof EntityPlayer)) {
            return 0.0F;
        }
        EntityPlayer player = (EntityPlayer) entity;
        float missing = missingFood(player);
        if (EnigmaticBridge.hasBauble(player, CurseRefs.berserkEmblem())) {
            float maxHealth = Math.max(1.0F, player.getMaxHealth());
            float missingHealth = (maxHealth - Math.min(player.getHealth(), maxHealth)) / maxHealth;
            missing = missing * 0.8F + missingHealth * 0.4F;
        }
        return missing;
    }

    /** EL 染血勇气徽记的缺失生命池：佩戴本徽记时 = 池×0.8 + 缺失饥饿×0.4（1.21 MixinBerserkEmblem） */
    public static float mixBerserkPool(EntityPlayer player, float pool) {
        return wearing(player) ? pool * 0.8F + missingFood(player) * 0.4F : pool;
    }

    public static boolean hasHellBladeCharm(EntityLivingBase entity) {
        return entity instanceof EntityPlayer && EnigmaticBridge.hasBauble((EntityPlayer) entity, CurseRefs.HELL_BLADE_CHARM.get());
    }
}
