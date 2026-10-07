package net.mx.edelicacy.curse.item.forbidden;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.mx.edelicacy.curse.util.CurseRefs;
import net.mx.edelicacy.registry.DelicacyItems;

/** 禁忌物品标记（1.21 的 forbidden 数据组件）：提示框换成紫色边框，附属模组的物品也可实现它 */
public interface IForbiddenItem {

    /** 禁忌物品，或神秘遗物的禁忌之果 / 本模组的禁忌之果切片 */
    static boolean isForbiddenStyled(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        Item item = stack.getItem();
        return item instanceof IForbiddenItem || item == CurseRefs.forbiddenFruit() || item == DelicacyItems.FORBIDDEN_FRUIT_SLICE;
    }
}
