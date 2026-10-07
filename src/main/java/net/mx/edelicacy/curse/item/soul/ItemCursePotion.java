package net.mx.edelicacy.curse.item.soul;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemHandlerHelper;
import net.mx.edelicacy.curse.client.CurseTooltips;
import net.mx.edelicacy.curse.potion.CursePotions;
import net.mx.edelicacy.curse.util.CurseItemEntities;
import net.mx.edelicacy.data.DelicacyData;
import net.mx.edelicacy.item.base.DelicacyItem;

/**
 * 诅咒药水：解除斩断七咒之戒带来的永久生命诅咒并回满生命（用 heal，兼容急救模组；1.21 直接 setHealth）。
 * 身上没有生命诅咒时喝下没有效果但仍会消耗。
 */
public class ItemCursePotion extends DelicacyItem {

    public ItemCursePotion() {
        super("curse_potion");
        rarity(EnumRarity.RARE);
        stackSize(1);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean hasEffect(ItemStack stack) {
        return true;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 48;
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
        if (world instanceof WorldServer) {
            spawnSpell((WorldServer) world, entity);
            world.playSound(null, entity.posX, entity.posY, entity.posZ, SoundEvents.EVOCATION_ILLAGER_CAST_SPELL, SoundCategory.PLAYERS,
                    1.0F, 0.6F + world.rand.nextFloat() * 0.2F);
        }
        if (!(entity instanceof EntityPlayer)) {
            return stack;
        }
        EntityPlayer player = (EntityPlayer) entity;
        if (!world.isRemote && CursePotions.HEALTH_CURSE != null && player.isPotionActive(CursePotions.HEALTH_CURSE)) {
            DelicacyData.get(player).setHealthCursed(false);
            player.removePotionEffect(CursePotions.HEALTH_CURSE);
            player.heal(player.getMaxHealth());
        }
        if (player.capabilities.isCreativeMode) {
            return stack;
        }
        stack.shrink(1);
        if (stack.isEmpty()) {
            return new ItemStack(Items.GLASS_BOTTLE);
        }
        ItemHandlerHelper.giveItemToPlayer(player, new ItemStack(Items.GLASS_BOTTLE));
        return stack;
    }

    /** 1.21 的 0xF13F1A 彩色药水粒子：SPELL_MOB 以 0 数量发包时偏移量即 RGB */
    private static void spawnSpell(WorldServer world, EntityLivingBase entity) {
        double h = entity.width / 6.0D;
        double v = entity.height / 4.0D;
        for (int i = 0; i < 48; i++) {
            double x = entity.posX + world.rand.nextGaussian() * h;
            double y = entity.posY + entity.height / 2.0D + world.rand.nextGaussian() * v;
            double z = entity.posZ + world.rand.nextGaussian() * h;
            world.spawnParticle(EnumParticleTypes.SPELL_MOB, x, y, z, 0, 0xF1 / 255.0D, 0x3F / 255.0D, 0x1A / 255.0D, 1.0D);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        if (CurseTooltips.shift()) {
            CurseTooltips.line(tooltip, "tooltip.enigmaticdelicacy.cursePotion");
        } else {
            CurseTooltips.holdShift(tooltip);
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
