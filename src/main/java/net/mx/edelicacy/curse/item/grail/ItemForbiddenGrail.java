package net.mx.edelicacy.curse.item.grail;

import java.util.List;

import javax.annotation.Nullable;

import keletu.enigmaticlegacy.EnigmaticLegacy;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mx.edelicacy.curse.CurseConfig;
import net.mx.edelicacy.curse.client.CurseTooltips;
import net.mx.edelicacy.curse.item.forbidden.IForbiddenItem;
import net.mx.edelicacy.curse.potion.CursePotions;
import net.mx.edelicacy.curse.potion.StackedEffects;
import net.mx.edelicacy.curse.util.CurseItemEntities;
import net.mx.edelicacy.data.DelicacyData;
import net.mx.edelicacy.item.base.DelicacyItem;
import net.mx.edelicacy.util.EnigmaticBridge;

/**
 * 禁忌华盏（不消耗，冷却 16 秒）：仅禁忌之人可饮。饮后回复生命、获得一组增益，并进入禁忌状态（默认 120 秒），
 * 状态期间生命窃取 ×1.2、进食极快、每级禁忌之印抵挡一次伤害（见 {@link ForbiddenGrailHandler}）。
 */
public class ItemForbiddenGrail extends DelicacyItem implements IForbiddenItem {

    /** 1.21 回复 25 点、再被禁忌之果削减 80%；EL 1.12 不削减大额治疗，直接给实际值 */
    private static final float HEAL = 5.0F;

    public ItemForbiddenGrail() {
        super("forbidden_grail");
        rarity(EnumRarity.EPIC);
        stackSize(1);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 64;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.DRINK;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!EnigmaticBridge.isForbidden(player)) {
            return new ActionResult<>(EnumActionResult.PASS, stack);
        }
        player.setActiveHand(hand);
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World world, EntityLivingBase entity) {
        if (!(entity instanceof EntityPlayer) || world.isRemote) {
            return stack;
        }
        EntityPlayer player = (EntityPlayer) entity;
        if (!EnigmaticBridge.isForbidden(player)) {
            return stack;
        }
        player.heal(HEAL);
        world.playSound(null, player.posX, player.posY, player.posZ, EnigmaticLegacy.CHARGED_ON_SOUND, SoundCategory.PLAYERS, 1.0F, 0.25F);
        DelicacyData.get(player).setForbiddenTick(CurseConfig.forbiddenGrailDuration * 20);
        player.getCooldownTracker().setCooldown(this, player.capabilities.isCreativeMode ? 20 : 320);

        int m = EnigmaticBridge.isTheOne(player) ? 20 : 16;
        StackedEffects.add(player, new PotionEffect(MobEffects.RESISTANCE, 40 * m, 0, false, true));
        StackedEffects.add(player, new PotionEffect(MobEffects.RESISTANCE, 12 * m, 2, false, true));
        StackedEffects.add(player, new PotionEffect(MobEffects.STRENGTH, 40 * m, 1, false, true));
        StackedEffects.add(player, new PotionEffect(MobEffects.STRENGTH, 16 * m, 3, false, true));
        StackedEffects.add(player, new PotionEffect(MobEffects.SPEED, 40 * m, 0, false, true));
        if (CursePotions.FORBIDDEN_IMPRINT != null) {
            StackedEffects.add(player, new PotionEffect(CursePotions.FORBIDDEN_IMPRINT, 30 * m, 1, false, true));
        }
        return stack;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        if (CurseTooltips.shift()) {
            CurseTooltips.line(tooltip, "tooltip.enigmaticdelicacy.forbiddenGrail1");
            CurseTooltips.line(tooltip, "tooltip.enigmaticdelicacy.forbiddenGrail2", String.format("%.1f", HEAL * CurseTooltips.imprintHealFactor()));
            CurseTooltips.line(tooltip, "tooltip.enigmaticdelicacy.forbiddenGrail3", "1.2");
            CurseTooltips.line(tooltip, "tooltip.enigmaticdelicacy.forbiddenGrail4");
            CurseTooltips.line(tooltip, "tooltip.enigmaticdelicacy.forbiddenGrail5");
            CurseTooltips.line(tooltip, "tooltip.enigmaticdelicacy.forbiddenGrail6");
        } else {
            EntityPlayer player = CurseTooltips.localPlayer();
            int tick = player == null ? 0 : DelicacyData.get(player).getForbiddenTick();
            if (tick > 0) {
                CurseTooltips.line(tooltip, "tooltip.enigmaticdelicacy.forbiddenGrailDuration", tick / 20);
                CurseTooltips.empty(tooltip);
            }
            CurseTooltips.holdShift(tooltip);
        }
        CurseTooltips.empty(tooltip);
        CurseTooltips.forbiddenOnly(tooltip);
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
