package net.mx.edelicacy.food.item;

import java.util.List;

import javax.annotation.Nullable;

import com.wdcftgg.farmersdelightlegacy.common.registry.ModEffects;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mx.edelicacy.registry.DelicacyPotions;
import net.mx.edelicacy.util.StackNBT;
import net.mx.edelicacy.util.TooltipHelper;

/**
 * 星叶粽：加蜂蜜后饱食 5→6、饱和 0.8→1.0。
 * <p>1.21 是在背包里把蜂蜜瓶点到粽子上；1.12 改为主手持粽子、副手持蜂蜜（矿辞 dropHoney / 未来版蜂蜜瓶 / 白糖）潜行右键，
 * 每次加一个。是否加过蜂蜜存 NBT，模型由 honey 属性切换。
 */
public class ItemRiceCake extends EffectFoodItem {

    public static final String HONEY = "Honey";

    public ItemRiceCake() {
        super("rice_cake_in_astral_leaf", 5, 0.8F);
        alwaysEdible();
        stackSize(1);
        effect(() -> DelicacyPotions.ASTRAL_DRUNKENNESS, 800, 0);
        effect(() -> ModEffects.NOURISHMENT, 800, 0);
    }

    public static boolean hasHoney(ItemStack stack) {
        return StackNBT.getBoolean(stack, HONEY);
    }

    @Override
    public int getHealAmount(ItemStack stack) {
        return hasHoney(stack) ? 6 : 5;
    }

    @Override
    public float getSaturationModifier(ItemStack stack) {
        return hasHoney(stack) ? 1.0F : 0.8F;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (hand == EnumHand.MAIN_HAND && player.isSneaking() && !hasHoney(stack) && HoneyHelper.isHoney(player.getHeldItemOffhand())) {
            if (!world.isRemote) {
                addHoney(stack, player);
            }
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }
        return super.onItemRightClick(world, player, hand);
    }

    private static void addHoney(ItemStack stack, EntityPlayer player) {
        ItemStack honey = player.getHeldItemOffhand();
        if (stack.getCount() > 1) {
            ItemStack single = stack.splitStack(1);
            StackNBT.setBoolean(single, HONEY, true);
            give(player, single);
        } else {
            StackNBT.setBoolean(stack, HONEY, true);
        }
        if (!player.capabilities.isCreativeMode) {
            ItemStack container = honey.getItem().hasContainerItem(honey) ? honey.getItem().getContainerItem(honey) : ItemStack.EMPTY;
            honey.shrink(1);
            if (!container.isEmpty()) {
                give(player, container);
            }
        }
        player.world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.BLOCK_SLIME_PLACE, SoundCategory.PLAYERS, 1.0F, 1.0F);
    }

    private static void give(EntityPlayer player, ItemStack stack) {
        if (!player.inventory.addItemStackToInventory(stack)) {
            player.dropItem(stack, false);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);
        if (!hasHoney(stack)) {
            TooltipHelper.line(tooltip, "tooltip.enigmaticdelicacy.riceCakeHoney");
        }
    }
}
