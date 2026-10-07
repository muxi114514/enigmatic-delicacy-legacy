package net.mx.edelicacy.food.item;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mx.edelicacy.registry.DelicacyPotions;
import net.mx.edelicacy.util.TooltipHelper;

/**
 * 谜之果酱：4 次，吃一口获得失色（隐藏粒子），用完剩玻璃瓶；也可以对动物使用，给它失色并扣一次。
 * <p>修正 1.21 的问题：对动物用掉最后一次后果酱仍留在手里且次数为 0，可以无限对动物使用；这里改为直接变成玻璃瓶。
 */
public class ItemEnigmaticJam extends MultiUseFoodItem {

    private static final int ANIMAL_FADING_TICKS = 600;

    public ItemEnigmaticJam() {
        super("enigmatic_jam", 4, 1.0F, 4);
        alwaysEdible();
        hideParticles();
        useDuration(30);
        effect(() -> DelicacyPotions.FADING, 600, 0);
    }

    @Override
    protected ItemStack consumeUse(ItemStack stack, EntityPlayer player) {
        int uses = getUses(stack) - 1;
        if (uses > 0) {
            setUses(stack, uses);
            return stack;
        }
        return new ItemStack(Items.GLASS_BOTTLE);
    }

    @Override
    public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer player, EntityLivingBase target, EnumHand hand) {
        if (!(target instanceof EntityAnimal)) {
            return false;
        }
        if (!player.world.isRemote) {
            Potion fading = DelicacyPotions.FADING;
            if (fading != null) {
                target.addPotionEffect(new PotionEffect(fading, ANIMAL_FADING_TICKS, 0, false, true));
            }
            if (!player.capabilities.isCreativeMode) {
                player.setHeldItem(hand, consumeUse(stack, player));
            }
        }
        return true;
    }

    @Override
    public int getRGBDurabilityForDisplay(ItemStack stack) {
        return 0xEEEEEE;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        TooltipHelper.line(tooltip, "tooltip.enigmaticdelicacy.enigmaticJam");
        super.addInformation(stack, world, tooltip, flag);
    }
}
