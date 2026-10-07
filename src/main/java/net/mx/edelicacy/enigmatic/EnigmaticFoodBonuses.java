package net.mx.edelicacy.enigmatic;

import java.math.BigDecimal;
import java.math.RoundingMode;

import keletu.enigmaticlegacy.EnigmaticConfigs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.player.EntityPlayer;
import net.mx.eaddons.attribute.AttributeSources;
import net.mx.eaddons.attribute.EnigmaticAttributes;
import net.mx.edelicacy.data.DelicacyData;
import net.mx.edelicacy.enigmatic.api.EnigmaticColor;

/**
 * 谜之食物的永久加成：每种颜色 = 对应 EL 1.12 谜之护符加成的一半，数值实时读取 EL 配置。
 * <p>全部走神遗拓展的属性来源登记表（服务端按间隔求值、变化才改修饰符），不再像 1.21 那样每 tick 给所有生物挂属性。
 */
public final class EnigmaticFoodBonuses {

    private static final String KEY_PREFIX = "enigmaticdelicacy.enigmatic_food.";

    private EnigmaticFoodBonuses() {
    }

    /** preInit 调用一次 */
    public static void register() {
        for (EnigmaticColor color : EnigmaticColor.values()) {
            // 冲刺加成要跟随冲刺状态，逐 tick 求值；其余 1 秒一次足够
            int interval = color == EnigmaticColor.AQUA ? 1 : 20;
            AttributeSources.register(KEY_PREFIX + color.id(), attribute(color), operation(color), interval,
                    player -> currentAmount(player, color));
        }
    }

    static double currentAmount(EntityPlayer player, EnigmaticColor color) {
        if (color == EnigmaticColor.AQUA && !player.isSprinting()) {
            return 0;
        }
        return DelicacyData.get(player).hasFoodAttribute(color.dataKey()) ? amount(color) : 0;
    }

    private static IAttribute attribute(EnigmaticColor color) {
        switch (color) {
            case RED:
                return SharedMonsterAttributes.ATTACK_DAMAGE;
            case AQUA:
                return SharedMonsterAttributes.MOVEMENT_SPEED;
            case VIOLET:
                return EnigmaticAttributes.PROJECTILE_DEFLECT;
            case MAGENTA:
                return EnigmaticAttributes.FALL_SPEED;
            case GREEN:
                return EnigmaticAttributes.MINING_SPEED;
            case BLACK:
                return EnigmaticAttributes.LIFESTEAL;
            default:
                return EntityLivingBase.SWIM_SPEED;
        }
    }

    /** 0 加值 / 2 乘总值，与 EL 护符的写法一致 */
    private static int operation(EnigmaticColor color) {
        return color == EnigmaticColor.AQUA || color == EnigmaticColor.MAGENTA || color == EnigmaticColor.BLUE ? 2 : 0;
    }

    /** 修饰符数值（护符的一半） */
    public static double amount(EnigmaticColor color) {
        switch (color) {
            case RED:
                return EnigmaticConfigs.enigmaticAmuletDamageBonus / 2.0D;
            case AQUA:
                return EnigmaticConfigs.enigmaticAmuletSprintSpeedBonus / 2.0D;
            case VIOLET:
                return EnigmaticConfigs.enigmaticAmuletDeflectionChance / 2.0D;
            case MAGENTA:
                // 下落速度倍率 0.9 → 修饰 -0.05，即 ×0.95
                return (EnigmaticConfigs.enigmaticAmuletSlowFallMultiplier - 1.0D) / 2.0D;
            case GREEN:
                return EnigmaticConfigs.enigmaticAmuletMiningSpeedBonus / 2.0D;
            case BLACK:
                return EnigmaticConfigs.enigmaticAmuletLifesteal / 2.0D;
            default:
                return EnigmaticConfigs.enigmaticAmuletSwimSpeedBonus / 2.0D;
        }
    }

    /** EL 护符提示行的语言键（品红只显示缓降那一行） */
    public static String tooltipKey(EnigmaticColor color) {
        return "tooltip.enigmaticlegacy.enigmaticAmuletModifier" + (color == EnigmaticColor.MAGENTA ? "MAGENTA1" : color.name());
    }

    /** 提示行里的数值，格式同 EL 护符 */
    public static String displayValue(EnigmaticColor color) {
        switch (color) {
            case RED:
                return format(amount(color));
            case MAGENTA:
                return percent(1.0D + amount(color));
            default:
                return percent(amount(color));
        }
    }

    private static String percent(double value) {
        return format(value * 100.0D) + "%";
    }

    private static String format(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }
}
