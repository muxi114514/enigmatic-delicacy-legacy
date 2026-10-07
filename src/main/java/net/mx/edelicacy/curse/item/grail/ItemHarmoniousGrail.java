package net.mx.edelicacy.curse.item.grail;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mx.edelicacy.curse.client.CurseTooltips;
import net.mx.edelicacy.curse.potion.StackedEffects;
import net.mx.edelicacy.curse.util.CurseItemEntities;
import net.mx.edelicacy.curse.util.CurseRefs;
import net.mx.edelicacy.item.base.DelicacyItem;
import net.mx.edelicacy.util.EnigmaticBridge;

/**
 * 调谐圣杯：800 耐久，每饮消耗 100（不会损坏，最后一口后变回 EL 的邪恶圣杯）；只有七咒 / 救赎之人能饮，获得大量增益与短暂的副作用。
 */
public class ItemHarmoniousGrail extends DelicacyItem {

    private static final int COST_PER_DRINK = 100;

    public ItemHarmoniousGrail() {
        super("harmonious_grail");
        rarity(EnumRarity.RARE);
        setMaxStackSize(1);
        setMaxDamage(800);
        setNoRepair();
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
        ItemStack stack = player.getHeldItem(hand);
        if (!EnigmaticBridge.isTheOne(player)) {
            return new ActionResult<>(EnumActionResult.PASS, stack);
        }
        player.setActiveHand(hand);
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World world, EntityLivingBase entity) {
        world.playSound(null, entity.posX, entity.posY, entity.posZ, SoundEvents.ENTITY_GENERIC_EAT, SoundCategory.PLAYERS, 1.0F,
                1.0F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.4F);
        if (!(entity instanceof EntityPlayer) || world.isRemote) {
            return stack;
        }
        EntityPlayer player = (EntityPlayer) entity;
        applyEffects(player);
        player.addStat(StatList.getObjectUseStats(this));
        if (player.capabilities.isCreativeMode) {
            return stack;
        }
        if (player instanceof EntityPlayerMP) {
            stack.attemptDamageItem(COST_PER_DRINK, world.rand, (EntityPlayerMP) player);
        }
        if (stack.getItemDamage() >= stack.getMaxDamage() - 1) {
            return CurseRefs.unholyGrail() != null ? new ItemStack(CurseRefs.unholyGrail()) : ItemStack.EMPTY;
        }
        return stack;
    }

    /** 1.21 的 setDamage 把耐久钳在 max-1，圣杯永不碎 */
    @Override
    public void setDamage(ItemStack stack, int damage) {
        super.setDamage(stack, Math.max(0, Math.min(damage, getMaxDamage(stack) - 1)));
    }

    private static void applyEffects(EntityPlayer player) {
        int m = EnigmaticBridge.isTheOne(player) ? 40 : 20;
        StackedEffects.add(player, new PotionEffect(MobEffects.REGENERATION, 5 * m, 3, false, true));
        StackedEffects.add(player, new PotionEffect(MobEffects.REGENERATION, 20 * m, 0, false, true));
        StackedEffects.add(player, new PotionEffect(MobEffects.ABSORPTION, 60 * m, 2, false, true));
        StackedEffects.add(player, new PotionEffect(MobEffects.RESISTANCE, 60 * m, 0, false, true));
        StackedEffects.add(player, new PotionEffect(MobEffects.STRENGTH, 60 * m, 2, false, true));
        StackedEffects.add(player, new PotionEffect(MobEffects.SPEED, 60 * m, 0, false, true));
        StackedEffects.add(player, new PotionEffect(MobEffects.NAUSEA, 65, 0, false, true));
        StackedEffects.add(player, new PotionEffect(MobEffects.SLOWNESS, 25, 2, false, true));
        StackedEffects.add(player, new PotionEffect(MobEffects.WEAKNESS, 50, 4, false, true));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        if (CurseTooltips.shift()) {
            CurseTooltips.line(tooltip, "tooltip.enigmaticdelicacy.harmoniousGrail1");
            CurseTooltips.line(tooltip, "tooltip.enigmaticdelicacy.harmoniousGrail2");
        } else {
            CurseTooltips.holdShift(tooltip);
        }
        if (stack.isItemEnchanted()) {
            CurseTooltips.empty(tooltip);
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
