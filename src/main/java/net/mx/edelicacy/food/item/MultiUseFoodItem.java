package net.mx.edelicacy.food.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.mx.edelicacy.util.StackNBT;

/**
 * 可多次食用的食物（对应 1.21 用数据组件记次数的果酱、以太牛排）：剩余次数存 NBT，没有标签视为满次数；
 * 吃一口只扣次数，次数用尽时由子类决定变成什么。创造模式不扣次数。耐久条显示剩余次数。
 */
public abstract class MultiUseFoodItem extends EffectFoodItem {

    public static final String USES = "Uses";

    private final int maxUses;

    public MultiUseFoodItem(String name, int hunger, float saturation, int maxUses) {
        super(name, hunger, saturation);
        this.maxUses = maxUses;
        stackSize(1);
    }

    public int getMaxUses() {
        return maxUses;
    }

    public int getUses(ItemStack stack) {
        return Math.max(0, Math.min(maxUses, StackNBT.getInt(stack, USES, maxUses)));
    }

    public void setUses(ItemStack stack, int uses) {
        StackNBT.setInt(stack, USES, Math.max(0, Math.min(maxUses, uses)));
    }

    /** 扣一次后返回手里应有的物品 */
    protected abstract ItemStack consumeUse(ItemStack stack, EntityPlayer player);

    /** 与 ItemFood 的食用流程相同，只是不直接 shrink */
    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World world, EntityLivingBase entity) {
        if (!(entity instanceof EntityPlayer)) {
            return stack;
        }
        EntityPlayer player = (EntityPlayer) entity;
        player.getFoodStats().addStats(this, stack);
        world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_PLAYER_BURP, SoundCategory.PLAYERS,
                0.5F, world.rand.nextFloat() * 0.1F + 0.9F);
        onFoodEaten(stack, world, player);
        player.addStat(StatList.getObjectUseStats(this));
        if (player instanceof EntityPlayerMP) {
            CriteriaTriggers.CONSUME_ITEM.trigger((EntityPlayerMP) player, stack);
        }
        return player.capabilities.isCreativeMode ? stack : consumeUse(stack, player);
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return getUses(stack) < maxUses;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return 1.0D - (double) getUses(stack) / maxUses;
    }
}
