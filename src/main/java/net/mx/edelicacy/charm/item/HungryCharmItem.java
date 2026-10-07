package net.mx.edelicacy.charm.item;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.Multimap;

import baubles.api.BaubleType;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mx.eaddons.attribute.EnigmaticAttributes;
import net.mx.edelicacy.charm.client.CharmTooltips;
import net.mx.edelicacy.item.base.DelicacyBaubleItem;
import net.mx.edelicacy.util.TooltipHelper;

/**
 * 饥饿护符（CHARM）：跳跃 +16%，每 12 tick 额外消耗 0.05 饥饿值；
 * 吃喝快 8 tick（HungryCharmEvents），低饱食度可疾跑（HungerSprint）。
 */
public class HungryCharmItem extends DelicacyBaubleItem {

    private static final double JUMP_BONUS = 0.16D;
    private static final UUID JUMP_ID = UUID.nameUUIDFromBytes("enigmaticdelicacy:hungry_charm/jump".getBytes(StandardCharsets.UTF_8));

    public HungryCharmItem() {
        super("hungry_charm", BaubleType.CHARM);
    }

    @Override
    protected void fillModifiers(Multimap<String, AttributeModifier> modifiers, ItemStack stack, EntityLivingBase wearer) {
        modifiers.put(EnigmaticAttributes.JUMP_BOOST.getName(),
                new AttributeModifier(JUMP_ID, "enigmaticdelicacy:hungry_charm", JUMP_BONUS, 2));
    }

    @Override
    public void onWornTick(ItemStack stack, EntityLivingBase wearer) {
        super.onWornTick(stack, wearer);
        if (!wearer.world.isRemote && wearer instanceof EntityPlayer && wearer.ticksExisted % 12 == 0) {
            ((EntityPlayer) wearer).addExhaustion(0.05F);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        TooltipHelper.empty(tooltip);
        if (TooltipHelper.shiftDown()) {
            TooltipHelper.line(tooltip, "tooltip.enigmaticdelicacy.hungryCharm1");
            TooltipHelper.line(tooltip, "tooltip.enigmaticdelicacy.hungryCharm2");
        } else {
            TooltipHelper.holdShift(tooltip);
        }
        CharmTooltips.header(tooltip);
        CharmTooltips.attribute(tooltip, EnigmaticAttributes.JUMP_BOOST, 2, JUMP_BONUS);
    }
}
