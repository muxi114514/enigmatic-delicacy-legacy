package net.mx.edelicacy.flora.soil;

import java.util.Random;

import com.wdcftgg.farmersdelightlegacy.common.block.BlockOrganicCompost;
import com.wdcftgg.farmersdelightlegacy.common.registry.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.mx.edelicacy.compat.ModCompat;
import net.mx.edelicacy.flora.compat.NdSoulCompostCompat;
import net.mx.edelicacy.registry.DelicacyBlocks;
import net.mx.edelicacy.util.WorldHelper;

/**
 * 无尽沃土随机刻对周围 ±3（水平）/±1（竖直）范围的影响：
 * 加速有机堆肥（及下界乐事灵魂堆肥），1/48 概率同化农夫乐事沃土。
 * <p>扫描只读已加载区块（按区块缓存），任何副作用前再确认作用半径内区块全部已加载。
 */
public final class SoilNeighborhood {

    private static final int RADIUS_XZ = 3;
    private static final int RADIUS_Y = 1;
    private static final int ASSIMILATE_CHANCE = 48;
    private static final int EXTRA_COMPOST_TICKS = 3;
    /** 堆肥自身会扫描 ±1 并改写方块，守卫半径留一格余量 */
    private static final int SIDE_EFFECT_GUARD = 2;

    private SoilNeighborhood() {
    }

    public static void randomTick(World world, BlockPos center, Random rand) {
        LocalChunkCache chunks = new LocalChunkCache(world);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -RADIUS_XZ; dx <= RADIUS_XZ; dx++) {
            for (int dz = -RADIUS_XZ; dz <= RADIUS_XZ; dz++) {
                for (int dy = -RADIUS_Y; dy <= RADIUS_Y; dy++) {
                    cursor.setPos(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                    IBlockState state = chunks.getBlockState(cursor);
                    if (state != null) {
                        affect(world, cursor, state, rand);
                    }
                }
            }
        }
    }

    /** cursor 为可变坐标，只在真正产生副作用时转成不可变坐标 */
    private static void affect(World world, BlockPos.MutableBlockPos cursor, IBlockState state, Random rand) {
        Block block = state.getBlock();
        if (block instanceof BlockOrganicCompost) {
            tickExtra(world, cursor.toImmutable(), rand, BlockOrganicCompost.class);
        } else if (block == ModBlocks.RICH_SOIL) {
            if (rand.nextInt(ASSIMILATE_CHANCE) == 0 && WorldHelper.isAreaLoaded(world, cursor, 1)) {
                world.setBlockState(cursor.toImmutable(), DelicacyBlocks.INFINISOIL.getDefaultState(), 3);
            }
        } else if (block == ModBlocks.RICH_SOIL_FARMLAND) {
            if (rand.nextInt(ASSIMILATE_CHANCE) == 0 && WorldHelper.isAreaLoaded(world, cursor, 1)) {
                // 1.21 的耕地分支用 1/50 且丢失湿度，这里与沃土分支统一为 1/48 并保留湿度
                world.setBlockState(cursor.toImmutable(), DelicacyBlocks.INFINISOIL_FARMLAND.getDefaultState()
                        .withProperty(BlockFarmland.MOISTURE, state.getValue(BlockFarmland.MOISTURE)), 3);
            }
        } else if (ModCompat.nethersDelight && NdSoulCompostCompat.isSoulCompost(block)) {
            tickExtra(world, cursor.toImmutable(), rand, block.getClass());
        }
    }

    /** 额外 3 次随机刻；每次重新读取状态（1.21 复用旧状态导致只生效一次） */
    private static void tickExtra(World world, BlockPos pos, Random rand, Class<?> type) {
        if (!WorldHelper.isAreaLoaded(world, pos, SIDE_EFFECT_GUARD)) {
            return;
        }
        for (int i = 0; i < EXTRA_COMPOST_TICKS; i++) {
            IBlockState current = world.getBlockState(pos);
            Block block = current.getBlock();
            if (!type.isInstance(block) || !block.getTickRandomly()) {
                return;
            }
            block.randomTick(world, pos, current, rand);
        }
    }
}
