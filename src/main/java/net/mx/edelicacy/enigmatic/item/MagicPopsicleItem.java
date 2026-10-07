package net.mx.edelicacy.enigmatic.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mx.edelicacy.compat.ModCompat;
import net.mx.edelicacy.enigmatic.api.EnigmaticColor;
import net.mx.edelicacy.registry.DelicacyPotions;

/**
 * 魔法冰棍：蓝色谜之食物，海豚的恩惠，自带附魔光效。
 * <p>1.21 的 Iron's Spells 瞬间法力（25 + 5% 上限，默认上限 100 即 30 点）换成 Trinkets and Baubles 回魔 30 点。
 * 吃完返还末影之杖（容器在 init 时设置，EL 物品那时已注册）。
 */
public class MagicPopsicleItem extends EnigmaticFoodItem {

    private static final float MANA_REFILL = 30.0F;

    public MagicPopsicleItem() {
        super("magic_popsicle", 3, 0.4F, EnigmaticColor.BLUE);
        effect(() -> DelicacyPotions.DOLPHINS_GRACE, 1200, 0);
    }

    @Override
    protected void onEaten(ItemStack stack, World world, EntityPlayer player) {
        super.onEaten(stack, world, player);
        if (!world.isRemote) {
            ModCompat.refillMana(player, MANA_REFILL);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean hasEffect(ItemStack stack) {
        return true;
    }
}
