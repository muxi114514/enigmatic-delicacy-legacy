package net.mx.edelicacy.flora.soil;

import java.util.Random;

import com.wdcftgg.farmersdelightlegacy.common.block.BlockMushroomColony;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;

/**
 * 农夫乐事（及继承它的下界乐事）菌落在无尽沃土上的存活与生长，由 MixinFdMushroomColony 调用。
 * <p>对应 1.21 把无尽沃土加入 mushroom_colony_growable_on 标签：任意亮度可存活，随机刻 1/4 概率长一级。
 */
public final class ColonyHooks {

    private ColonyHooks() {
    }

    public static boolean isGrowBlock(IBlockState ground) {
        return Infinisoils.isInfinisoil(ground);
    }

    /** 菌落随机刻末尾调用：方块仍在且下方为无尽沃土时尝试生长 */
    public static void onColonyTick(Block self, World world, BlockPos pos, Random rand) {
        if (world.isRemote || !(self instanceof BlockMushroomColony)) {
            return;
        }
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() != self) {
            return;
        }
        BlockMushroomColony colony = (BlockMushroomColony) self;
        int age = state.getValue(BlockMushroomColony.AGE);
        if (age >= colony.getMaxAge() || !Infinisoils.isInfinisoil(world.getBlockState(pos.down()))) {
            return;
        }
        if (ForgeHooks.onCropsGrowPre(world, pos, state, rand.nextInt(4) == 0)) {
            world.setBlockState(pos, state.withProperty(BlockMushroomColony.AGE, age + 1), 2);
            ForgeHooks.onCropsGrowPost(world, pos, state, world.getBlockState(pos));
        }
    }
}
