package net.mx.edelicacy.fading;

import java.util.List;

import javax.annotation.Nullable;

import baubles.api.BaubleType;
import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import keletu.enigmaticlegacy.util.interfaces.IScroll;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mx.edelicacy.item.base.DelicacyBaubleItem;
import net.mx.edelicacy.registry.DelicacyPotions;
import net.mx.edelicacy.util.TooltipHelper;

/**
 * 失色卷轴（TRINKET，EL 卷轴类）：不在冷却时持续给予失色；失色被消耗后冷却 {@link FadingConfig#scrollCooldown} tick。
 * <p>实现 EL 的 IScroll：与 EL 卷轴互斥（同时只能戴一个卷轴），并由 EL 渲染环绕玩家的卷轴。
 * 1.21 每 tick 重新添加效果；1.12 每次添加都会发包，这里只在剩余时间低于阈值时续期。
 */
public class FadingScrollItem extends DelicacyBaubleItem implements IScroll {

    private static final int DURATION = 810;
    private static final int REFRESH_BELOW = 790;

    public FadingScrollItem() {
        super("fading_scroll", BaubleType.TRINKET);
    }

    @Override
    public boolean canEquip(ItemStack stack, EntityLivingBase wearer) {
        if (!super.canEquip(stack, wearer)) {
            return false;
        }
        if (wearer instanceof EntityPlayer) {
            IBaublesItemHandler baubles = BaublesApi.getBaublesHandler((EntityPlayer) wearer);
            for (int i = 0; i < baubles.getSlots(); i++) {
                ItemStack equipped = baubles.getStackInSlot(i);
                if (!equipped.isEmpty() && equipped.getItem() instanceof IScroll) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onWornTick(ItemStack stack, EntityLivingBase wearer) {
        super.onWornTick(stack, wearer);
        Potion fading = DelicacyPotions.FADING;
        if (fading == null || wearer.world.isRemote || !(wearer instanceof EntityPlayer)
                || ((EntityPlayer) wearer).getCooldownTracker().hasCooldown(this)) {
            return;
        }
        PotionEffect current = wearer.getActivePotionEffect(fading);
        if (current == null || current.getDuration() < REFRESH_BELOW) {
            wearer.addPotionEffect(new PotionEffect(fading, DURATION, 0, false, false));
        }
    }

    @Override
    public void onUnequipped(ItemStack stack, EntityLivingBase wearer) {
        super.onUnequipped(stack, wearer);
        Potion fading = DelicacyPotions.FADING;
        if (fading != null && !wearer.world.isRemote && wearer.isPotionActive(fading)) {
            wearer.removePotionEffect(fading);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        TooltipHelper.empty(tooltip);
        if (TooltipHelper.shiftDown()) {
            TooltipHelper.line(tooltip, "tooltip.enigmaticdelicacy.fadingScroll1");
            // 1.21 把 tick 数当秒显示，这里换算成秒
            TooltipHelper.line(tooltip, "tooltip.enigmaticdelicacy.fadingScroll2", seconds(FadingConfig.scrollCooldown));
        } else {
            TooltipHelper.holdShift(tooltip);
        }
    }

    private static String seconds(int ticks) {
        return ticks % 20 == 0 ? String.valueOf(ticks / 20) : String.format("%.1f", ticks / 20.0F);
    }
}
