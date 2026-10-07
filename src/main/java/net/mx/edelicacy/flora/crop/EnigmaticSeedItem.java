package net.mx.edelicacy.flora.crop;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemSeeds;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.mx.edelicacy.item.base.DelicacyItem;
import net.mx.edelicacy.registry.DelicacyBlocks;

/** 神秘种子：只能种在无尽沃土耕地上，堆叠 16 */
public class EnigmaticSeedItem extends ItemSeeds {

    public EnigmaticSeedItem(Block bush, Block farmland) {
        super(bush, farmland);
        DelicacyItem.setup(this, "enigmatic_seed");
        setMaxStackSize(16);
    }

    /** 原版种子只问土壤能否承载作物（农夫乐事耕地也行），这里限定无尽沃土耕地 */
    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing,
                                      float hitX, float hitY, float hitZ) {
        if (world.getBlockState(pos).getBlock() != DelicacyBlocks.INFINISOIL_FARMLAND) {
            return EnumActionResult.FAIL;
        }
        return super.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
    }
}
