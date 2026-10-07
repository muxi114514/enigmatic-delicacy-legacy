package net.mx.edelicacy.util;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

/**
 * 区块访问守卫：tick / 事件里访问可能跨区块的坐标时，只读内存中已加载的区块，
 * 不触发同步加载或生成（否则会卡住服务端主线程）。
 */
public final class WorldHelper {

    private WorldHelper() {
    }

    /** 已加载的区块；未加载返回 null（不加载、不生成） */
    @Nullable
    public static Chunk getLoadedChunk(World world, int chunkX, int chunkZ) {
        return world.getChunkProvider().getLoadedChunk(chunkX, chunkZ);
    }

    /** 坐标所在区块已加载时返回方块状态，否则 null */
    @Nullable
    public static IBlockState getBlockStateIfLoaded(World world, BlockPos pos) {
        Chunk chunk = getLoadedChunk(world, pos.getX() >> 4, pos.getZ() >> 4);
        return chunk == null ? null : chunk.getBlockState(pos);
    }

    /** 以 center 为中心、半径 radius 覆盖的所有区块都已加载（副作用前用它守卫） */
    public static boolean isAreaLoaded(World world, BlockPos center, int radius) {
        int minX = (center.getX() - radius) >> 4;
        int maxX = (center.getX() + radius) >> 4;
        int minZ = (center.getZ() - radius) >> 4;
        int maxZ = (center.getZ() + radius) >> 4;
        for (int cx = minX; cx <= maxX; cx++) {
            for (int cz = minZ; cz <= maxZ; cz++) {
                if (getLoadedChunk(world, cx, cz) == null) {
                    return false;
                }
            }
        }
        return true;
    }
}
