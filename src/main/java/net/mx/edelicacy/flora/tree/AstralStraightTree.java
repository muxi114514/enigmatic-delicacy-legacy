package net.mx.edelicacy.flora.tree;

import java.util.Random;

import net.minecraft.block.BlockLog;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * 直干星辰树（1.21 StraightTrunkPlacer + BlobFoliagePlacer(半径 2, 偏移 0) + TwoLayersFeatureSize(1, 0, 1)）。
 * <ul>
 *   <li>普通：树干 5~8，树冠 4 层（astral_tree）</li>
 *   <li>小型：树干 2~3，树冠 2 层（astral_small_tree，沃土上的二阶段树苗）</li>
 * </ul>
 */
final class AstralStraightTree extends AstralTreeBase {

    private static final int FOLIAGE_RADIUS = 2;

    static final AstralStraightTree NORMAL = new AstralStraightTree(5, 3, 3);
    static final AstralStraightTree SMALL = new AstralStraightTree(2, 1, 1);

    private final int baseHeight;
    private final int heightRand;
    private final int foliageHeight;

    private AstralStraightTree(int baseHeight, int heightRand, int foliageHeight) {
        this.baseHeight = baseHeight;
        this.heightRand = heightRand;
        this.foliageHeight = foliageHeight;
    }

    @Override
    public boolean generate(World world, Random rand, BlockPos pos) {
        int height = baseHeight + rand.nextInt(heightRand + 1);
        if (!withinHeight(world, pos, height) || !hasSpace(world, pos, height)) {
            return false;
        }
        for (int i = 0; i < height; i++) {
            placeLog(world, pos.up(i), BlockLog.EnumAxis.Y);
        }
        BlockPos top = pos.up(height);
        for (int y = 0; y >= -foliageHeight; y--) {
            // Java 整数除法向零取整，与 1.21 相同
            int range = Math.max(FOLIAGE_RADIUS - 1 - y / 2, 0);
            placeLeavesRow(world, rand, top, range, y);
        }
        return true;
    }

    /** TwoLayersFeatureSize(1, 0, 1)：底层只检查树干位置，往上检查 3×3；空间不足整棵放弃 */
    private static boolean hasSpace(World world, BlockPos pos, int height) {
        for (int y = 0; y <= height + 1; y++) {
            int size = y < 1 ? 0 : 1;
            for (int dx = -size; dx <= size; dx++) {
                for (int dz = -size; dz <= size; dz++) {
                    if (!isFree(world, pos.add(dx, y, dz))) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /** BlobFoliagePlacer：四角随机缺一半，最上层四角全缺 */
    private void placeLeavesRow(World world, Random rand, BlockPos center, int range, int y) {
        for (int dx = -range; dx <= range; dx++) {
            for (int dz = -range; dz <= range; dz++) {
                boolean corner = Math.abs(dx) == range && Math.abs(dz) == range;
                if (corner && (rand.nextInt(2) == 0 || y == 0)) {
                    continue;
                }
                placeLeaf(world, center.add(dx, y, dz), rand);
            }
        }
    }
}
