package net.mx.edelicacy.food.item;

import java.util.List;

import javax.annotation.Nullable;

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
import net.mx.edelicacy.food.entity.EntityThrownBlazingMeatRoll;
import net.mx.edelicacy.registry.DelicacyPotions;
import net.mx.edelicacy.util.TooltipHelper;

/** 炽焰肉卷：吃下获得坚毅并着火 5 秒；潜行右键掷出，落点小范围爆燃（见 {@link EntityThrownBlazingMeatRoll}） */
public class ItemBlazingMeatRoll extends EffectFoodItem {

    private static final int IGNITE_SECONDS = 5;

    public ItemBlazingMeatRoll() {
        super("blazing_meat_roll", 8, 1.0F);
        alwaysEdible();
        stackSize(1);
        effect(() -> DelicacyPotions.PERSEVERANCE, 1200, 0);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!player.isSneaking()) {
            return super.onItemRightClick(world, player, hand);
        }
        world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.PLAYERS,
                0.5F, 0.4F / (world.rand.nextFloat() * 0.4F + 0.8F));
        if (!world.isRemote) {
            EntityThrownBlazingMeatRoll thrown = new EntityThrownBlazingMeatRoll(world, player);
            thrown.shoot(player, player.rotationPitch, player.rotationYaw, 0.0F, 0.9F, 0.0F);
            world.spawnEntity(thrown);
            if (!player.capabilities.isCreativeMode) {
                stack.shrink(1);
            }
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    protected void onEaten(ItemStack stack, World world, EntityPlayer player) {
        if (!world.isRemote) {
            player.setFire(IGNITE_SECONDS);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);
        TooltipHelper.line(tooltip, "tooltip.enigmaticdelicacy.blazingMeatRoll1");
    }
}
