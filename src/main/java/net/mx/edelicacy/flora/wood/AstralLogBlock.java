package net.mx.edelicacy.flora.wood;

import net.minecraft.block.BlockLog;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/**
 * 星辰原木/木头/去皮原木/去皮木头：亮度 8，继承原版原木以便树叶把它当作支撑（不会凋落）。
 */
public class AstralLogBlock extends BlockLog {

    public AstralLogBlock() {
        setHardness(3.5F);
        setResistance(3.5F * 5.0F / 3.0F);
        setLightLevel(8.0F / 15.0F);
        setDefaultState(blockState.getBaseState().withProperty(LOG_AXIS, EnumAxis.Y));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, LOG_AXIS);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        switch (meta & 12) {
            case 0:
                return getDefaultState().withProperty(LOG_AXIS, EnumAxis.Y);
            case 4:
                return getDefaultState().withProperty(LOG_AXIS, EnumAxis.X);
            case 8:
                return getDefaultState().withProperty(LOG_AXIS, EnumAxis.Z);
            default:
                return getDefaultState().withProperty(LOG_AXIS, EnumAxis.NONE);
        }
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        switch (state.getValue(LOG_AXIS)) {
            case X:
                return 4;
            case Z:
                return 8;
            case NONE:
                return 12;
            default:
                return 0;
        }
    }

    @Override
    public MapColor getMapColor(IBlockState state, IBlockAccess world, BlockPos pos) {
        return MapColor.PURPLE;
    }
}
