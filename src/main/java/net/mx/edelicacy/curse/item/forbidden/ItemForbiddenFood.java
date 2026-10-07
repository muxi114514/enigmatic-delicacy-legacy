package net.mx.edelicacy.curse.item.forbidden;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mx.edelicacy.curse.client.CurseTooltips;
import net.mx.edelicacy.item.base.DelicacyFoodItem;
import net.mx.edelicacy.util.EnigmaticBridge;

/**
 * 禁忌食物（1.21 的 BaseForbiddenFood）：只有吃过禁忌之果的人能吃（他们饥饿值被锁满，所以不看饥饿），吃完额外治疗。
 * <p>1.21 存 heal×5、再被禁忌之果的 -80% 治疗削成 heal；EL 1.12 只削减 ≤1 点的治疗，所以这里直接治疗 heal，
 * 实际回复量与 1.21 一致。
 */
public class ItemForbiddenFood extends DelicacyFoodItem implements IForbiddenItem {

    private final float heal;

    public ItemForbiddenFood(String name, int hunger, float saturation, float heal) {
        super(name, hunger, saturation);
        this.heal = heal;
        rarity(EnumRarity.UNCOMMON);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!EnigmaticBridge.isForbidden(player)) {
            return new ActionResult<>(EnumActionResult.FAIL, stack);
        }
        player.setActiveHand(hand);
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    protected void onEaten(ItemStack stack, World world, EntityPlayer player) {
        if (!world.isRemote && heal > 0) {
            player.heal(heal);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);
        CurseTooltips.forbiddenHeal(tooltip, heal);
        CurseTooltips.empty(tooltip);
        CurseTooltips.forbiddenOnly(tooltip);
    }
}
