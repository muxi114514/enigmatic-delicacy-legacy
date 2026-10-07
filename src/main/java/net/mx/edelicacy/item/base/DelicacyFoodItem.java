package net.mx.edelicacy.item.base;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * 食物基类（对应 1.21 的 FoodProperties + FD ConsumableItem）：
 * 支持多个药水效果、快速食用、食用后返还容器（碗、瓶等），并在提示里列出效果。
 * <p>药水用 Supplier 给出：构造时本模组的药水还没注册，到吃的时候才取。
 * 子类覆写 {@link #onEaten} 追加效果。
 */
public class DelicacyFoodItem extends ItemFood {

    /** 时长取此值视为「无限」（1.21 的 -1） */
    public static final int INFINITE = Integer.MAX_VALUE;

    protected final List<FoodEffect> effects = new ArrayList<>();
    private ItemStack container = ItemStack.EMPTY;
    private int useDuration = 32;
    private EnumRarity rarity = EnumRarity.COMMON;
    private boolean showEffects = true;

    public DelicacyFoodItem(String name, int hunger, float saturation) {
        super(hunger, saturation, false);
        DelicacyItem.setup(this, name);
    }

    public DelicacyFoodItem alwaysEdible() {
        setAlwaysEdible();
        return this;
    }

    /** 1.21 的 fast()：食用时间 16 tick */
    public DelicacyFoodItem fast() {
        useDuration = 16;
        return this;
    }

    public DelicacyFoodItem useDuration(int ticks) {
        useDuration = ticks;
        return this;
    }

    public DelicacyFoodItem effect(Supplier<Potion> potion, int duration, int amplifier) {
        return effect(potion, duration, amplifier, 1.0F);
    }

    public DelicacyFoodItem effect(Supplier<Potion> potion, int duration, int amplifier, float chance) {
        effects.add(new FoodEffect(potion, duration, amplifier, chance));
        return this;
    }

    public DelicacyFoodItem container(Item item) {
        container = new ItemStack(item);
        return this;
    }

    public DelicacyFoodItem stackSize(int size) {
        setMaxStackSize(size);
        return this;
    }

    public DelicacyFoodItem rarity(EnumRarity rarity) {
        this.rarity = rarity;
        return this;
    }

    public DelicacyFoodItem hideEffects() {
        showEffects = false;
        return this;
    }

    public ItemStack getContainer() {
        return container;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return useDuration;
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return rarity;
    }

    @Override
    protected void onFoodEaten(ItemStack stack, World world, EntityPlayer player) {
        if (!world.isRemote) {
            for (FoodEffect effect : effects) {
                Potion potion = effect.potion.get();
                if (potion != null && (effect.chance >= 1.0F || world.rand.nextFloat() < effect.chance)) {
                    player.addPotionEffect(new PotionEffect(potion, effect.duration, effect.amplifier));
                }
            }
        }
        onEaten(stack, world, player);
    }

    /** 吃完后的额外效果（两端都会调用，自行判断 isRemote） */
    protected void onEaten(ItemStack stack, World world, EntityPlayer player) {
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World world, EntityLivingBase entity) {
        ItemStack result = super.onItemUseFinish(stack, world, entity);
        if (container.isEmpty() || (entity instanceof EntityPlayer && ((EntityPlayer) entity).capabilities.isCreativeMode)) {
            return result;
        }
        if (result.isEmpty()) {
            return container.copy();
        }
        if (entity instanceof EntityPlayer && !world.isRemote) {
            EntityPlayer player = (EntityPlayer) entity;
            if (!player.inventory.addItemStackToInventory(container.copy())) {
                player.dropItem(container.copy(), false);
            }
        }
        return result;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        if (showEffects) {
            addEffectTooltip(tooltip);
        }
    }

    @SideOnly(Side.CLIENT)
    protected void addEffectTooltip(List<String> tooltip) {
        for (FoodEffect effect : effects) {
            Potion potion = effect.potion.get();
            if (potion == null) {
                continue;
            }
            StringBuilder line = new StringBuilder(I18n.format(potion.getName()));
            if (effect.amplifier > 0) {
                line.append(' ').append(I18n.format("enchantment.level." + (effect.amplifier + 1)));
            }
            if (effect.duration != INFINITE && effect.duration > 20) {
                line.append(" (").append(Potion.getPotionDurationString(new PotionEffect(potion, effect.duration), 1.0F)).append(')');
            }
            if (effect.chance < 1.0F) {
                line.append(' ').append(Math.round(effect.chance * 100)).append('%');
            }
            tooltip.add((potion.isBadEffect() ? TextFormatting.RED : TextFormatting.BLUE) + line.toString());
        }
    }

    /** 一个食用效果 */
    public static final class FoodEffect {
        public final Supplier<Potion> potion;
        public final int duration;
        public final int amplifier;
        public final float chance;

        public FoodEffect(Supplier<Potion> potion, int duration, int amplifier, float chance) {
            this.potion = potion;
            this.duration = duration;
            this.amplifier = amplifier;
            this.chance = chance;
        }
    }
}
