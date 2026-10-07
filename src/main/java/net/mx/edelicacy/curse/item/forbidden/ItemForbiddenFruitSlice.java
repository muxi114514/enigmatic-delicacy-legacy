package net.mx.edelicacy.curse.item.forbidden;

import java.util.List;

import javax.annotation.Nullable;

import keletu.enigmaticlegacy.EnigmaticConfigs;
import keletu.enigmaticlegacy.api.cap.IForbiddenConsumed;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mx.edelicacy.curse.client.CurseTooltips;
import net.mx.edelicacy.curse.util.CurseItemEntities;
import net.mx.edelicacy.item.base.DelicacyFoodItem;
import net.mx.edelicacy.util.EnigmaticBridge;

/**
 * 禁忌之果切片：1.21 直接注册了一个 EL+ 的禁忌之果实例，即吃下后同样成为禁忌之人（只有还没吃过的人能吃）。
 * EL 1.12 的禁忌之果构造时写死了注册名，不能复用，这里按 EL+ 的数值复刻（凋零 IV、反胃 III、虚弱 IV、缓慢 III）。
 */
public class ItemForbiddenFruitSlice extends DelicacyFoodItem {

    public ItemForbiddenFruitSlice() {
        super("forbidden_fruit_slice", 2, 0.5F);
        alwaysEdible();
        stackSize(1);
        rarity(EnumRarity.RARE);
        hideEffects();
        effect(() -> MobEffects.WITHER, 400, 3);
        effect(() -> MobEffects.NAUSEA, 400, 2);
        effect(() -> MobEffects.WEAKNESS, 480, 3);
        effect(() -> MobEffects.SLOWNESS, 640, 2);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (EnigmaticBridge.isForbidden(player)) {
            return new ActionResult<>(EnumActionResult.FAIL, stack);
        }
        player.setActiveHand(hand);
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    protected void onEaten(ItemStack stack, World world, EntityPlayer player) {
        IForbiddenConsumed consumed = IForbiddenConsumed.get(player);
        if (consumed != null) {
            consumed.setConsumed(true);
        }
        if (!world.isRemote) {
            world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ENDERDRAGON_GROWL,
                    SoundCategory.PLAYERS, 0.5F, world.rand.nextFloat() * 0.1F + 0.9F);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        if (CurseTooltips.shift()) {
            CurseTooltips.line(tooltip, "tooltip.enigmaticlegacy.forbiddenFruit1");
            CurseTooltips.line(tooltip, "tooltip.enigmaticlegacy.forbiddenFruit2");
            CurseTooltips.line(tooltip, "tooltip.enigmaticlegacy.forbiddenFruit3", Math.round(EnigmaticConfigs.regenerationSubtraction * 100) + "%");
        } else {
            CurseTooltips.line(tooltip, "tooltip.enigmaticlegacy.forbiddenFruitLore");
            CurseTooltips.empty(tooltip);
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
