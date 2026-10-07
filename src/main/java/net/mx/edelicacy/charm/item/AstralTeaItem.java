package net.mx.edelicacy.charm.item;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mx.edelicacy.charm.AstralTranquility;
import net.mx.edelicacy.charm.client.CharmClient;
import net.mx.edelicacy.item.base.DelicacyDrinkItem;
import net.mx.edelicacy.util.EnigmaticBridge;
import net.mx.edelicacy.util.TooltipHelper;

/** 天体安神茶：给七咒之人一次正常入睡的机会（标记见 {@link AstralTranquility}） */
public class AstralTeaItem extends DelicacyDrinkItem {

    public AstralTeaItem() {
        super("astral_tea", 0, 0.0F);
    }

    @Override
    protected void onEaten(ItemStack stack, World world, EntityPlayer player) {
        if (!world.isRemote) {
            AstralTranquility.grant(player);
        }
    }

    /** 只有七咒之人才看得到效果说明（同 1.21），标题沿用 EL 1.12 泰迪熊的「第七诅咒的变化」 */
    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        if (EnigmaticBridge.isCursed(CharmClient.localPlayer())) {
            TooltipHelper.line(tooltip, "tooltip.enigmaticlegacy.compatXat3");
            TooltipHelper.line(tooltip, "tooltip.enigmaticdelicacy.astralTea");
        }
    }
}
