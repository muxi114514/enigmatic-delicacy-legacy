package net.mx.edelicacy.food;

import java.util.ArrayList;
import java.util.List;

import com.wdcftgg.farmersdelightlegacy.common.registry.ModEffects;

import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.mx.edelicacy.food.item.EffectFoodItem;
import net.mx.edelicacy.food.item.ItemBlazingMeatRoll;
import net.mx.edelicacy.food.item.ItemEnigmaticJam;
import net.mx.edelicacy.food.item.ItemEtheriumSteak;
import net.mx.edelicacy.food.item.ItemInfinifeed;
import net.mx.edelicacy.food.item.ItemRiceCake;
import net.mx.edelicacy.item.base.DelicacyFoodItem;
import net.mx.edelicacy.registry.DelicacyPotions;

/** 本模块全部物品的构造（数值取自 1.21 的 EnigmaticFoods；饱和度系数语义两版相同：饱和 = 饥饿值 × 系数 × 2） */
final class FoodItems {

    private FoodItems() {
    }

    static List<Item> create() {
        List<Item> items = new ArrayList<>();
        items.add(new ItemEnigmaticJam());
        items.add(new EffectFoodItem("enigmatic_cream", 5, 1.0F).hideParticles()
                .alwaysEdible().container(Items.BOWL).stackSize(16)
                .effect(() -> DelicacyPotions.FADING, 400, 0));
        items.add(bowl("astral_rice", 7, 0.6F)
                .effect(() -> DelicacyPotions.ASTRAL_DRUNKENNESS, 1000, 0)
                .effect(() -> ModEffects.NOURISHMENT, 3600, 0));
        items.add(new ItemRiceCake());
        items.add(bowl("starlight_salad", 7, 0.6F)
                .effect(() -> DelicacyPotions.ASTRAL_DRUNKENNESS, 800, 0)
                .effect(() -> MobEffects.SATURATION, 800, 0)
                .effect(() -> DelicacyPotions.TENACITY, 800, 0));
        items.add(bowl("ichoroot_soup", 8, 0.6F)
                .effect(() -> ModEffects.NOURISHMENT, 3600, 0)
                .effect(() -> MobEffects.ABSORPTION, 2400, 1));
        items.add(new DelicacyFoodItem("astral_fruit_slice", 4, 1.4F)
                .alwaysEdible().fast().rarity(EnumRarity.RARE)
                .effect(() -> MobEffects.REGENERATION, 300, 2)
                .effect(() -> MobEffects.STRENGTH, 2400, 2)
                .effect(() -> MobEffects.RESISTANCE, 2400, 1)
                .effect(() -> MobEffects.FIRE_RESISTANCE, 3000, 0));
        // 1.21 取 EL+ 宇宙蛋糕的食物属性（= 金胡萝卜）
        items.add(new DelicacyFoodItem("cosmic_cake_slice", 6, 1.2F).rarity(EnumRarity.UNCOMMON));
        items.add(new ItemEtheriumSteak());
        items.add(new ItemBlazingMeatRoll());
        items.add(new ItemInfinifeed());
        return items;
    }

    /** FD 的碗装食物：堆叠 16，吃完返还碗，总能吃 */
    private static DelicacyFoodItem bowl(String name, int hunger, float saturation) {
        return new DelicacyFoodItem(name, hunger, saturation).alwaysEdible().container(Items.BOWL).stackSize(16);
    }
}
