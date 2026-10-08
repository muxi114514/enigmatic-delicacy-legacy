package net.mx.edelicacy.charm.item;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mx.edelicacy.charm.CharmConfig;
import net.mx.edelicacy.charm.glistening.GlisteningResistance;
import net.mx.edelicacy.charm.glistening.GlisteningShield;
import net.mx.edelicacy.item.base.DelicacyDrinkItem;
import net.mx.edelicacy.registry.DelicacyItems;
import net.mx.edelicacy.registry.DelicacyPotions;
import net.mx.edelicacy.util.EnigmaticBridge;
import net.mx.edelicacy.util.TooltipHelper;

/**
 * 辉光茶：坚韧 II 40 秒、80% 发光 20 秒；
 * 重置守护护符冷却并 +10% 减伤，闪耀之书护盾充能 1/8 上限。
 */
public class GlisteningTeaItem extends DelicacyDrinkItem {

    private static final int CHARM_BONUS = 10;

    public GlisteningTeaItem() {
        super("glistening_tea", 0, 0.0F);
        effect(() -> DelicacyPotions.TENACITY, 800, 1);
        effect(() -> MobEffects.GLOWING, 400, 0, 0.8F);
    }

    @Override
    protected void onEaten(ItemStack stack, World world, EntityPlayer player) {
        if (world.isRemote) {
            return;
        }
        Item charmItem = DelicacyItems.GLISTENING_CHARM;
        if (charmItem != null) {
            // removeCooldown 是客户端方法；设为 0 tick 双端通用，下一 tick 清掉并同步给客户端
            player.getCooldownTracker().setCooldown(charmItem, 0);
            ItemStack charm = EnigmaticBridge.getBauble(player, charmItem);
            if (!charm.isEmpty()) {
                GlisteningResistance.add(charm, CHARM_BONUS);
            }
        }
        GlisteningShield.Book book = GlisteningShield.locate(player);
        if (book != null) {
            int max = CharmConfig.glisteningBookThreshold;
            book.setShield(Math.min(book.shield() + max / 8, max));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        TooltipHelper.line(tooltip, "tooltip.enigmaticdelicacy.glisteningTea");
        TooltipHelper.empty(tooltip);
        addEffectTooltip(tooltip);
    }
}
