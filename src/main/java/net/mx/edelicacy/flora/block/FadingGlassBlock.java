package net.mx.edelicacy.flora.block;

import java.util.Random;

import net.minecraft.block.BlockGlass;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumBlockRenderType;

/**
 * 褪隐玻璃：完全不渲染的玻璃，亮度 8，打掉什么也不掉（1.21 没有战利品表）。
 * 1.21 中尚无任何玩法使用它，仅注册方块与物品，且不放进创造栏。
 */
public class FadingGlassBlock extends BlockGlass {

    public FadingGlassBlock() {
        super(Material.GLASS, false);
        setHardness(0.3F);
        setResistance(0.5F);
        setSoundType(SoundType.GLASS);
        setLightLevel(8.0F / 15.0F);
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.INVISIBLE;
    }

    @Override
    public int quantityDropped(Random random) {
        return 0;
    }

    @Override
    protected boolean canSilkHarvest() {
        return false;
    }
}
