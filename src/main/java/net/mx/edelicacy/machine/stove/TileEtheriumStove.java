package net.mx.edelicacy.machine.stove;

import javax.annotation.Nullable;

import com.wdcftgg.farmersdelightlegacy.common.block.BlockStove;
import com.wdcftgg.farmersdelightlegacy.common.recipe.CampfireCookingRecipe;
import com.wdcftgg.farmersdelightlegacy.common.tile.TileEntityCookingPot;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import net.mx.edelicacy.machine.MachineConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 以太炉灶方块实体：顶部 6 格烤架（营火配方）+ 无燃料熔炉（原版熔炉配方 / 以太装备修复）+ 加速正上方的烹饪锅。
 * <p>IInventory 只暴露熔炉的 2 格（与 1.21 一致），上/侧面进输入、底面出输出。
 * 下列 getter 供 Hwyla 等外部插件读取；客户端副本只同步了物品，进度要在服务端读（NBT 键见 {@link #writeToNBT}）。
 */
public class TileEtheriumStove extends TileEntity implements ITickable, ISidedInventory {

    private static final Logger LOG = LogManager.getLogger("enigmaticdelicacy");
    private static final int[] SLOTS_DOWN = {StoveFurnace.OUTPUT};
    private static final int[] SLOTS_OTHER = {StoveFurnace.INPUT};
    /** 烤架区域：相对正上方方块原点的底部 1 像素（与 1.21 相同） */
    private static final AxisAlignedBB GRILL_AREA = new AxisAlignedBB(3 / 16D, 0, 3 / 16D, 13 / 16D, 1 / 16D, 13 / 16D);

    private final StoveGrill grill = new StoveGrill();
    private final StoveFurnace furnace = new StoveFurnace();
    private final IItemHandler[] sidedHandlers = new IItemHandler[EnumFacing.values().length];
    private boolean potErrorLogged;

    // ---------------- 公开读取接口（Hwyla 等） ----------------

    public static final int GRILL_SLOTS = StoveGrill.SLOTS;

    public boolean isLit() {
        IBlockState state = world == null ? null : world.getBlockState(pos);
        return state != null && state.getBlock() instanceof BlockStove && state.getValue(BlockStove.LIT);
    }

    public ItemStack getFurnaceInput() {
        return furnace.stacks().get(StoveFurnace.INPUT);
    }

    public ItemStack getFurnaceOutput() {
        return furnace.stacks().get(StoveFurnace.OUTPUT);
    }

    /** 当前进度与本轮总进度（总进度为 0 表示没在处理） */
    public int getFurnaceProgress() {
        return furnace.getProgress();
    }

    public int getFurnaceProgressTotal() {
        return furnace.getTotal();
    }

    public ItemStack getGrillStack(int slot) {
        return grill.get(slot);
    }

    public int getGrillCookTime(int slot) {
        return grill.getTime(slot);
    }

    public int getGrillCookTimeTotal(int slot) {
        return grill.getTotal(slot);
    }

    // ---------------- 烤架 ----------------

    public int getNextEmptyGrillSlot() {
        return grill.nextEmptySlot();
    }

    /** 服务端：放上烤架，从 input 拆 1 个 */
    public boolean addToGrill(ItemStack input, CampfireCookingRecipe recipe, int slot) {
        if (grill.add(input, recipe, slot)) {
            syncToClient();
            return true;
        }
        return false;
    }

    /** 正上方方块挡住了烤架（同一区块，安全读取） */
    public boolean isGrillBlocked() {
        if (world == null) {
            return false;
        }
        BlockPos above = pos.up();
        IBlockState state = world.getBlockState(above);
        AxisAlignedBB box = state.getCollisionBoundingBox(world, above);
        return box != null && box != Block.NULL_AABB && box.intersects(GRILL_AREA);
    }

    /** 方块被破坏时掉落全部内容 */
    public void dropContents() {
        grill.dropAll(world, pos);
        net.minecraft.inventory.InventoryHelper.dropInventoryItems(world, pos, this);
    }

    // ---------------- tick ----------------

    @Override
    public void update() {
        if (world == null) {
            return;
        }
        IBlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof BlockStove)) {
            return;
        }
        boolean lit = state.getValue(BlockStove.LIT);
        if (world.isRemote) {
            if (lit) {
                grill.spawnSmoke(world, pos, state.getValue(BlockStove.FACING), world.rand);
            }
            return;
        }
        if (isGrillBlocked()) {
            if (!grill.isEmpty()) {
                grill.dropAll(world, pos);
                syncToClient();
            }
        } else if (lit) {
            if (grill.cook(world, pos, MachineConfig.grillProgressPerTick)) {
                syncToClient();
            }
        } else {
            grill.cool();
        }
        if (lit) {
            boostCookingPot();
            if (furnace.tick(MachineConfig.furnaceProgressPerTick)) {
                markDirty();
            }
        }
    }

    /** 额外驱动一次正上方的烹饪锅（同一区块，不跨区块） */
    private void boostCookingPot() {
        if (!MachineConfig.boostCookingPot) {
            return;
        }
        TileEntity above = world.getTileEntity(pos.up());
        if (!(above instanceof TileEntityCookingPot) || above.isInvalid()) {
            return;
        }
        try {
            // 经 ITickable 调用：libs 里的农夫乐事原始 jar 在编译类路径上，方法名是 SRG
            ((ITickable) above).update();
        } catch (RuntimeException e) {
            if (!potErrorLogged) {
                potErrorLogged = true;
                LOG.error("Etherium Stove failed to boost the cooking pot at {}", pos.up(), e);
            }
        }
    }

    // ---------------- 同步与存档 ----------------

    private void syncToClient() {
        markDirty();
        if (world != null) {
            IBlockState state = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, state, state, 3);
        }
    }

    /**
     * NBT 键：Grill（烤架物品列表）、CookingTimes / CookingTotalTimes、Furnace{Input, Output}、FurnaceTime / FurnaceTimeTotal。
     */
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        grill.write(compound);
        furnace.write(compound);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        grill.read(compound);
        furnace.read(compound);
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, 0, getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        readFromNBT(pkt.getNbtCompound());
    }

    @Override
    public boolean shouldRefresh(net.minecraft.world.World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }

    // ---------------- 自动化 ----------------

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            EnumFacing side = facing == null ? EnumFacing.UP : facing;
            IItemHandler handler = sidedHandlers[side.ordinal()];
            if (handler == null) {
                handler = new SidedInvWrapper(this, side);
                sidedHandlers[side.ordinal()] = handler;
            }
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(handler);
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public int[] getSlotsForFace(EnumFacing side) {
        return side == EnumFacing.DOWN ? SLOTS_DOWN : SLOTS_OTHER;
    }

    @Override
    public boolean canInsertItem(int index, ItemStack stack, EnumFacing direction) {
        return isItemValidForSlot(index, stack);
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
        return index == StoveFurnace.OUTPUT;
    }

    // ---------------- IInventory（熔炉 2 格） ----------------

    @Override
    public int getSizeInventory() {
        return 2;
    }

    /** 全空才算空（修正 1.21 写反的 isEmpty） */
    @Override
    public boolean isEmpty() {
        for (ItemStack stack : furnace.stacks()) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return index >= 0 && index < 2 ? furnace.stacks().get(index) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        ItemStack result = ItemStackHelper.getAndSplit(furnace.stacks(), index, count);
        if (!result.isEmpty()) {
            if (index == StoveFurnace.INPUT) {
                furnace.resetProgress();
            }
            markDirty();
        }
        return result;
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        ItemStack result = ItemStackHelper.getAndRemove(furnace.stacks(), index);
        if (index == StoveFurnace.INPUT) {
            furnace.resetProgress();
        }
        markDirty();
        return result;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        if (index < 0 || index >= 2) {
            return;
        }
        ItemStack old = furnace.stacks().get(index);
        boolean same = !stack.isEmpty() && stack.isItemEqual(old) && ItemStack.areItemStackTagsEqual(stack, old);
        furnace.stacks().set(index, stack);
        if (stack.getCount() > getInventoryStackLimit()) {
            stack.setCount(getInventoryStackLimit());
        }
        if (index == StoveFurnace.INPUT && !same) {
            furnace.resetProgress();
        }
        markDirty();
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return world != null && world.getTileEntity(pos) == this
                && player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void openInventory(EntityPlayer player) {
    }

    @Override
    public void closeInventory(EntityPlayer player) {
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return index == StoveFurnace.INPUT && StoveFurnace.isProcessable(stack);
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {
    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {
        furnace.stacks().clear();
        furnace.resetProgress();
    }

    @Override
    public String getName() {
        return "gui.enigmaticdelicacy.etherium_stove";
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TextComponentTranslation(getName());
    }
}
