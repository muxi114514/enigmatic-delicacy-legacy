package net.mx.edelicacy.machine.pan;

import net.minecraft.item.ItemStack;
import net.mx.edelicacy.util.StackNBT;

/** 神秘遗物 eldritch_pan 物品上记录的禁忌值（对应 1.21 饕餮之锅的 KILL_COUNT 数据组件） */
public final class ForbiddenPoints {

    public static final String TAG = "ForbiddenPoints";

    private ForbiddenPoints() {
    }

    public static int get(ItemStack pan) {
        return Math.max(0, StackNBT.getInt(pan, TAG, 0));
    }

    public static void set(ItemStack pan, int points) {
        if (points <= 0) {
            StackNBT.remove(pan, TAG);
        } else {
            StackNBT.setInt(pan, TAG, points);
        }
    }

    /** 饱和加法，不会溢出成负数 */
    public static int add(int current, int amount) {
        long sum = (long) current + amount;
        return (int) Math.max(0, Math.min(Integer.MAX_VALUE, sum));
    }
}
