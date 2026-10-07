package net.mx.edelicacy.flora.wood;

import java.util.Random;

import net.minecraft.block.BlockSlab;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.mx.edelicacy.registry.DelicacyBlocks;

/**
 * 星辰木台阶。1.12 的台阶需要单层与双层两个方块，以及一个变种属性（ItemSlab 合并台阶时要比较它），
 * 这里用只有一个取值的占位属性；模型映射时忽略它。
 */
public abstract class AstralSlabBlock extends BlockSlab {

    public static final PropertyEnum<Variant> VARIANT = PropertyEnum.create("variant", Variant.class);

    protected AstralSlabBlock() {
        super(Material.WOOD);
        setHardness(AstralWoodParts.PLANKS_HARDNESS);
        setResistance(AstralWoodParts.PLANKS_RESISTANCE);
        setSoundType(SoundType.WOOD);
        IBlockState state = blockState.getBaseState().withProperty(VARIANT, Variant.DEFAULT);
        if (!isDouble()) {
            state = state.withProperty(HALF, EnumBlockHalf.BOTTOM);
            useNeighborBrightness = true;
        }
        setDefaultState(state);
    }

    @Override
    public String getUnlocalizedName(int meta) {
        return getUnlocalizedName();
    }

    @Override
    public IProperty<?> getVariantProperty() {
        return VARIANT;
    }

    @Override
    public Comparable<?> getTypeForItem(ItemStack stack) {
        return Variant.DEFAULT;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return isDouble() ? new BlockStateContainer(this, VARIANT) : new BlockStateContainer(this, HALF, VARIANT);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        IBlockState state = getDefaultState();
        if (!isDouble()) {
            state = state.withProperty(HALF, (meta & 8) == 0 ? EnumBlockHalf.BOTTOM : EnumBlockHalf.TOP);
        }
        return state;
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return !isDouble() && state.getValue(HALF) == EnumBlockHalf.TOP ? 8 : 0;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Item.getItemFromBlock(DelicacyBlocks.ASTRAL_SLAB);
    }

    @Override
    public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
        return new ItemStack(DelicacyBlocks.ASTRAL_SLAB);
    }

    /** 单层台阶（注册名 astral_slab，带物品） */
    public static class HalfSlab extends AstralSlabBlock {
        @Override
        public boolean isDouble() {
            return false;
        }
    }

    /** 双层台阶（注册名 astral_double_slab，无物品，掉落两个单层） */
    public static class DoubleSlab extends AstralSlabBlock {
        @Override
        public boolean isDouble() {
            return true;
        }
    }

    public enum Variant implements IStringSerializable {
        DEFAULT;

        @Override
        public String getName() {
            return "default";
        }
    }
}
