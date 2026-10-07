package net.mx.edelicacy.flora.soil;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.wdcftgg.farmersdelightlegacy.common.registry.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 蘑菇/菌类 → 菌落 的对照：原版蘑菇固定转成农夫乐事菌落，其余来自配置 convertList。
 * <p>配置在 postInit 解析成不可变表，之后只读，主线程访问安全。
 */
public final class ColonyConversion {

    private static final Logger LOG = LogManager.getLogger("enigmaticdelicacy");
    private static volatile Map<Block, Block> modColonies = Collections.emptyMap();

    private ColonyConversion() {
    }

    /** 解析 "plant->colony" 列表；缺失的方块（对应模组未安装）直接跳过 */
    public static void resolve(String[] entries) {
        Map<Block, Block> map = new IdentityHashMap<>();
        for (String entry : entries) {
            String[] parts = entry.split("->");
            if (parts.length != 2) {
                LOG.warn("Invalid Infini-Soil convert entry '{}', expected 'plant->colony'", entry);
                continue;
            }
            Block plant = findBlock(parts[0].trim());
            Block colony = findBlock(parts[1].trim());
            if (plant != null && colony != null) {
                map.put(plant, colony);
            }
        }
        modColonies = Collections.unmodifiableMap(map);
    }

    @Nullable
    private static Block findBlock(String id) {
        ResourceLocation key = new ResourceLocation(id);
        return ForgeRegistries.BLOCKS.containsKey(key) ? ForgeRegistries.BLOCKS.getValue(key) : null;
    }

    /** 植物对应的菌落初始状态，不可转换返回 null */
    @Nullable
    public static IBlockState colonyFor(Block plant) {
        if (plant == Blocks.BROWN_MUSHROOM) {
            return ModBlocks.BROWN_MUSHROOM_COLONY.getDefaultState();
        }
        if (plant == Blocks.RED_MUSHROOM) {
            return ModBlocks.RED_MUSHROOM_COLONY.getDefaultState();
        }
        Block colony = modColonies.get(plant);
        return colony == null ? null : colony.getDefaultState();
    }

    /** 沃土上方的蘑菇转成菌落，返回是否发生了转换（1.21 convertMushroomToColony + tryConvertModMushroomToColony） */
    public static boolean convert(World world, BlockPos pos, IBlockState state) {
        IBlockState colony = colonyFor(state.getBlock());
        if (colony == null) {
            return false;
        }
        world.setBlockState(pos, colony, 3);
        return true;
    }
}
