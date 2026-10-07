package net.mx.edelicacy.food.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import net.mx.edelicacy.item.base.DelicacyFoodItem;

/**
 * 在 {@link DelicacyFoodItem} 上补「隐藏粒子」：1.21 的失色类食物以 visible=false 施加效果，
 * 否则失色（强化隐身）会被药水粒子暴露。基类的效果施加不带粒子开关，这里整体接管。
 */
public class EffectFoodItem extends DelicacyFoodItem {

    private boolean hideParticles;

    public EffectFoodItem(String name, int hunger, float saturation) {
        super(name, hunger, saturation);
    }

    public EffectFoodItem hideParticles() {
        hideParticles = true;
        return this;
    }

    @Override
    protected void onFoodEaten(ItemStack stack, World world, EntityPlayer player) {
        if (!world.isRemote) {
            for (FoodEffect effect : effects) {
                Potion potion = effect.potion.get();
                if (potion != null && (effect.chance >= 1.0F || world.rand.nextFloat() < effect.chance)) {
                    player.addPotionEffect(new PotionEffect(potion, effect.duration, effect.amplifier, false, !hideParticles));
                }
            }
        }
        onEaten(stack, world, player);
    }
}
