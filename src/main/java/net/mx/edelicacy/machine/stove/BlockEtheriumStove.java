package net.mx.edelicacy.machine.stove;

import com.wdcftgg.farmersdelightlegacy.common.block.BlockStove;
import com.wdcftgg.farmersdelightlegacy.common.recipe.CampfireCookingRecipe;
import com.wdcftgg.farmersdelightlegacy.common.recipe.manager.CampfireCookingRecipeManager;
import keletu.enigmaticlegacy.EnigmaticLegacy;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mx.edelicacy.EnigmaticDelicacy;
import net.mx.edelicacy.gui.DelicacyGuiHandler;
import net.mx.edelicacy.item.base.DelicacyItem;

/**
 * 以太炉灶。继承农夫乐事炉灶：沿用 FACING / LIT 与元数据，点燃时自动被农夫乐事认作热源（HeatSourceHelper 按 BlockStove + LIT 判断）。
 * <p>与农夫乐事炉灶的区别：用星尘（astral_dust）点燃（不认打火石/火焰弹），亮度 15，换成自己的方块实体，空手或无法上架的物品右键打开熔炉界面。
 */
public class BlockEtheriumStove extends BlockStove {

    public BlockEtheriumStove() {
        DelicacyItem.setup(this, "etherium_stove");
        setHardness(2.0F);
        setResistance(10.0F);
        setHarvestLevel("pickaxe", 0);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEtheriumStove();
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
        return state.getValue(LIT) ? 15 : 0;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
                                    EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack held = player.getHeldItem(hand);
        if (state.getValue(LIT)) {
            if (isShovel(held)) {
                extinguish(state, world, pos);
                held.damageItem(1, player);
                return true;
            }
            if (held.getItem() == Items.WATER_BUCKET) {
                extinguish(state, world, pos);
                if (!world.isRemote && !player.capabilities.isCreativeMode) {
                    player.setHeldItem(hand, new ItemStack(Items.BUCKET));
                }
                return true;
            }
        } else if (!held.isEmpty() && held.getItem() == EnigmaticLegacy.astralDust) {
            if (!world.isRemote) {
                world.playSound(null, pos, SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS, 1.0F,
                        (world.rand.nextFloat() - world.rand.nextFloat()) * 0.2F + 1.0F);
                world.setBlockState(pos, state.withProperty(LIT, true), 11);
                if (!player.capabilities.isCreativeMode) {
                    held.shrink(1);
                }
            }
            return true;
        }

        TileEntity tile = world.getTileEntity(pos);
        if (!(tile instanceof TileEtheriumStove)) {
            return false;
        }
        TileEtheriumStove stove = (TileEtheriumStove) tile;
        if (facing == EnumFacing.UP && !held.isEmpty()) {
            int slot = stove.getNextEmptyGrillSlot();
            CampfireCookingRecipe recipe = slot >= 0 && !stove.isGrillBlocked() ? CampfireCookingRecipeManager.findRecipe(held) : null;
            if (recipe != null) {
                if (!world.isRemote) {
                    stove.addToGrill(player.capabilities.isCreativeMode ? held.copy() : held, recipe, slot);
                }
                return true;
            }
        }
        if (!world.isRemote) {
            player.openGui(EnigmaticDelicacy.instance, DelicacyGuiHandler.ETHERIUM_STOVE, world, pos.getX(), pos.getY(), pos.getZ());
        }
        return true;
    }

    private static boolean isShovel(ItemStack stack) {
        return !stack.isEmpty() && (stack.getItem() instanceof ItemSpade || stack.getItem().getToolClasses(stack).contains("shovel"));
    }

    @Override
    public void extinguish(IBlockState state, World world, BlockPos pos) {
        // 同一方块只改属性，方块实体保留（shouldRefresh 只在换方块时重建）
        world.setBlockState(pos, state.withProperty(LIT, false), 3);
        world.playSound(null, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.6F);
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEtheriumStove) {
            ((TileEtheriumStove) tile).dropContents();
        }
        super.breakBlock(world, pos, state);
    }

    @Override
    public boolean hasComparatorInputOverride(IBlockState state) {
        return true;
    }

    @Override
    public int getComparatorInputOverride(IBlockState state, World world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        return tile instanceof TileEtheriumStove ? net.minecraft.inventory.Container.calcRedstoneFromInventory((TileEtheriumStove) tile) : 0;
    }
}
