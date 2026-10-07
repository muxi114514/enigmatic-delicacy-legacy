package net.mx.edelicacy.curse.potion;

/** 禁忌之印：受到的伤害与获得的治疗都减少 (等级+2)×5%，结算见 {@link ForbiddenImprintHandler} */
public class PotionForbiddenImprint extends CursePotionBase {

    public PotionForbiddenImprint() {
        super("forbidden_imprint", false, 0x5F2E76, true);
    }

    /** 等级（amplifier）对应的减免比例 */
    public static float reduction(int amplifier) {
        return Math.max(0.0F, Math.min(1.0F, (amplifier + 2) * 0.05F));
    }
}
