package net.mx.edelicacy.machine.stove;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotFurnaceOutput;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * 以太炉灶的熔炉界面：输入 (56,17)、输出 (116,35)，无燃料槽，没有配方书。
 * 进度以千分比同步（窗口属性只有 16 位，修复轮次的总进度可能超出）。
 * 输出槽沿用原版熔炉输出槽：取出时按熔炉配方发经验、触发 PlayerSmeltedEvent。
 */
public class ContainerEtheriumStove extends Container {

    public static final int SLOT_INPUT = 0;
    public static final int SLOT_OUTPUT = 1;
    public static final int PLAYER_START = 2;
    private static final int PLAYER_END = PLAYER_START + 36;
    private static final int SCALE = 1000;

    private final TileEtheriumStove stove;
    private int lastProgress = -1;
    private int clientProgress;

    public ContainerEtheriumStove(InventoryPlayer playerInv, TileEtheriumStove stove) {
        this.stove = stove;
        addSlotToContainer(new Slot(stove, StoveFurnace.INPUT, 56, 17));
        addSlotToContainer(new SlotFurnaceOutput(playerInv.player, stove, StoveFurnace.OUTPUT, 116, 35));
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlotToContainer(new Slot(playerInv, col, 8 + col * 18, 142));
        }
    }

    private int scaledProgress() {
        int total = stove.getFurnaceProgressTotal();
        int progress = stove.getFurnaceProgress();
        return total <= 0 || progress <= 0 ? 0 : (int) Math.min(SCALE, (long) progress * SCALE / total);
    }

    @Override
    public void addListener(IContainerListener listener) {
        super.addListener(listener);
        listener.sendWindowProperty(this, 0, scaledProgress());
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        int progress = scaledProgress();
        if (progress != lastProgress) {
            for (IContainerListener listener : listeners) {
                listener.sendWindowProperty(this, 0, progress);
            }
            lastProgress = progress;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int id, int data) {
        if (id == 0) {
            clientProgress = data;
        }
    }

    /** 客户端：箭头像素宽度 */
    @SideOnly(Side.CLIENT)
    public int getCookProgressScaled(int pixels) {
        return clientProgress * pixels / SCALE;
    }

    /** 双击收集不从输出槽拿，保证经验只在取出时结算 */
    @Override
    public boolean canMergeSlot(ItemStack stack, Slot slot) {
        return slot.slotNumber != SLOT_OUTPUT && super.canMergeSlot(stack, slot);
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return stove.isUsableByPlayer(player);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack copy = ItemStack.EMPTY;
        Slot slot = inventorySlots.get(index);
        if (slot == null || !slot.getHasStack()) {
            return copy;
        }
        ItemStack stack = slot.getStack();
        copy = stack.copy();
        if (index == SLOT_OUTPUT) {
            if (!mergeItemStack(stack, PLAYER_START, PLAYER_END, true)) {
                return ItemStack.EMPTY;
            }
            slot.onSlotChange(stack, copy);
        } else if (index != SLOT_INPUT) {
            if (StoveFurnace.isProcessable(stack)) {
                if (!mergeItemStack(stack, SLOT_INPUT, SLOT_INPUT + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index < PLAYER_START + 27) {
                if (!mergeItemStack(stack, PLAYER_START + 27, PLAYER_END, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!mergeItemStack(stack, PLAYER_START, PLAYER_START + 27, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!mergeItemStack(stack, PLAYER_START, PLAYER_END, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.putStack(ItemStack.EMPTY);
        } else {
            slot.onSlotChanged();
        }
        if (stack.getCount() == copy.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stack);
        return copy;
    }
}
