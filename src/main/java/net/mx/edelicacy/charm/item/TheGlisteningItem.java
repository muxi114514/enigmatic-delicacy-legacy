package net.mx.edelicacy.charm.item;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mx.edelicacy.charm.CharmConfig;
import net.mx.edelicacy.charm.glistening.GlisteningShield;
import net.mx.edelicacy.item.base.DelicacyItem;
import net.mx.edelicacy.util.TooltipHelper;

/**
 * 闪耀之书：主手攻击 5（总 6）、攻速 -2.2；右键持续「饮用」每 5 tick 充能 4 点护盾（开始时进入 60 秒冷却）。
 * <p>放在背包时的减伤、护盾吸收与攻击回盾在 GlisteningBookEvents 结算。
 */
public class TheGlisteningItem extends DelicacyItem {

    private static final int CHARGE_COOLDOWN = 1200;
    private static final int CHARGE_STEP = 4;
    private static final int USE_DURATION = 32000;

    public TheGlisteningItem() {
        super("the_glistening");
        rarity(EnumRarity.EPIC);
        stackSize(1);
    }

    @Override
    public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot slot) {
        Multimap<String, AttributeModifier> map = HashMultimap.create();
        if (slot == EntityEquipmentSlot.MAINHAND) {
            map.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", 5.0D, 0));
            map.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", -2.2D, 0));
        }
        return map;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!player.getCooldownTracker().hasCooldown(this) && GlisteningShield.get(stack) < CharmConfig.glisteningBookThreshold) {
            player.getCooldownTracker().setCooldown(this, CHARGE_COOLDOWN);
            player.setActiveHand(hand);
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }
        return new ActionResult<>(EnumActionResult.PASS, stack);
    }

    /** count 为剩余使用时间；护盾只在服务端增加，满了两端都停止 */
    @Override
    public void onUsingTick(ItemStack stack, EntityLivingBase living, int count) {
        if (count % 5 != 0) {
            return;
        }
        int shield = GlisteningShield.get(stack);
        if (shield >= CharmConfig.glisteningBookThreshold) {
            living.stopActiveHand();
        } else if (!living.world.isRemote) {
            GlisteningShield.set(stack, shield + CHARGE_STEP);
        }
    }

    /** 充能时服务端改了 NBT 并同步回来，原版会因物品「不同」而打断使用 */
    @Override
    public boolean canContinueUsing(ItemStack oldStack, ItemStack newStack) {
        return oldStack.getItem() == newStack.getItem();
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || oldStack.getItem() != newStack.getItem();
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.DRINK;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return USE_DURATION;
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return GlisteningShield.raw(stack) > 0;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return 1.0D - (double) GlisteningShield.get(stack) / CharmConfig.glisteningBookThreshold;
    }

    @Override
    public int getRGBDurabilityForDisplay(ItemStack stack) {
        return 0x2BF8FF;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        if (TooltipHelper.shiftDown()) {
            TooltipHelper.line(tooltip, "tooltip.enigmaticdelicacy.theGlistening1");
            TooltipHelper.line(tooltip, "tooltip.enigmaticdelicacy.theGlistening2");
            TooltipHelper.empty(tooltip);
            TooltipHelper.line(tooltip, "tooltip.enigmaticdelicacy.inInventory");
            TooltipHelper.line(tooltip, "tooltip.enigmaticdelicacy.theGlistening3");
            TooltipHelper.line(tooltip, "tooltip.enigmaticdelicacy.theGlistening4");
            TooltipHelper.line(tooltip, "tooltip.enigmaticdelicacy.theGlistening5");
        } else {
            TooltipHelper.line(tooltip, "tooltip.enigmaticdelicacy.theGlisteningLore");
            TooltipHelper.line(tooltip, "tooltip.enigmaticdelicacy.theGlisteningShield", GlisteningShield.get(stack));
            TooltipHelper.empty(tooltip);
            TooltipHelper.holdShift(tooltip);
        }
        TooltipHelper.empty(tooltip);
    }
}
