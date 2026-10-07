package net.mx.edelicacy.curse.item.soul;

import java.util.List;

import javax.annotation.Nullable;

import baubles.api.BaubleType;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mx.edelicacy.curse.CurseConfig;
import net.mx.edelicacy.curse.client.CurseTooltips;
import net.mx.edelicacy.curse.util.CurseItemEntities;
import net.mx.edelicacy.item.base.DelicacyBaubleItem;

/** 咒魂晶坠（项链）：效果见 {@link CurseCrystalPendantHandler}；咒魂值变化时同步给客户端用于提示 */
public class ItemCurseCrystalPendant extends DelicacyBaubleItem {

    public ItemCurseCrystalPendant() {
        super("curse_crystal_pendant", BaubleType.AMULET);
        rarity(EnumRarity.RARE);
    }

    @Override
    public boolean willAutoSync(ItemStack stack, EntityLivingBase player) {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        CurseTooltips.empty(tooltip);
        if (CurseTooltips.shift()) {
            CurseTooltips.line(tooltip, "tooltip.enigmaticdelicacy.curseCrystalPendant1", CurseConfig.pendantRecoveryRate + "%");
            CurseTooltips.line(tooltip, "tooltip.enigmaticdelicacy.curseCrystalPendant2", CurseConfig.pendantHealingModifier + "%");
            CurseTooltips.line(tooltip, "tooltip.enigmaticdelicacy.curseCrystalPendant3");
            CurseTooltips.line(tooltip, "tooltip.enigmaticdelicacy.curseCrystalPendant4");
            CurseTooltips.line(tooltip, "tooltip.enigmaticdelicacy.curseCrystalPendant5");
        } else {
            CurseTooltips.holdShift(tooltip);
        }
        CurseTooltips.empty(tooltip);
        CurseTooltips.line(tooltip, "tooltip.enigmaticdelicacy.curseSoulAmount", SoulNBT.getSoulAmount(stack));
        if (stack.isItemEnchanted()) {
            CurseTooltips.empty(tooltip);
        }
    }

    @Override
    public boolean hasCustomEntity(ItemStack stack) {
        return true;
    }

    @Override
    @Nullable
    public Entity createEntity(World world, Entity location, ItemStack stack) {
        return CurseItemEntities.fireproof(world, location, stack);
    }
}
