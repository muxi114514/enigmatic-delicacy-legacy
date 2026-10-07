package net.mx.edelicacy.machine.pan;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.SlotItemHandler;
import net.mx.edelicacy.machine.recipe.AbyssalCookingRecipe;
import net.mx.edelicacy.machine.recipe.AbyssalCookingRecipes;

/**
 * 饕餮之锅的禁忌转化界面：左上展示锅本身，输入 (44,40) → 产物 (116,40)，取走产物时扣禁忌值。
 * <p>产物只在服务端计算；取出前再核对一次配方（多人同时打开时防止同一 tick 内白拿）。
 * 禁忌值拆成高低 16 位两条窗口属性同步（原版窗口属性按 short 发送）。
 */
public class ContainerVoraciousPan extends Container {

    public static final int SLOT_PAN = 0;
    public static final int SLOT_INPUT = 1;
    public static final int SLOT_RESULT = 2;
    public static final int PLAYER_START = 3;
    private static final int PLAYER_END = PLAYER_START + 36;

    private final TileVoraciousPan pan;
    private final EntityPlayer player;
    private final InventoryBasic display = new InventoryBasic("pan", false, 1);
    private final InventoryBasic result = new InventoryBasic("result", false, 1);
    private ItemStack lastInput = ItemStack.EMPTY;
    private int lastPoints = -1;
    private int clientLow;
    private int clientHigh;

    public ContainerVoraciousPan(InventoryPlayer playerInv, TileVoraciousPan pan) {
        this.pan = pan;
        this.player = playerInv.player;
        addSlotToContainer(new Slot(display, 0, 8, 8) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return false;
            }

            @Override
            public boolean canTakeStack(EntityPlayer playerIn) {
                return false;
            }
        });
        addSlotToContainer(new SlotItemHandler(pan.getInput(), 0, 44, 40));
        addSlotToContainer(new ResultSlot(result, 0, 116, 40));
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlotToContainer(new Slot(playerInv, col, 8 + col * 18, 142));
        }
    }

    /** 服务端：按当前输入与禁忌值重算产物 */
    private void updateResult() {
        if (player.world.isRemote) {
            return;
        }
        ItemStack input = pan.getInput().getStackInSlot(0);
        AbyssalCookingRecipe recipe = AbyssalCookingRecipes.find(input, pan.getPoints());
        result.setInventorySlotContents(0, recipe == null ? ItemStack.EMPTY : recipe.getResult());
        if (pan.getPoints() != lastPoints) {
            display.setInventorySlotContents(0, pan.getPanDisplay());
        }
        lastInput = input.copy();
        lastPoints = pan.getPoints();
    }

    @Override
    public void addListener(IContainerListener listener) {
        updateResult();
        super.addListener(listener);
        sendPoints(listener, pan.getPoints());
    }

    private void sendPoints(IContainerListener listener, int points) {
        listener.sendWindowProperty(this, 0, points & 0xFFFF);
        listener.sendWindowProperty(this, 1, points >>> 16);
    }

    @Override
    public void detectAndSendChanges() {
        ItemStack input = pan.getInput().getStackInSlot(0);
        int points = pan.getPoints();
        boolean pointsChanged = points != lastPoints;
        if (pointsChanged || !ItemStack.areItemStacksEqual(input, lastInput)) {
            updateResult();
        }
        super.detectAndSendChanges();
        if (pointsChanged) {
            for (IContainerListener listener : listeners) {
                sendPoints(listener, points);
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int id, int data) {
        if (id == 0) {
            clientLow = data & 0xFFFF;
        } else if (id == 1) {
            clientHigh = data & 0xFFFF;
        }
    }

    /** 客户端：同步来的禁忌值 */
    @SideOnly(Side.CLIENT)
    public int getClientPoints() {
        return clientLow | clientHigh << 16;
    }

    /** 双击收集不许从产物槽、展示槽拿（否则绕过 onTake 不扣禁忌值） */
    @Override
    public boolean canMergeSlot(ItemStack stack, Slot slot) {
        return slot.inventory != result && slot.inventory != display && super.canMergeSlot(stack, slot);
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return pan.isUsableByPlayer(playerIn);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        ItemStack copy = ItemStack.EMPTY;
        Slot slot = inventorySlots.get(index);
        if (slot == null || !slot.getHasStack() || index == SLOT_PAN) {
            return copy;
        }
        ItemStack stack = slot.getStack();
        copy = stack.copy();
        if (index == SLOT_RESULT) {
            if (!slot.canTakeStack(playerIn) || !mergeItemStack(stack, PLAYER_START, PLAYER_END, true)) {
                return ItemStack.EMPTY;
            }
            slot.onSlotChange(stack, copy);
        } else if (index >= PLAYER_START) {
            if (!mergeItemStack(stack, SLOT_INPUT, SLOT_INPUT + 1, false)) {
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
        slot.onTake(playerIn, stack);
        return copy;
    }

    /** 产物槽：只出不进，取出时结算 */
    private final class ResultSlot extends Slot {

        ResultSlot(InventoryBasic inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return false;
        }

        @Override
        public boolean canTakeStack(EntityPlayer playerIn) {
            if (!getHasStack()) {
                return false;
            }
            if (playerIn.world.isRemote) {
                return true;
            }
            AbyssalCookingRecipe recipe = AbyssalCookingRecipes.find(pan.getInput().getStackInSlot(0), pan.getPoints());
            return recipe != null && ItemStack.areItemStacksEqual(recipe.getResult(), getStack());
        }

        @Override
        public ItemStack onTake(EntityPlayer playerIn, ItemStack stack) {
            if (!playerIn.world.isRemote) {
                consumeIngredients(playerIn);
                updateResult();
            }
            return super.onTake(playerIn, stack);
        }

        private void consumeIngredients(EntityPlayer playerIn) {
            ItemStack input = pan.getInput().getStackInSlot(0);
            AbyssalCookingRecipe recipe = AbyssalCookingRecipes.find(input, pan.getPoints());
            if (recipe == null) {
                return;
            }
            pan.consumePoints(recipe.getCost());
            ItemStack single = input.copy();
            single.setCount(1);
            ItemStack remainder = ForgeHooks.getContainerItem(single);
            pan.getInput().extractItem(0, 1, false);
            if (remainder.isEmpty()) {
                return;
            }
            // 容器返还：能放回输入槽就放回，否则给玩家（修正 1.21 背包满时把返还物吞掉）
            ItemStack left = pan.getInput().insertItem(0, remainder, false);
            if (!left.isEmpty()) {
                ItemHandlerHelper.giveItemToPlayer(playerIn, left);
            }
        }
    }
}
