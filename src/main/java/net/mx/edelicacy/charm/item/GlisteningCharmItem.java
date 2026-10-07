package net.mx.edelicacy.charm.item;

import java.util.List;

import javax.annotation.Nullable;

import baubles.api.BaubleType;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mx.edelicacy.charm.CharmConfig;
import net.mx.edelicacy.charm.glistening.GlisteningResistance;
import net.mx.edelicacy.item.base.DelicacyBaubleItem;
import net.mx.edelicacy.util.TooltipHelper;

/**
 * 守护护符（CHARM）：减伤系数每 10 秒 +5%（物品冷却计时）直到上限，受击 -5%；
 * 减伤与延长受击无敌在 GlisteningCharmEvents 结算，坚韧效果翻倍由 WP2 的坚韧效果判定。
 */
public class GlisteningCharmItem extends DelicacyBaubleItem {

    /** 1.21 用物品冷却 200 tick 当回复计时 */
    public static final int REGEN_INTERVAL = 200;
    public static final int REGEN_STEP = 5;

    public GlisteningCharmItem() {
        super("glistening_charm", BaubleType.CHARM);
        rarity(EnumRarity.UNCOMMON);
    }

    @Override
    public void onWornTick(ItemStack stack, EntityLivingBase wearer) {
        super.onWornTick(stack, wearer);
        if (wearer.world.isRemote || !(wearer instanceof EntityPlayer)) {
            return;
        }
        EntityPlayer player = (EntityPlayer) wearer;
        if (GlisteningResistance.get(stack) < CharmConfig.glisteningCharmThreshold && !player.getCooldownTracker().hasCooldown(this)) {
            player.getCooldownTracker().setCooldown(this, REGEN_INTERVAL);
            GlisteningResistance.add(stack, REGEN_STEP);
        }
    }

    /** 减伤系数存在物品 NBT 里，改动后让 Baubles 同步给客户端显示 */
    @Override
    public boolean willAutoSync(ItemStack stack, EntityLivingBase wearer) {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        TooltipHelper.empty(tooltip);
        if (TooltipHelper.shiftDown()) {
            TooltipHelper.line(tooltip, "tooltip.enigmaticdelicacy.glisteningCharm1");
            TooltipHelper.line(tooltip, "tooltip.enigmaticdelicacy.glisteningCharm2");
            TooltipHelper.empty(tooltip);
            TooltipHelper.line(tooltip, "tooltip.enigmaticdelicacy.glisteningCharm3");
            TooltipHelper.line(tooltip, "tooltip.enigmaticdelicacy.glisteningCharm4");
            TooltipHelper.line(tooltip, "tooltip.enigmaticdelicacy.glisteningCharm5");
        } else {
            TooltipHelper.holdShift(tooltip);
        }
        TooltipHelper.empty(tooltip);
        TooltipHelper.line(tooltip, "tooltip.enigmaticdelicacy.glisteningCharm6", GlisteningResistance.get(stack) + "%");
    }
}
