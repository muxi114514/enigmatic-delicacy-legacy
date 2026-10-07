package net.mx.edelicacy.machine.stove;

import java.util.Random;

import com.wdcftgg.farmersdelightlegacy.common.recipe.CampfireCookingRecipe;
import com.wdcftgg.farmersdelightlegacy.common.recipe.manager.CampfireCookingRecipeManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** 以太炉灶顶部的 6 格烤架：用农夫乐事的营火配方烹饪，熟了弹出 */
final class StoveGrill {

    static final int SLOTS = 6;
    /** 每格相对方块中心的偏移（x, z），与农夫乐事炉灶一致 */
    static final float[][] OFFSETS = {
            {0.3F, 0.2F}, {0.0F, 0.2F}, {-0.3F, 0.2F},
            {0.3F, -0.2F}, {0.0F, -0.2F}, {-0.3F, -0.2F}
    };
    /** 熄灭时每 tick 回退的进度（1.21 为 3） */
    private static final int COOL_DOWN = 3;

    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
    private final int[] times = new int[SLOTS];
    private final int[] totals = new int[SLOTS];

    ItemStack get(int slot) {
        return items.get(slot);
    }

    int getTime(int slot) {
        return times[slot];
    }

    int getTotal(int slot) {
        return totals[slot];
    }

    boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    int nextEmptySlot() {
        for (int i = 0; i < SLOTS; i++) {
            if (items.get(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    /** 放入一个（从 input 拆出 1 个） */
    boolean add(ItemStack input, CampfireCookingRecipe recipe, int slot) {
        if (slot < 0 || slot >= SLOTS || !items.get(slot).isEmpty() || input.isEmpty()) {
            return false;
        }
        times[slot] = 0;
        totals[slot] = recipe.getCookingTime();
        items.set(slot, input.splitStack(1));
        return true;
    }

    /** 点燃时推进烹饪（修正 1.21 漏了累加进度、烤架永远不熟的问题）；返回物品是否变化 */
    boolean cook(World world, BlockPos pos, int speed) {
        boolean changed = false;
        for (int i = 0; i < SLOTS; i++) {
            ItemStack stack = items.get(i);
            if (stack.isEmpty()) {
                continue;
            }
            times[i] += speed;
            if (times[i] < totals[i]) {
                continue;
            }
            CampfireCookingRecipe recipe = CampfireCookingRecipeManager.findRecipe(stack);
            if (recipe != null) {
                ItemStack result = recipe.getResultStack();
                if (!result.isEmpty()) {
                    EntityItem drop = new EntityItem(world, pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, result);
                    drop.motionX = world.rand.nextGaussian() * 0.01D;
                    drop.motionY = 0.1D;
                    drop.motionZ = world.rand.nextGaussian() * 0.01D;
                    world.spawnEntity(drop);
                }
            }
            clear(i);
            changed = true;
        }
        return changed;
    }

    void cool() {
        for (int i = 0; i < SLOTS; i++) {
            if (times[i] > 0) {
                times[i] = Math.max(0, Math.min(totals[i], times[i] - COOL_DOWN));
            }
        }
    }

    void dropAll(World world, BlockPos pos) {
        for (int i = 0; i < SLOTS; i++) {
            ItemStack stack = items.get(i);
            if (!stack.isEmpty()) {
                InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), stack);
                clear(i);
            }
        }
    }

    private void clear(int slot) {
        items.set(slot, ItemStack.EMPTY);
        times[slot] = 0;
        totals[slot] = 0;
    }

    /** 客户端：烤架冒烟 */
    void spawnSmoke(World world, BlockPos pos, EnumFacing facing, Random rand) {
        for (int i = 0; i < SLOTS; i++) {
            if (items.get(i).isEmpty() || rand.nextFloat() >= 0.2F) {
                continue;
            }
            float xOffset = OFFSETS[i][0];
            float zOffset = OFFSETS[i][1];
            if (facing.getAxis() == EnumFacing.Axis.Z) {
                float tmp = xOffset;
                xOffset = zOffset;
                zOffset = tmp;
            }
            EnumFacing side = facing.rotateY();
            double x = pos.getX() + 0.5D - facing.getFrontOffsetX() * zOffset + side.getFrontOffsetX() * xOffset;
            double y = pos.getY() + 1.0D;
            double z = pos.getZ() + 0.5D - facing.getFrontOffsetZ() * zOffset + side.getFrontOffsetZ() * xOffset;
            for (int k = 0; k < 3; k++) {
                world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x, y, z, 0.0D, 5.0E-4D, 0.0D);
            }
        }
    }

    void write(NBTTagCompound tag) {
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < SLOTS; i++) {
            ItemStack stack = items.get(i);
            if (!stack.isEmpty()) {
                NBTTagCompound entry = stack.writeToNBT(new NBTTagCompound());
                entry.setByte("Slot", (byte) i);
                list.appendTag(entry);
            }
        }
        tag.setTag("Grill", list);
        tag.setIntArray("CookingTimes", times.clone());
        tag.setIntArray("CookingTotalTimes", totals.clone());
    }

    void read(NBTTagCompound tag) {
        for (int i = 0; i < SLOTS; i++) {
            items.set(i, ItemStack.EMPTY);
        }
        NBTTagList list = tag.getTagList("Grill", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound entry = list.getCompoundTagAt(i);
            int slot = entry.getByte("Slot") & 255;
            if (slot < SLOTS) {
                items.set(slot, new ItemStack(entry));
            }
        }
        int[] loadedTimes = tag.getIntArray("CookingTimes");
        int[] loadedTotals = tag.getIntArray("CookingTotalTimes");
        java.util.Arrays.fill(times, 0);
        java.util.Arrays.fill(totals, 0);
        System.arraycopy(loadedTimes, 0, times, 0, Math.min(SLOTS, loadedTimes.length));
        System.arraycopy(loadedTotals, 0, totals, 0, Math.min(SLOTS, loadedTotals.length));
    }
}
