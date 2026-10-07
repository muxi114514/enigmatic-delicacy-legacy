package net.mx.edelicacy.food.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mx.edelicacy.block.base.DelicacyPieBlock;
import net.mx.edelicacy.item.base.DelicacyItem;
import net.mx.edelicacy.registry.DelicacyItems;

/** 黏糯谜之派：4 块黏糯派切片；碰撞箱随切掉的块数与朝向变化（与 1.21 一致，L 形的一档取整块） */
public class BlockStickyPie extends DelicacyPieBlock {

    private static final double HEIGHT = 4.75D;
    /** [已切块数][水平朝向序号]，以朝北为原型 */
    private static final AxisAlignedBB[][] SHAPES = buildShapes();

    public BlockStickyPie() {
        super(() -> DelicacyItems.STICKY_PIE_SLICE);
        DelicacyItem.setup(this, "sticky_pie");
    }

    private static AxisAlignedBB[][] buildShapes() {
        double[][] north = {
                {2, 2, 14, 14},
                {2, 2, 14, 14},
                {2, 2, 14, 8},
                {8, 2, 14, 8}
        };
        AxisAlignedBB[][] result = new AxisAlignedBB[north.length][4];
        for (int bites = 0; bites < north.length; bites++) {
            // 水平序号：南 0、西 1、北 2、东 3；对应从北顺时针转 2、3、0、1 次（与方块状态里的 y 旋转一致）
            int[] turns = {2, 3, 0, 1};
            for (int index = 0; index < 4; index++) {
                result[bites][index] = rotate(north[bites], turns[index]);
            }
        }
        return result;
    }

    /** 俯视顺时针 90° 一次：(x, z) → (16 - z, x) */
    private static AxisAlignedBB rotate(double[] box, int turns) {
        double x1 = box[0];
        double z1 = box[1];
        double x2 = box[2];
        double z2 = box[3];
        for (int i = 0; i < turns; i++) {
            double nx1 = 16 - z2;
            double nx2 = 16 - z1;
            z1 = x1;
            z2 = x2;
            x1 = nx1;
            x2 = nx2;
        }
        return new AxisAlignedBB(x1 / 16, 0, z1 / 16, x2 / 16, HEIGHT / 16, z2 / 16);
    }

    /** FD 的派朝向玩家视线方向（不取反），沿用的 1.21 方块状态按此旋转 */
    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ,
                                            int meta, EntityLivingBase placer, EnumHand hand) {
        return getDefaultState().withProperty(FACING, placer.getHorizontalFacing());
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return SHAPES[state.getValue(BITES)][state.getValue(FACING).getHorizontalIndex()];
    }

    /** 贴图有透明像素，需镂空渲染层（FD 的宴席方块同样如此） */
    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }
}
