package net.mx.edelicacy.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/** 物品 NBT 读写（对应 1.21 的数据组件），读时不创建标签 */
public final class StackNBT {

    private StackNBT() {
    }

    private static NBTTagCompound tag(ItemStack stack) {
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        return stack.getTagCompound();
    }

    public static int getInt(ItemStack stack, String key, int fallback) {
        NBTTagCompound tag = stack.getTagCompound();
        return tag != null && tag.hasKey(key) ? tag.getInteger(key) : fallback;
    }

    public static void setInt(ItemStack stack, String key, int value) {
        tag(stack).setInteger(key, value);
    }

    public static float getFloat(ItemStack stack, String key, float fallback) {
        NBTTagCompound tag = stack.getTagCompound();
        return tag != null && tag.hasKey(key) ? tag.getFloat(key) : fallback;
    }

    public static void setFloat(ItemStack stack, String key, float value) {
        tag(stack).setFloat(key, value);
    }

    public static boolean getBoolean(ItemStack stack, String key) {
        NBTTagCompound tag = stack.getTagCompound();
        return tag != null && tag.getBoolean(key);
    }

    public static void setBoolean(ItemStack stack, String key, boolean value) {
        tag(stack).setBoolean(key, value);
    }

    public static void remove(ItemStack stack, String key) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag != null) {
            tag.removeTag(key);
        }
    }
}
