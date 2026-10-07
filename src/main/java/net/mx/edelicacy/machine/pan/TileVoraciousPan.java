package net.mx.edelicacy.machine.pan;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.ItemStackHandler;

/**
 * 放下的饕餮之锅：保存整件 eldritch_pan 物品与它的禁忌值，外加一个待转化的输入槽。
 * <p>禁忌值单独存放，取回锅时写回物品 NBT；{@link #takePanForDrop()} 取出后即清空，杜绝重复掉落。只在服务端主线程修改。
 */
public class TileVoraciousPan extends TileEntity {

    private ItemStack pan = ItemStack.EMPTY;
    private int points;
    private final ItemStackHandler input = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            markDirty();
        }
    };

    public ItemStackHandler getInput() {
        return input;
    }

    public int getPoints() {
        return points;
    }

    /** 放置时写入：只存 1 件 */
    public void setPan(ItemStack stack) {
        ItemStack copy = stack.copy();
        copy.setCount(1);
        points = ForbiddenPoints.get(copy);
        pan = copy;
        markDirty();
    }

    /** 界面里展示用的副本（带当前禁忌值） */
    public ItemStack getPanDisplay() {
        if (pan.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack copy = pan.copy();
        ForbiddenPoints.set(copy, points);
        return copy;
    }

    /** 扣除禁忌值；不足时不扣并返回 false */
    public boolean consumePoints(int cost) {
        if (cost < 0 || cost > points) {
            return false;
        }
        points -= cost;
        markDirty();
        return true;
    }

    /** 取出锅（写回禁忌值）并清空，之后再调用只会得到空物品 */
    public ItemStack takePanForDrop() {
        if (pan.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack result = pan;
        ForbiddenPoints.set(result, points);
        pan = ItemStack.EMPTY;
        points = 0;
        markDirty();
        return result;
    }

    /** 取出输入槽里的物品并清空 */
    public ItemStack takeInputForDrop() {
        ItemStack stack = input.getStackInSlot(0);
        input.setStackInSlot(0, ItemStack.EMPTY);
        return stack;
    }

    public boolean isUsableByPlayer(EntityPlayer player) {
        return world != null && world.getTileEntity(pos) == this
                && player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        if (!pan.isEmpty()) {
            compound.setTag("Pan", pan.writeToNBT(new NBTTagCompound()));
        }
        compound.setInteger("Points", points);
        compound.setTag("Input", input.serializeNBT());
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        pan = compound.hasKey("Pan", Constants.NBT.TAG_COMPOUND) ? new ItemStack(compound.getCompoundTag("Pan")) : ItemStack.EMPTY;
        points = Math.max(0, compound.getInteger("Points"));
        if (compound.hasKey("Input", Constants.NBT.TAG_COMPOUND)) {
            input.deserializeNBT(compound.getCompoundTag("Input"));
        }
    }
}
