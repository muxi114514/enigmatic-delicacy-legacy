package net.mx.edelicacy.curse.potion;

import net.minecraft.entity.SharedMonsterAttributes;

/** 生命诅咒：每级 -5% 生命上限（乘基础值）。斩断七咒之戒后由 {@link HealthCurseHandler} 维持为永久 */
public class PotionHealthCurse extends CursePotionBase {

    public PotionHealthCurse() {
        super("health_curse", false, 0xF13F1A, true);
        registerPotionAttributeModifier(SharedMonsterAttributes.MAX_HEALTH, "5b3b9c3e-6f2a-4d39-9c39-0a1f61a2d1c4", -0.05D, 1);
    }
}
