package net.mx.edelicacy.curse.item.soul;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.mx.edelicacy.util.StackNBT;

/** 咒魂值、击杀数、灌注阈值（1.21 的 soul_amount / kill_count / soul_threshold 数据组件） */
public final class SoulNBT {

    public static final String SOUL_AMOUNT = "SoulAmount";
    public static final String KILL_COUNT = "KillCount";
    public static final String SOUL_THRESHOLD = "SoulThreshold";

    private SoulNBT() {
    }

    public static int getSoulAmount(ItemStack stack) {
        return StackNBT.getInt(stack, SOUL_AMOUNT, 0);
    }

    public static void setSoulAmount(ItemStack stack, int amount) {
        StackNBT.setInt(stack, SOUL_AMOUNT, Math.max(0, amount));
    }

    public static int getKillCount(ItemStack stack) {
        return StackNBT.getInt(stack, KILL_COUNT, 0);
    }

    public static void addKillCount(ItemStack stack) {
        StackNBT.setInt(stack, KILL_COUNT, getKillCount(stack) + 1);
    }

    public static int getSoulThreshold(ItemStack stack) {
        return StackNBT.getInt(stack, SOUL_THRESHOLD, 0);
    }

    /** 阈值 = 当前生命上限 × 8 */
    public static void setSoulThreshold(ItemStack stack, EntityPlayer player) {
        StackNBT.setInt(stack, SOUL_THRESHOLD, MathHelper.floor(player.getMaxHealth() * 8.0F));
    }

    /** 清除咒魂值与击杀数（晶刃重置用） */
    public static void clearSouls(ItemStack stack) {
        StackNBT.remove(stack, SOUL_AMOUNT);
        StackNBT.remove(stack, KILL_COUNT);
        if (stack.hasTagCompound() && stack.getTagCompound().hasNoTags()) {
            stack.setTagCompound(null);
        }
    }
}
