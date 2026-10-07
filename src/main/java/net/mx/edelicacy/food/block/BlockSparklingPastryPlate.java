package net.mx.edelicacy.food.block;

import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mx.edelicacy.block.base.DelicacyFeastBlock;
import net.mx.edelicacy.food.client.FoodClient;
import net.mx.edelicacy.item.base.DelicacyItem;
import net.mx.edelicacy.registry.DelicacyItems;

/** 一盘闪冰酥：3 份闪冰酥，空手取用，亮度 4，份数越多闪光越频繁 */
public class BlockSparklingPastryPlate extends DelicacyFeastBlock {

    /** FD 的托盘碰撞箱 */
    private static final AxisAlignedBB TRAY = new AxisAlignedBB(0.0625D, 0.0D, 0.0625D, 0.9375D, 0.125D, 0.9375D);

    public BlockSparklingPastryPlate() {
        super(Material.CAKE, 3, () -> DelicacyItems.SPARKLING_PASTRY, null);
        DelicacyItem.setup(this, "sparkling_pastry_plate");
        setHardness(0.5F);
        // (int) (15 × 0.27) = 4；直接写 4/15 会因浮点截断成 3
        setLightLevel(0.27F);
        shape(TRAY);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
        if (rand.nextFloat() < 0.1F * getServings(state)) {
            double x = pos.getX() + 0.2D + rand.nextDouble() * 0.6D;
            double y = pos.getY() + 0.25D;
            double z = pos.getZ() + 0.2D + rand.nextDouble() * 0.6D;
            FoodClient.spawnSparkle(world, x, y, z);
        }
    }

    /** 贴图有透明像素，需镂空渲染层（FD 的宴席方块同样如此） */
    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }
}
