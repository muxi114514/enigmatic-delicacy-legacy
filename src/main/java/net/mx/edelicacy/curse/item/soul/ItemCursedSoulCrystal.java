package net.mx.edelicacy.curse.item.soul;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemHandlerHelper;
import net.mx.edelicacy.curse.client.CurseTooltips;
import net.mx.edelicacy.curse.util.CurseItemEntities;
import net.mx.edelicacy.item.base.DelicacyItem;
import net.mx.edelicacy.registry.DelicacyItems;

/**
 * 咒魂晶（破碎 / 完整 / 碎片三种）。破碎的咒魂晶吸满灵魂（阈值 = 生命上限 × 8）后在背包里变成完整的咒魂晶。
 * 用处决之斧在砧板上切完整的晶体得到碎片是农夫乐事切割配方（整合者添加）。
 */
public class ItemCursedSoulCrystal extends DelicacyItem {

    private final boolean broken;

    public ItemCursedSoulCrystal(String name, int stackSize, boolean broken) {
        super(name);
        this.broken = broken;
        rarity(EnumRarity.RARE);
        stackSize(stackSize);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean hasEffect(ItemStack stack) {
        return !broken;
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (!broken || world.isRemote || !(entity instanceof EntityPlayer)) {
            return;
        }
        EntityPlayer player = (EntityPlayer) entity;
        int threshold = SoulNBT.getSoulThreshold(stack);
        if (threshold <= 0) {
            // 1.21 合成出来的破碎晶体没有阈值，0 >= 0 会立刻变成完整晶体；这里补上阈值
            SoulNBT.setSoulThreshold(stack, player);
            return;
        }
        if (SoulNBT.getSoulAmount(stack) >= threshold && DelicacyItems.CURSED_SOUL_CRYSTAL != null) {
            stack.shrink(1);
            ItemHandlerHelper.giveItemToPlayer(player, new ItemStack(DelicacyItems.CURSED_SOUL_CRYSTAL));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        if (CurseTooltips.shift()) {
            CurseTooltips.line(tooltip, "tooltip.enigmaticdelicacy.CursedSoulCrystal1");
            CurseTooltips.line(tooltip, "tooltip.enigmaticdelicacy.CursedSoulCrystal2");
        } else {
            CurseTooltips.holdShift(tooltip);
        }
        if (broken && SoulNBT.getSoulThreshold(stack) > 0) {
            CurseTooltips.line(tooltip, "tooltip.enigmaticdelicacy.curseSoulAmountPercent",
                    SoulNBT.getSoulAmount(stack), SoulNBT.getSoulThreshold(stack));
        }
    }

    @Override
    public boolean hasCustomEntity(ItemStack stack) {
        return true;
    }

    @Override
    @Nullable
    public Entity createEntity(World world, Entity location, ItemStack stack) {
        return CurseItemEntities.fireproof(world, location, stack);
    }
}
