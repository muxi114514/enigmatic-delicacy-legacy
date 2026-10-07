package net.mx.edelicacy.flora.tree;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraftforge.event.terraingen.TerrainGen;
import net.mx.edelicacy.util.WorldHelper;

/**
 * 树苗长树（1.21 TreeGrower）：2×2 树苗 → 大型树，单棵 → 直干树，沃土上 → 小型树。
 * 树的水平伸展最远约 9 格，长树前确认该范围的区块全部已加载，否则本次放弃、等下次随机刻。
 */
final class AstralTreeGrower {

    private static final int GUARD_RADIUS = 12;

    private AstralTreeGrower() {
    }

    static void growSmall(World world, BlockPos pos, IBlockState sapling, Random rand) {
        place(world, rand, AstralStraightTree.SMALL, pos, sapling, pos);
    }

    static void grow(World world, BlockPos pos, IBlockState sapling, Random rand) {
        Block block = sapling.getBlock();
        for (int i = 0; i >= -1; i--) {
            for (int j = 0; j >= -1; j--) {
                BlockPos corner = pos.add(i, 0, j);
                if (isTwoByTwo(world, corner, block)) {
                    place(world, rand, AstralFancyTree.INSTANCE, corner, sapling,
                            corner, corner.east(), corner.south(), corner.south().east());
                    return;
                }
            }
        }
        place(world, rand, AstralStraightTree.NORMAL, pos, sapling, pos);
    }

    private static boolean isTwoByTwo(World world, BlockPos corner, Block block) {
        return world.getBlockState(corner).getBlock() == block && world.getBlockState(corner.east()).getBlock() == block
                && world.getBlockState(corner.south()).getBlock() == block
                && world.getBlockState(corner.south().east()).getBlock() == block;
    }

    /** 清掉树苗后生成，失败则把树苗原样放回 */
    private static void place(World world, Random rand, WorldGenAbstractTree tree, BlockPos origin, IBlockState sapling,
                              BlockPos... saplings) {
        if (!WorldHelper.isAreaLoaded(world, origin, GUARD_RADIUS) || !TerrainGen.saplingGrowTree(world, rand, origin)) {
            return;
        }
        IBlockState[] previous = new IBlockState[saplings.length];
        for (int i = 0; i < saplings.length; i++) {
            previous[i] = world.getBlockState(saplings[i]);
            world.setBlockState(saplings[i], Blocks.AIR.getDefaultState(), 4);
        }
        if (!tree.generate(world, rand, origin)) {
            for (int i = 0; i < saplings.length; i++) {
                world.setBlockState(saplings[i], previous[i].getBlock() == sapling.getBlock() ? previous[i] : sapling, 4);
            }
        }
    }
}
