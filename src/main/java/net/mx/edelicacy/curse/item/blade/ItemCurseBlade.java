package net.mx.edelicacy.curse.item.blade;

import java.util.List;

import javax.annotation.Nullable;

import com.wdcftgg.farmersdelightlegacy.api.knife.ItemKnifeBase;

import keletu.enigmaticlegacy.EnigmaticLegacy;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mx.edelicacy.curse.CurseConfig;
import net.mx.edelicacy.curse.client.CurseTooltips;
import net.mx.edelicacy.curse.damage.CurseDamageSources;
import net.mx.edelicacy.curse.item.CurseMaterials;
import net.mx.edelicacy.curse.util.CurseItemEntities;
import net.mx.edelicacy.item.base.DelicacyItem;
import net.mx.edelicacy.util.StackNBT;

/**
 * 诅咒刃片（农夫乐事的刀）：潜行右键持续 50 tick、期间不断扣血完成蓄力；蓄力后的下一击见 {@link CurseBladeHandler}，
 * 佩戴七咒之戒时对空砧板使用可斩断戒指，见 {@link CursedRingCutter}。
 */
public class ItemCurseBlade extends ItemKnifeBase {

    public static final String ACTIVE = "Active";
    private static final int CHARGE_TICKS = 50;

    public ItemCurseBlade() {
        super(CurseMaterials.CURSE_BLADE, 5.0D, -2.4D);
        DelicacyItem.setup(this, "curse_blade");
    }

    public static boolean isActive(ItemStack stack) {
        return StackNBT.getBoolean(stack, ACTIVE);
    }

    public static void setActive(ItemStack stack, boolean active) {
        if (active) {
            StackNBT.setBoolean(stack, ACTIVE, true);
        } else {
            StackNBT.remove(stack, ACTIVE);
        }
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.UNCOMMON;
    }

    @Override
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        return CurseMaterials.isNetheriteScrap(repair);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return CHARGE_TICKS;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.BOW;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (player.isSneaking() && !isActive(stack)) {
            player.setActiveHand(hand);
            if (!world.isRemote) {
                player.attackEntityFrom(CurseDamageSources.EVIL_CURSE, 0.5F);
            }
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }
        return new ActionResult<>(EnumActionResult.PASS, stack);
    }

    @Override
    public void onUsingTick(ItemStack stack, EntityLivingBase entity, int count) {
        int used = getMaxItemUseDuration(stack) - count;
        if (!entity.world.isRemote && used > 10 && used % 16 == 0) {
            entity.attackEntityFrom(CurseDamageSources.EVIL_CURSE, 0.25F);
        }
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World world, EntityLivingBase entity) {
        if (entity instanceof EntityPlayer && !world.isRemote) {
            EntityPlayer player = (EntityPlayer) entity;
            setActive(stack, true);
            world.playSound(null, player.posX, player.posY, player.posZ, EnigmaticLegacy.CHARGED_ON_SOUND, SoundCategory.PLAYERS, 2.5F, 0.3F);
            player.attackEntityFrom(CurseDamageSources.EVIL_CURSE, 1.0F);
            stack.damageItem(10, player);
        }
        return stack;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        if (CurseTooltips.shift()) {
            String percent = ((CurseConfig.curseBladeAmplifier + 1) * 5) + "%";
            CurseTooltips.line(tooltip, "tooltip.enigmaticdelicacy.curseBlade1");
            CurseTooltips.line(tooltip, "tooltip.enigmaticdelicacy.curseBlade2", percent);
            CurseTooltips.line(tooltip, "tooltip.enigmaticdelicacy.curseBlade3", percent);
            if (CurseTooltips.localPlayerHasCursedRing()) {
                CurseTooltips.empty(tooltip);
                CurseTooltips.line(tooltip, "tooltip.enigmaticdelicacy.curseBlade4");
            }
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
