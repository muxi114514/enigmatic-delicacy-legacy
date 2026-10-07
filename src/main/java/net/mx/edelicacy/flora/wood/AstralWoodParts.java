package net.mx.edelicacy.flora.wood;

import com.wdcftgg.farmersdelightlegacy.common.block.BlockCabinet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockButtonWood;
import net.minecraft.block.BlockPressurePlate;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;

/**
 * 星辰木系列里只需改数值的简单方块（原版构造器多为 protected，只能子类化）。
 * <p>1.21 的抗爆值 R 换算到 1.12：setResistance(R × 5 / 3)。
 */
public final class AstralWoodParts {

    /** 木板强度 3 / 抗爆 5（1.21 AstralWoodProperties.PLANKS） */
    static final float PLANKS_HARDNESS = 3.0F;
    static final float PLANKS_RESISTANCE = 5.0F * 5.0F / 3.0F;
    /** 门与活板门强度 5 / 抗爆 5 */
    static final float DOOR_HARDNESS = 5.0F;
    static final float DOOR_RESISTANCE = 5.0F * 5.0F / 3.0F;

    private AstralWoodParts() {
    }

    /** 木板与神秘果箱：普通木质方块 */
    public static class Wood extends Block {
        public Wood(MapColor color, float hardness, float resistance) {
            super(Material.WOOD, color);
            setHardness(hardness);
            setResistance(resistance);
            setSoundType(SoundType.WOOD);
        }
    }

    public static class Stairs extends BlockStairs {
        public Stairs(IBlockState planks) {
            super(planks);
            // 父类从木板复制了硬度与抗爆，这里只需打开邻居亮度
            useNeighborBrightness = true;
        }
    }

    public static class Trapdoor extends BlockTrapDoor {
        public Trapdoor() {
            super(Material.WOOD);
            setHardness(DOOR_HARDNESS);
            setResistance(DOOR_RESISTANCE);
            setSoundType(SoundType.WOOD);
        }
    }

    public static class PressurePlate extends BlockPressurePlate {
        public PressurePlate() {
            super(Material.WOOD, Sensitivity.EVERYTHING);
            setHardness(0.5F);
            setResistance(0.5F * 5.0F / 3.0F);
            setSoundType(SoundType.WOOD);
        }
    }

    public static class Button extends BlockButtonWood {
        public Button() {
            setHardness(0.5F);
            setSoundType(SoundType.WOOD);
        }
    }

    /**
     * 星辰橱柜：直接复用农夫乐事橱柜（方块实体与 GUI 都只认 TileEntityCabinet，与命名空间无关）。
     * 1.21 没给它写战利品表导致打掉不掉落，这里沿用父类默认掉落自身。
     */
    public static class Cabinet extends BlockCabinet {
        public Cabinet() {
            setHardness(PLANKS_HARDNESS);
            setResistance(PLANKS_RESISTANCE);
        }
    }
}
