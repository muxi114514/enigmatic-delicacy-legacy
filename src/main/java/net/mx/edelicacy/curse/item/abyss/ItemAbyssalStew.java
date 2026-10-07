package net.mx.edelicacy.curse.item.abyss;

import java.util.List;

import javax.annotation.Nullable;

import keletu.enigmaticlegacy.EnigmaticLegacy;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemHandlerHelper;
import net.mx.edelicacy.curse.CurseConfig;
import net.mx.edelicacy.curse.client.CurseTooltips;
import net.mx.edelicacy.curse.util.CurseRefs;
import net.mx.edelicacy.data.DelicacyData;
import net.mx.edelicacy.item.base.DelicacyItem;

/**
 * 深渊乱炖（深渊物品，仅天选之人可用，EL 负责拦截）：饮用后进入 180 秒深渊状态，效果见 {@link AbyssalStewHandler}。
 * 1.21 继承 BaseDrinkableItem，不结算食物的饥饿值，这里同样只喝不饱；状态中再喝会先返还上一份的深渊之心。
 */
public class ItemAbyssalStew extends DelicacyItem {

    public ItemAbyssalStew() {
        super("abyssal_stew");
        rarity(EnumRarity.EPIC);
        stackSize(16);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 32;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.DRINK;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        player.setActiveHand(hand);
        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World world, EntityLivingBase entity) {
        if (!(entity instanceof EntityPlayer)) {
            return stack;
        }
        EntityPlayer player = (EntityPlayer) entity;
        if (!world.isRemote) {
            DelicacyData data = DelicacyData.get(player);
            if (data.getAbyssStewTick() > 0 && !player.capabilities.isCreativeMode) {
                ItemHandlerHelper.giveItemToPlayer(player, new ItemStack(CurseRefs.abyssalHeart()));
            }
            world.playSound(null, player.posX, player.posY, player.posZ, EnigmaticLegacy.CHARGED_ON_SOUND, SoundCategory.PLAYERS, 1.0F, 0.25F);
            data.setAbyssStewTick(CurseConfig.abyssalStewDuration * 20);
            if (player instanceof EntityPlayerMP) {
                CriteriaTriggers.CONSUME_ITEM.trigger((EntityPlayerMP) player, stack);
            }
        }
        player.addStat(StatList.getObjectUseStats(this));
        if (player.capabilities.isCreativeMode) {
            return stack;
        }
        stack.shrink(1);
        if (stack.isEmpty()) {
            return new ItemStack(Items.BOWL);
        }
        ItemHandlerHelper.giveItemToPlayer(player, new ItemStack(Items.BOWL));
        return stack;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        if (CurseTooltips.shift()) {
            CurseTooltips.line(tooltip, "tooltip.enigmaticdelicacy.abyssalStew1");
            CurseTooltips.line(tooltip, "tooltip.enigmaticdelicacy.abyssalStew2", CurseConfig.abyssalStewDamageBoost + "%");
            CurseTooltips.line(tooltip, "tooltip.enigmaticdelicacy.abyssalStew3", CurseConfig.abyssalStewDamageResistance + "%");
            CurseTooltips.line(tooltip, "tooltip.enigmaticdelicacy.abyssalStew4");
            CurseTooltips.line(tooltip, "tooltip.enigmaticdelicacy.abyssalStew5");
            CurseTooltips.line(tooltip, "tooltip.enigmaticdelicacy.abyssalStew6");
            CurseTooltips.line(tooltip, "tooltip.enigmaticdelicacy.abyssalStew7");
        } else {
            CurseTooltips.holdShift(tooltip);
        }
    }
}
