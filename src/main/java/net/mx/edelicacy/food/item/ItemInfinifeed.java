package net.mx.edelicacy.food.item;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mx.edelicacy.item.base.DelicacyItem;
import net.mx.edelicacy.util.TooltipHelper;

/** 野性滋养精华：无限的动物饲料，不消耗；幼崽加速长大，成年动物进入求偶。发射器也能用（见 InfinifeedDispenseBehavior） */
public class ItemInfinifeed extends DelicacyItem {

    public ItemInfinifeed() {
        super("infinifeed");
        rarity(EnumRarity.UNCOMMON);
        stackSize(1);
        setContainerItem(Items.BOWL);
    }

    /**
     * 对目标生效；返回是否生效。
     *
     * @param player 使用者，发射器为 null（求偶不记繁殖者）
     */
    public static boolean tryApply(World world, EntityLivingBase target, @Nullable EntityPlayer player) {
        if (!(target instanceof EntityAnimal)) {
            return false;
        }
        EntityAnimal animal = (EntityAnimal) target;
        int age = animal.getGrowingAge();
        if (animal.isChild()) {
            if (world instanceof WorldServer) {
                // 与 1.21 一致：至少 120 秒，或剩余成长时间的 1/30（单位秒）
                animal.ageUp(Math.max(-age / 30, 120), true);
                ((WorldServer) world).spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, animal.posX,
                        animal.posY + animal.getEyeHeight(), animal.posZ, 6, animal.width / 2, animal.height / 2, animal.width / 2, 0.0D);
            }
            return true;
        }
        if (age == 0 && !animal.isInLove()) {
            if (!world.isRemote) {
                animal.setInLove(player);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer player, EntityLivingBase target, EnumHand hand) {
        return tryApply(target.world, target, player);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        TooltipHelper.line(tooltip, "tooltip.enigmaticdelicacy.infinifeed1");
    }
}
