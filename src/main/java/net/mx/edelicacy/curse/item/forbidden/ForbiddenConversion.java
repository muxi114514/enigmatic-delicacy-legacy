package net.mx.edelicacy.curse.item.forbidden;

import java.util.Objects;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.mx.edelicacy.curse.util.CurseRefs;
import net.mx.edelicacy.registry.DelicacyItems;

/**
 * 禁忌转化的接缝：禁忌之刃击杀时按「深渊烹饪」配方转化掉落物。配方表属于饕餮之锅那条线（WP5），
 * 由它 / 整合者在 init 里调用 {@link #setConverter} 接上；没接时用下面的最小内置表（1.21 配方里与掉落物相关的两条：苹果、生肉）。
 */
public final class ForbiddenConversion {

    /** 返回转化后的单个产物（数量为每个输入对应的产量），不能转化时返回 EMPTY；不得修改 input */
    public interface Converter {
        ItemStack convert(ItemStack input, World world);
    }

    private static volatile Converter converter = ForbiddenConversion::builtin;

    private ForbiddenConversion() {
    }

    public static void setConverter(Converter newConverter) {
        converter = Objects.requireNonNull(newConverter);
    }

    public static ItemStack convert(ItemStack input, World world) {
        if (input.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack result = converter.convert(input, world);
        return result == null ? ItemStack.EMPTY : result;
    }

    /** 内置表：苹果 → 禁忌之果，各种肉（含腐肉）→ 禁忌肉团 */
    private static ItemStack builtin(ItemStack input, World world) {
        Item item = input.getItem();
        if (item == Items.APPLE && CurseRefs.forbiddenFruit() != null) {
            return new ItemStack(CurseRefs.forbiddenFruit());
        }
        if (DelicacyItems.FORBIDDEN_MEAT != null && (item == Items.BEEF || item == Items.COOKED_BEEF || item == Items.PORKCHOP
                || item == Items.COOKED_PORKCHOP || item == Items.CHICKEN || item == Items.COOKED_CHICKEN || item == Items.MUTTON
                || item == Items.COOKED_MUTTON || item == Items.RABBIT || item == Items.COOKED_RABBIT || item == Items.ROTTEN_FLESH)) {
            return new ItemStack(DelicacyItems.FORBIDDEN_MEAT);
        }
        return ItemStack.EMPTY;
    }
}
