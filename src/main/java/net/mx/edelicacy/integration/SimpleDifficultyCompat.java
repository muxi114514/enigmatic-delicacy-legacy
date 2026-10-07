package net.mx.edelicacy.integration;

import com.charles445.simpledifficulty.api.config.JsonConfig;
import com.charles445.simpledifficulty.api.config.json.JsonItemIdentity;

import net.mx.edelicacy.EnigmaticDelicacy;

/**
 * SimpleDifficulty 联动（只在它已加载时调用）：
 * 口渴值沿用 1.21 原版给 Thirst 模组登记的数值；温度为 1.12 新增——冰棍降温，激情炒饭与烈焰肉卷暖身
 * （顶替原版「我的下界乐事」的辛辣效果）。SD 在 postInit 合并进 json，玩家仍可在配置里改。
 */
final class SimpleDifficultyCompat {

    private static final JsonItemIdentity ANY_META = new JsonItemIdentity(-1);

    private SimpleDifficultyCompat() {
    }

    static void register() {
        thirst("enigmatic_fruit", 2, 2);
        // 七色神秘果食用数值与普通神秘果相同，口渴也按它
        for (String color : new String[]{"red", "aqua", "violet", "magenta", "green", "black", "blue"}) {
            thirst("enigmatic_fruit_" + color, 2, 2);
        }
        thirst("enigmatic_jam", 3, 2);
        thirst("enigmatic_cream", 4, 4);
        thirst("astral_fruit_slice", 2, 5);
        thirst("celestial_custard", 9, 6);
        thirst("magic_popsicle", 7, 5);
        thirst("starlight_salad", 6, 4);
        thirst("divine_fruit_pie", 6, 12);
        thirst("abyssal_stew", 6, 4);
        thirst("curse_potion", 5, 1);
        thirst("ichoroot_soup", 7, 5);
        thirst("astral_tea", 7, 4);
        thirst("glistening_tea", 7, 5);
        thirst("harmonious_grail", 3, 2);

        temperature("food", "magic_popsicle", -2.0F, 1200);
        temperature("food", "passion_fried_rice", 2.0F, 2400);
        temperature("food", "blazing_meat_roll", 1.5F, 1200);
    }

    private static void thirst(String name, int amount, float saturation) {
        JsonConfig.registerConsumableThirst(EnigmaticDelicacy.MODID + ":" + name, amount, saturation, 0.0F, ANY_META);
    }

    private static void temperature(String group, String name, float temperature, int duration) {
        JsonConfig.registerConsumableTemperature(group, EnigmaticDelicacy.MODID + ":" + name, temperature, duration, ANY_META);
    }
}
