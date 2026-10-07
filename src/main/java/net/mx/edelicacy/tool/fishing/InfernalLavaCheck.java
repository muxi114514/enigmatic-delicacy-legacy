package net.mx.edelicacy.tool.fishing;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.mx.edelicacy.util.WorldHelper;

/**
 * 熔岩浮漂的「开阔熔岩」判定（对应原版开阔水域）：以浮漂为中心 5×5，自下而上 4 层，
 * 每层必须整层同类，且只能由「熔岩源」过渡到「空气」。未加载的区块一律视为不合格，不触发加载。
 */
final class InfernalLavaCheck {

    private enum Layer {
        ABOVE_LAVA, INSIDE_LAVA, INVALID
    }

    private InfernalLavaCheck() {
    }

    static boolean isOpenLava(World world, BlockPos pos) {
        Layer type = Layer.INVALID;
        for (int dy = -1; dy <= 2; dy++) {
            Layer layer = layerType(world, pos.getX(), pos.getY() + dy, pos.getZ());
            switch (layer) {
                case ABOVE_LAVA:
                    if (type == Layer.INVALID) {
                        return false;
                    }
                    break;
                case INSIDE_LAVA:
                    if (type == Layer.ABOVE_LAVA) {
                        return false;
                    }
                    break;
                default:
                    return false;
            }
            type = layer;
        }
        return true;
    }

    private static Layer layerType(World world, int cx, int y, int cz) {
        Layer result = null;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                cursor.setPos(cx + dx, y, cz + dz);
                Layer block = blockType(world, cursor);
                if (result == null) {
                    result = block;
                } else if (result != block) {
                    return Layer.INVALID;
                }
                if (result == Layer.INVALID) {
                    return Layer.INVALID;
                }
            }
        }
        return result == null ? Layer.INVALID : result;
    }

    private static Layer blockType(World world, BlockPos pos) {
        IBlockState state = WorldHelper.getBlockStateIfLoaded(world, pos);
        if (state == null) {
            return Layer.INVALID;
        }
        if (state.getBlock().isAir(state, world, pos)) {
            return Layer.ABOVE_LAVA;
        }
        boolean lavaSource = state.getMaterial() == Material.LAVA && state.getBlock() instanceof BlockLiquid
                && state.getValue(BlockLiquid.LEVEL) == 0;
        return lavaSource && state.getCollisionBoundingBox(world, pos) == Block.NULL_AABB ? Layer.INSIDE_LAVA : Layer.INVALID;
    }
}
