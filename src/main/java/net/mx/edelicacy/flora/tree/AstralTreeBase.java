package net.mx.edelicacy.flora.tree;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockLog;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.mx.edelicacy.registry.DelicacyBlocks;

/**
 * 星辰树生成器公共部分（对应 1.21 TreeFeature 的放置规则）：
 * 只替换空气/树叶/可替换植物；树叶 11:1 普通与开花；不改动树下的土壤。
 * <p>调用方（AstralTreeGrower）负责先确认整棵树覆盖的区块均已加载。
 */
abstract class AstralTreeBase extends WorldGenAbstractTree {

    /** 1.21 WeightedStateProvider：开花 1 / 普通 11 */
    private static final int LEAF_WEIGHT_TOTAL = 12;

    AstralTreeBase() {
        super(true);
    }

    /** 1.21 TreeFeature.validTreePos：空气或可被树替换的方块（含树叶、草、水，不含岩浆） */
    static boolean canReplace(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        return block.isAir(state, world, pos) || block.isLeaves(state, world, pos)
                || (state.getMaterial() != Material.LAVA && block.isReplaceable(world, pos));
    }

    /** 1.21 TrunkPlacer.isFree：可替换或已是原木 */
    static boolean isFree(World world, BlockPos pos) {
        return canReplace(world, pos) || world.getBlockState(pos).getBlock().isWood(world, pos);
    }

    static boolean withinHeight(World world, BlockPos pos, int height) {
        return pos.getY() >= 1 && pos.getY() + height + 1 <= world.getHeight();
    }

    void placeLog(World world, BlockPos pos, BlockLog.EnumAxis axis) {
        if (canReplace(world, pos)) {
            setBlockAndNotifyAdequately(world, pos, DelicacyBlocks.ASTRAL_LOG.getDefaultState().withProperty(BlockLog.LOG_AXIS, axis));
        }
    }

    void placeLeaf(World world, BlockPos pos, Random rand) {
        if (canReplace(world, pos)) {
            Block leaves = rand.nextInt(LEAF_WEIGHT_TOTAL) == 0 ? DelicacyBlocks.BLOSSOMING_ASTRAL_LEAVES : DelicacyBlocks.ASTRAL_LEAVES;
            setBlockAndNotifyAdequately(world, pos, leaves.getDefaultState()
                    .withProperty(BlockLeaves.DECAYABLE, true).withProperty(BlockLeaves.CHECK_DECAY, false));
        }
    }
}
