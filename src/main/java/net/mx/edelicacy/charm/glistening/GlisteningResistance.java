package net.mx.edelicacy.charm.glistening;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.mx.edelicacy.charm.CharmConfig;
import net.mx.edelicacy.util.StackNBT;

/** 守护护符物品上的减伤系数（%，对应 1.21 GLISTENING_RESISTANCE 组件）与上次计入受击的时间 */
public final class GlisteningResistance {

    private static final String KEY = "GlisteningResistance";
    private static final String LAST_HIT = "GlisteningLastHit";
    /** 1.21 只在受击无敌 ≤ 5 时扣减，即距上次受击 ≥ 15 tick */
    private static final long HIT_INTERVAL = 15;

    private GlisteningResistance() {
    }

    public static int get(ItemStack charm) {
        return Math.max(0, Math.min(CharmConfig.glisteningCharmThreshold, StackNBT.getInt(charm, KEY, 0)));
    }

    public static void set(ItemStack charm, int value) {
        StackNBT.setInt(charm, KEY, Math.max(0, Math.min(CharmConfig.glisteningCharmThreshold, value)));
    }

    public static void add(ItemStack charm, int delta) {
        set(charm, get(charm) + delta);
    }

    /** 记录一次受击；距上次计入不足间隔时返回 false（连击不重复扣减） */
    public static boolean markHit(ItemStack charm, long worldTime) {
        NBTTagCompound tag = charm.getTagCompound();
        long last = tag != null && tag.hasKey(LAST_HIT) ? tag.getLong(LAST_HIT) : Long.MIN_VALUE / 2;
        if (tag == null) {
            tag = new NBTTagCompound();
            charm.setTagCompound(tag);
        }
        tag.setLong(LAST_HIT, worldTime);
        return worldTime - last >= HIT_INTERVAL || worldTime < last;
    }
}
