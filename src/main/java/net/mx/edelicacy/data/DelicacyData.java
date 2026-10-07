package net.mx.edelicacy.data;

import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

/**
 * 玩家的佳肴数据（对应 1.21 的 DelicacyData 附加数据），死亡后保留（etheriumSteakTick 除外）。
 * <p>任何修改都会标脏，服务端在玩家 tick 末尾把整份数据同步给本人，不会每 tick 发包。
 * 只在服务端主线程修改；客户端副本只读。
 */
public class DelicacyData {

    @CapabilityInject(DelicacyData.class)
    public static Capability<DelicacyData> CAPABILITY = null;

    /** 正在使用的炼狱浮漂实体 */
    @Nullable
    private UUID infernalHook;
    /** 已用诅咒之刃斩断七咒之戒 */
    private boolean cursedRingCut;
    /** 永久生命诅咒（诅咒药水可解除） */
    private boolean healthCursed;
    /** 深渊炖汤剩余时间（tick） */
    private int abyssStewTick;
    /** 以太牛排护盾加成剩余时间（tick） */
    private int etheriumSteakTick;
    /** 禁忌圣杯状态剩余时间（tick） */
    private int forbiddenTick;
    /** 谜之食物的永久标记：颜色名 → 是否已吃过 */
    private NBTTagCompound foodAttributes = new NBTTagCompound();

    private boolean dirty;

    /** 服务端玩家一定有；客户端尚未收到同步时也有（默认值） */
    public static DelicacyData get(EntityPlayer player) {
        DelicacyData data = player.getCapability(CAPABILITY, null);
        return data != null ? data : new DelicacyData();
    }

    @Nullable
    public UUID getInfernalHook() {
        return infernalHook;
    }

    public void setInfernalHook(@Nullable UUID infernalHook) {
        this.infernalHook = infernalHook;
        markDirty();
    }

    public boolean isCursedRingCut() {
        return cursedRingCut;
    }

    public void setCursedRingCut(boolean cursedRingCut) {
        this.cursedRingCut = cursedRingCut;
        markDirty();
    }

    public boolean isHealthCursed() {
        return healthCursed;
    }

    public void setHealthCursed(boolean healthCursed) {
        this.healthCursed = healthCursed;
        markDirty();
    }

    public int getAbyssStewTick() {
        return abyssStewTick;
    }

    public void setAbyssStewTick(int abyssStewTick) {
        this.abyssStewTick = Math.max(0, abyssStewTick);
        markDirty();
    }

    public int getEtheriumSteakTick() {
        return etheriumSteakTick;
    }

    public void setEtheriumSteakTick(int etheriumSteakTick) {
        this.etheriumSteakTick = Math.max(0, etheriumSteakTick);
        markDirty();
    }

    public int getForbiddenTick() {
        return forbiddenTick;
    }

    public void setForbiddenTick(int forbiddenTick) {
        this.forbiddenTick = Math.max(0, forbiddenTick);
        markDirty();
    }

    public boolean hasFoodAttribute(String key) {
        return foodAttributes.getBoolean(key);
    }

    public void setFoodAttribute(String key, boolean value) {
        if (value) {
            foodAttributes.setBoolean(key, true);
        } else {
            foodAttributes.removeTag(key);
        }
        markDirty();
    }

    public void clearFoodAttributes() {
        foodAttributes = new NBTTagCompound();
        markDirty();
    }

    /** 只读副本，用于遍历 */
    public NBTTagCompound getFoodAttributes() {
        return foodAttributes.copy();
    }

    public void markDirty() {
        dirty = true;
    }

    boolean consumeDirty() {
        boolean was = dirty;
        dirty = false;
        return was;
    }

    public NBTTagCompound serialize() {
        NBTTagCompound tag = new NBTTagCompound();
        if (infernalHook != null) {
            tag.setUniqueId("InfernalHook", infernalHook);
        }
        tag.setBoolean("CursedRingCut", cursedRingCut);
        tag.setBoolean("HealthCursed", healthCursed);
        tag.setInteger("AbyssStewTick", abyssStewTick);
        tag.setInteger("EtheriumSteakTick", etheriumSteakTick);
        tag.setInteger("ForbiddenTick", forbiddenTick);
        tag.setTag("FoodAttributes", foodAttributes.copy());
        return tag;
    }

    public void deserialize(NBTTagCompound tag) {
        infernalHook = tag.hasUniqueId("InfernalHook") ? tag.getUniqueId("InfernalHook") : null;
        cursedRingCut = tag.getBoolean("CursedRingCut");
        healthCursed = tag.getBoolean("HealthCursed");
        abyssStewTick = tag.getInteger("AbyssStewTick");
        etheriumSteakTick = tag.getInteger("EtheriumSteakTick");
        forbiddenTick = tag.getInteger("ForbiddenTick");
        foodAttributes = tag.getCompoundTag("FoodAttributes").copy();
    }
}
