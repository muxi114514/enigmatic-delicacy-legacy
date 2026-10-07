package net.mx.edelicacy.flora.soil;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.mx.edelicacy.util.WorldHelper;

/**
 * 小范围扫描用的区块缓存（单次扫描内使用，不跨 tick 保存）：
 * 只读内存中已加载的区块，未加载返回 null，绝不触发区块加载或生成。
 */
final class LocalChunkCache {

    private static final int SLOTS = 4;

    private final World world;
    private final int[] keysX = new int[SLOTS];
    private final int[] keysZ = new int[SLOTS];
    private final Chunk[] chunks = new Chunk[SLOTS];
    private int size;

    LocalChunkCache(World world) {
        this.world = world;
    }

    @Nullable
    Chunk get(int chunkX, int chunkZ) {
        for (int i = 0; i < size; i++) {
            if (keysX[i] == chunkX && keysZ[i] == chunkZ) {
                return chunks[i];
            }
        }
        Chunk chunk = WorldHelper.getLoadedChunk(world, chunkX, chunkZ);
        if (size < SLOTS) {
            keysX[size] = chunkX;
            keysZ[size] = chunkZ;
            chunks[size] = chunk;
            size++;
        }
        return chunk;
    }

    /** 坐标所在区块已加载时返回方块状态，否则 null */
    @Nullable
    IBlockState getBlockState(BlockPos pos) {
        if (pos.getY() < 0 || pos.getY() >= world.getHeight()) {
            return null;
        }
        Chunk chunk = get(pos.getX() >> 4, pos.getZ() >> 4);
        return chunk == null ? null : chunk.getBlockState(pos);
    }
}
