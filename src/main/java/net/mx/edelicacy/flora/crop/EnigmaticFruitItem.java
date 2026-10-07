package net.mx.edelicacy.flora.crop;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.mx.edelicacy.EnigmaticDelicacy;
import net.mx.edelicacy.item.base.DelicacyFoodItem;
import net.mx.edelicacy.registry.DelicacyItems;

/**
 * 神秘果（4 饥饿 / 0.8 饱和，随时可吃）。七种彩色神秘果为稀有掉落，显示名与普通神秘果相同。
 */
public class EnigmaticFruitItem extends DelicacyFoodItem {

    public static final String BASE_NAME = "enigmatic_fruit";
    /** 彩色果实的注册名后缀，顺序同 1.21 的 randomColor 列表 */
    public static final String[] COLORS = {"red", "aqua", "violet", "magenta", "green", "black", "blue"};

    public EnigmaticFruitItem(String name, boolean colored) {
        super(name, 4, 0.8F);
        alwaysEdible();
        if (colored) {
            rarity(EnumRarity.UNCOMMON);
            setUnlocalizedName(EnigmaticDelicacy.MODID + "." + BASE_NAME);
        }
    }

    /** 随机一种彩色神秘果；物品尚未注册时返回 null */
    @Nullable
    public static Item randomColored(Random rand) {
        Item[] colored = {
                DelicacyItems.ENIGMATIC_FRUIT_RED, DelicacyItems.ENIGMATIC_FRUIT_AQUA, DelicacyItems.ENIGMATIC_FRUIT_VIOLET,
                DelicacyItems.ENIGMATIC_FRUIT_MAGENTA, DelicacyItems.ENIGMATIC_FRUIT_GREEN, DelicacyItems.ENIGMATIC_FRUIT_BLACK,
                DelicacyItems.ENIGMATIC_FRUIT_BLUE
        };
        return colored[rand.nextInt(colored.length)];
    }
}
