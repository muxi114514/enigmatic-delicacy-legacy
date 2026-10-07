package net.mx.edelicacy.item.base;

import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;

/** 饮品基类（对应 FD DrinkableItem / EL+ BaseDrinkableItem）：饮用动作，默认返还玻璃瓶、总能饮用 */
public class DelicacyDrinkItem extends DelicacyFoodItem {

    public DelicacyDrinkItem(String name, int hunger, float saturation) {
        super(name, hunger, saturation);
        alwaysEdible();
        container(Items.GLASS_BOTTLE);
        stackSize(16);
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.DRINK;
    }
}
