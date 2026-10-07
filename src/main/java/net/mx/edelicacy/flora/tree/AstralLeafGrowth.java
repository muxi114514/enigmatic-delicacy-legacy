package net.mx.edelicacy.flora.tree;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.mx.edelicacy.registry.DelicacyBlocks;
import net.mx.edelicacy.util.WorldHelper;

/**
 * 自然星辰树叶的随机刻：普通叶 1/25 开花；开花叶 1/20 凋谢，并在下方结果或给下方叶子/果实额外随机刻。
 * <p>只读写本格与正下方一格；因放置方块会通知邻居，执行前确认 ±1 范围区块已加载。
 */
final class AstralLeafGrowth {

    private static final int BLOSSOM_CHANCE = 25;
    private static final int WITHER_CHANCE = 20;
    private static final int LEAVES_BELOW_TICKS = 5;
    private static final int FRUIT_BELOW_TICKS = 4;

    private AstralLeafGrowth() {
    }

    static void tick(World world, BlockPos pos, IBlockState state, Random rand, boolean blossoming) {
        // 先掷概率再查区块（换方块会通知相邻方块，周围区块未全部加载时跳过）
        if (!blossoming) {
            if (rand.nextInt(BLOSSOM_CHANCE) == 0 && WorldHelper.isAreaLoaded(world, pos, 1)) {
                swap(world, pos, state, DelicacyBlocks.BLOSSOMING_ASTRAL_LEAVES);
            }
            return;
        }
        if (rand.nextInt(WITHER_CHANCE) != 0 || !WorldHelper.isAreaLoaded(world, pos, 1)) {
            return;
        }
        BlockPos below = pos.down();
        IBlockState belowState = world.getBlockState(below);
        swap(world, pos, state, DelicacyBlocks.ASTRAL_LEAVES);
        if (world.isAirBlock(below)) {
            world.setBlockState(below, DelicacyBlocks.ASTRAL_FRUIT.getDefaultState(), 3);
        } else if (belowState.getBlock() instanceof AstralLeavesBlock) {
            tickBelow(world, below, rand, LEAVES_BELOW_TICKS, AstralLeavesBlock.class);
        } else if (belowState.getBlock() instanceof AstralFruitBlock && !AstralFruitBlock.isMature(belowState)) {
            tickBelow(world, below, rand, FRUIT_BELOW_TICKS, AstralFruitBlock.class);
        }
    }

    /** 换成另一种星辰树叶，保留凋落相关属性 */
    private static void swap(World world, BlockPos pos, IBlockState from, Block to) {
        world.setBlockState(pos, to.getDefaultState()
                .withProperty(BlockLeaves.DECAYABLE, from.getValue(BlockLeaves.DECAYABLE))
                .withProperty(BlockLeaves.CHECK_DECAY, from.getValue(BlockLeaves.CHECK_DECAY)), 3);
    }

    /** 额外随机刻，每次重新读取状态（1.21 复用旧状态，第二次起基本无效） */
    private static void tickBelow(World world, BlockPos pos, Random rand, int times, Class<? extends Block> type) {
        for (int i = 0; i < times; i++) {
            IBlockState current = world.getBlockState(pos);
            if (!type.isInstance(current.getBlock())) {
                return;
            }
            current.getBlock().randomTick(world, pos, current, rand);
        }
    }
}
