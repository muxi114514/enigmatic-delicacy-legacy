package net.mx.edelicacy.flora.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.block.BlockLog;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

/**
 * 大型星辰树（1.21 FancyTrunkPlacer(3, 10, 0) + FancyFoliagePlacer(半径 2, 偏移 4, 高 4)
 * + TwoLayersFeatureSize(0, 0, 0, 最小截断高度 4)），算法逐行移植自 1.21 原版。
 */
final class AstralFancyTree extends AstralTreeBase {

    static final AstralFancyTree INSTANCE = new AstralFancyTree();

    private static final int BASE_HEIGHT = 3;
    private static final int HEIGHT_RAND = 10;
    private static final int MIN_CLIPPED_HEIGHT = 4;
    private static final int FOLIAGE_RADIUS = 2;
    private static final int FOLIAGE_OFFSET = 4;
    private static final int FOLIAGE_HEIGHT = 4;

    private AstralFancyTree() {
    }

    @Override
    public boolean generate(World world, Random rand, BlockPos pos) {
        int height = BASE_HEIGHT + rand.nextInt(HEIGHT_RAND + 1);
        if (!withinHeight(world, pos, height)) {
            return false;
        }
        int freeHeight = maxFreeHeight(world, pos, height);
        if (freeHeight < height && freeHeight < MIN_CLIPPED_HEIGHT) {
            return false;
        }
        for (BlockPos attachment : placeTrunk(world, rand, freeHeight, pos)) {
            for (int y = FOLIAGE_OFFSET; y >= FOLIAGE_OFFSET - FOLIAGE_HEIGHT; y--) {
                int range = FOLIAGE_RADIUS + (y != FOLIAGE_OFFSET && y != FOLIAGE_OFFSET - FOLIAGE_HEIGHT ? 1 : 0);
                placeLeavesRow(world, rand, attachment, range, y);
            }
        }
        return true;
    }

    /** 只检查树干所在竖列；被挡住时返回 挡住高度 - 2 */
    private static int maxFreeHeight(World world, BlockPos pos, int height) {
        for (int y = 0; y <= height + 1; y++) {
            if (!isFree(world, pos.up(y))) {
                return y - 2;
            }
        }
        return height;
    }

    private List<BlockPos> placeTrunk(World world, Random rand, int freeHeight, BlockPos pos) {
        int limit = freeHeight + 2;
        int trunkTop = MathHelper.floor(limit * 0.618D);
        int branchCount = Math.min(1, MathHelper.floor(1.382D + Math.pow(1.0D * limit / 13.0D, 2.0D)));
        int branchBaseLimit = pos.getY() + trunkTop;
        int y = limit - 5;
        List<FoliageCoords> coords = new ArrayList<>();
        coords.add(new FoliageCoords(pos.up(y), branchBaseLimit));
        for (; y >= 0; y--) {
            float shape = treeShape(limit, y);
            if (shape < 0.0F) {
                continue;
            }
            for (int k = 0; k < branchCount; k++) {
                double radius = shape * (rand.nextFloat() + 0.328D);
                double angle = rand.nextFloat() * 2.0F * Math.PI;
                double ox = radius * Math.sin(angle) + 0.5D;
                double oz = radius * Math.cos(angle) + 0.5D;
                BlockPos end = pos.add(MathHelper.floor(ox), y - 1, MathHelper.floor(oz));
                if (!makeLimb(world, end, end.up(5), false)) {
                    continue;
                }
                int dx = pos.getX() - end.getX();
                int dz = pos.getZ() - end.getZ();
                double baseY = end.getY() - Math.sqrt(dx * dx + dz * dz) * 0.381D;
                int branchBaseY = baseY > branchBaseLimit ? branchBaseLimit : (int) baseY;
                BlockPos branchBase = new BlockPos(pos.getX(), branchBaseY, pos.getZ());
                if (makeLimb(world, branchBase, end, false)) {
                    coords.add(new FoliageCoords(end, branchBase.getY()));
                }
            }
        }
        makeLimb(world, pos, pos.up(trunkTop), true);
        makeBranches(world, limit, pos, coords);
        List<BlockPos> attachments = new ArrayList<>();
        for (FoliageCoords c : coords) {
            if (trimBranches(limit, c.branchBase - pos.getY())) {
                attachments.add(c.pos);
            }
        }
        return attachments;
    }

    /** modify=false 时只检查沿线是否空闲，true 时沿线放原木 */
    private boolean makeLimb(World world, BlockPos base, BlockPos target, boolean modify) {
        if (!modify && base.equals(target)) {
            return true;
        }
        int dx = target.getX() - base.getX();
        int dy = target.getY() - base.getY();
        int dz = target.getZ() - base.getZ();
        int steps = Math.max(Math.abs(dx), Math.max(Math.abs(dy), Math.abs(dz)));
        if (steps == 0) {
            placeLog(world, base, BlockLog.EnumAxis.Y);
            return true;
        }
        float fx = (float) dx / steps;
        float fy = (float) dy / steps;
        float fz = (float) dz / steps;
        for (int j = 0; j <= steps; j++) {
            BlockPos point = base.add(MathHelper.floor(0.5F + j * fx), MathHelper.floor(0.5F + j * fy), MathHelper.floor(0.5F + j * fz));
            if (modify) {
                placeLog(world, point, logAxis(base, point));
            } else if (!isFree(world, point)) {
                return false;
            }
        }
        return true;
    }

    private static BlockLog.EnumAxis logAxis(BlockPos from, BlockPos to) {
        int ax = Math.abs(to.getX() - from.getX());
        int az = Math.abs(to.getZ() - from.getZ());
        int max = Math.max(ax, az);
        if (max <= 0) {
            return BlockLog.EnumAxis.Y;
        }
        return ax == max ? BlockLog.EnumAxis.X : BlockLog.EnumAxis.Z;
    }

    private static boolean trimBranches(int limit, int height) {
        return height >= limit * 0.2D;
    }

    private void makeBranches(World world, int limit, BlockPos pos, List<FoliageCoords> coords) {
        for (FoliageCoords c : coords) {
            BlockPos base = new BlockPos(pos.getX(), c.branchBase, pos.getZ());
            if (!base.equals(c.pos) && trimBranches(limit, c.branchBase - pos.getY())) {
                makeLimb(world, base, c.pos, true);
            }
        }
    }

    private static float treeShape(int limit, int y) {
        if (y < limit * 0.3F) {
            return -1.0F;
        }
        float half = limit / 2.0F;
        float offset = half - y;
        float width = MathHelper.sqrt(half * half - offset * offset);
        if (offset == 0.0F) {
            width = half;
        } else if (Math.abs(offset) >= half) {
            return 0.0F;
        }
        return width * 0.5F;
    }

    /** FancyFoliagePlacer：圆形截面，(|x|+0.5)² + (|z|+0.5)² 超出半径的跳过 */
    private void placeLeavesRow(World world, Random rand, BlockPos center, int range, int y) {
        for (int dx = -range; dx <= range; dx++) {
            for (int dz = -range; dz <= range; dz++) {
                float fx = Math.abs(dx) + 0.5F;
                float fz = Math.abs(dz) + 0.5F;
                if (fx * fx + fz * fz <= range * range) {
                    placeLeaf(world, center.add(dx, y, dz), rand);
                }
            }
        }
    }

    private static final class FoliageCoords {
        final BlockPos pos;
        final int branchBase;

        FoliageCoords(BlockPos pos, int branchBase) {
            this.pos = pos;
            this.branchBase = branchBase;
        }
    }
}
