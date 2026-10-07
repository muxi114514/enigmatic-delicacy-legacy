package net.mx.edelicacy.machine.stove;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.Constants;

/**
 * 以太炉灶的「无燃料熔炉」部分：1 输入 + 1 输出。
 * <p>修复优先于熔炼（防止别的模组给以太装备加了回收熔炼配方，修复反被熔掉）；
 * 熔炼与 GUI、JEI 用的是同一张原版熔炉配方表（修正 1.21 GUI 判定熔炉配方、处理却用营火配方的不一致）。
 */
final class StoveFurnace {

    static final int INPUT = 0;
    static final int OUTPUT = 1;
    /** 原版熔炉一次熔炼的进度 */
    private static final int SMELT_PROGRESS = 200;

    private final NonNullList<ItemStack> stacks = NonNullList.withSize(2, ItemStack.EMPTY);
    private int progress;
    private int total;

    NonNullList<ItemStack> stacks() {
        return stacks;
    }

    int getProgress() {
        return progress;
    }

    int getTotal() {
        return total;
    }

    /** 可放进输入槽处理的物品（熔炼或修复） */
    static boolean isProcessable(ItemStack stack) {
        return StoveRepairRules.needsRepair(stack) || !FurnaceRecipes.instance().getSmeltingResult(stack).isEmpty();
    }

    void resetProgress() {
        progress = 0;
    }

    /** 点燃时每 tick 调用；返回物品是否变化 */
    boolean tick(int speed) {
        ItemStack input = stacks.get(INPUT);
        if (input.isEmpty()) {
            progress = 0;
            total = 0;
            return false;
        }
        boolean repair = StoveRepairRules.needsRepair(input);
        if (repair ? !canRepair() : !canSmelt(input)) {
            progress = 0;
            total = 0;
            return false;
        }
        total = repair ? StoveRepairRules.getCycleProgress(input) : SMELT_PROGRESS;
        progress += speed;
        if (progress < total) {
            return false;
        }
        progress = 0;
        if (repair) {
            finishRepair(input);
        } else {
            finishSmelt(input);
        }
        return true;
    }

    private boolean canRepair() {
        return stacks.get(OUTPUT).isEmpty();
    }

    private boolean canSmelt(ItemStack input) {
        ItemStack result = FurnaceRecipes.instance().getSmeltingResult(input);
        if (result.isEmpty()) {
            return false;
        }
        ItemStack output = stacks.get(OUTPUT);
        if (output.isEmpty()) {
            return true;
        }
        if (!output.isItemEqual(result) || !ItemStack.areItemStackTagsEqual(output, result)) {
            return false;
        }
        int count = output.getCount() + result.getCount();
        return count <= output.getMaxStackSize();
    }

    private void finishSmelt(ItemStack input) {
        ItemStack result = FurnaceRecipes.instance().getSmeltingResult(input).copy();
        ItemStack output = stacks.get(OUTPUT);
        if (output.isEmpty()) {
            stacks.set(OUTPUT, result);
        } else {
            output.grow(result.getCount());
        }
        input.shrink(1);
        if (input.isEmpty()) {
            stacks.set(INPUT, ItemStack.EMPTY);
        }
    }

    /** 逐单位修复，修满后移到输出槽 */
    private void finishRepair(ItemStack input) {
        StoveRepairRules.finishCycle(input);
        if (!StoveRepairRules.needsRepair(input)) {
            stacks.set(OUTPUT, input);
            stacks.set(INPUT, ItemStack.EMPTY);
        }
    }

    void write(NBTTagCompound tag) {
        NBTTagCompound items = new NBTTagCompound();
        items.setTag("Input", stacks.get(INPUT).writeToNBT(new NBTTagCompound()));
        items.setTag("Output", stacks.get(OUTPUT).writeToNBT(new NBTTagCompound()));
        tag.setTag("Furnace", items);
        tag.setInteger("FurnaceTime", progress);
        tag.setInteger("FurnaceTimeTotal", total);
    }

    void read(NBTTagCompound tag) {
        NBTTagCompound items = tag.getCompoundTag("Furnace");
        stacks.set(INPUT, items.hasKey("Input", Constants.NBT.TAG_COMPOUND)
                ? new ItemStack(items.getCompoundTag("Input")) : ItemStack.EMPTY);
        stacks.set(OUTPUT, items.hasKey("Output", Constants.NBT.TAG_COMPOUND)
                ? new ItemStack(items.getCompoundTag("Output")) : ItemStack.EMPTY);
        progress = tag.getInteger("FurnaceTime");
        total = tag.getInteger("FurnaceTimeTotal");
    }
}
