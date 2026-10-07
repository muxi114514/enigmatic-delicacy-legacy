package net.mx.edelicacy.api;

import net.minecraft.item.ItemStack;

/**
 * 可在以太炉里修复的物品（如以太牛排的「耐久」）。普通有耐久的以太物品不需要实现，以太炉按耐久修。
 */
public interface IStoveRepairable {

    /** 修复一单位需要的 tick 数；≤0 表示当前无需修复 */
    int getStoveRepairTicks(ItemStack stack);

    /** 修复一单位 */
    void repairOnStove(ItemStack stack);
}
