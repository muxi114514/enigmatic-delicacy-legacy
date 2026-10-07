package net.mx.edelicacy.flora.wood;

import java.util.Random;

import net.minecraft.block.BlockDoor;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.mx.edelicacy.registry.DelicacyItems;

/** 星辰木门：原版门的掉落与选取写死成原版门物品，这里改成本模组的门物品 */
public class AstralDoorBlock extends BlockDoor {

    public AstralDoorBlock() {
        super(Material.WOOD);
        setHardness(AstralWoodParts.DOOR_HARDNESS);
        setResistance(AstralWoodParts.DOOR_RESISTANCE);
        setSoundType(SoundType.WOOD);
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return state.getValue(HALF) == EnumDoorHalf.UPPER ? Items.AIR : DelicacyItems.ASTRAL_DOOR;
    }

    @Override
    public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
        return new ItemStack(DelicacyItems.ASTRAL_DOOR);
    }
}
