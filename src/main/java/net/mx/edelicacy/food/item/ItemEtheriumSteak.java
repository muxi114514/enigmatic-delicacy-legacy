package net.mx.edelicacy.food.item;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mx.edelicacy.api.IStoveRepairable;
import net.mx.edelicacy.data.DelicacyData;
import net.mx.edelicacy.util.TooltipHelper;

/**
 * 以太牛排：8 次，每吃一口以太护盾阈值 +0.08（持续 = 原剩余/2 + 320 tick，属性来源见 EtheriumSteakHandler）；
 * 次数可在以太炉里修复，每 320 tick 修一次。
 */
public class ItemEtheriumSteak extends MultiUseFoodItem implements IStoveRepairable {

    public static final int MAX_USES = 8;
    public static final int TICKS_PER_USE = 320;
    public static final int BONUS_TICKS = 320;

    public ItemEtheriumSteak() {
        super("etherium_steak", 9, 0.9F, MAX_USES);
        alwaysEdible();
        // 1.21 为普通食物 32 tick + 8
        useDuration(40);
    }

    @Override
    protected ItemStack consumeUse(ItemStack stack, EntityPlayer player) {
        int uses = getUses(stack);
        if (uses <= 1) {
            stack.shrink(1);
        } else {
            setUses(stack, uses - 1);
        }
        return stack;
    }

    @Override
    protected void onEaten(ItemStack stack, World world, EntityPlayer player) {
        if (!world.isRemote) {
            DelicacyData data = DelicacyData.get(player);
            data.setEtheriumSteakTick(data.getEtheriumSteakTick() / 2 + BONUS_TICKS);
        }
    }

    @Override
    public int getStoveRepairTicks(ItemStack stack) {
        return getUses(stack) < MAX_USES ? TICKS_PER_USE : 0;
    }

    @Override
    public void repairOnStove(ItemStack stack) {
        setUses(stack, getUses(stack) + 1);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public int getRGBDurabilityForDisplay(ItemStack stack) {
        return 0x96EEEE;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        TooltipHelper.line(tooltip, "tooltip.enigmaticdelicacy.etheriumSteak1");
        TooltipHelper.line(tooltip, "tooltip.enigmaticdelicacy.etheriumSteak2");
    }
}
