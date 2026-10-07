package net.mx.edelicacy.integration.waila;

import java.util.List;

import javax.annotation.Nonnull;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.mx.edelicacy.machine.pan.TileVoraciousPan;
import net.mx.edelicacy.machine.stove.TileEtheriumStove;

/**
 * 以太炉与饕餮之锅的 Hwyla 信息。进度只在服务端准确，所以由服务端打包进 NBT 再在客户端显示。
 * 本类两端都会加载，文本用两端都有的 translation.I18n，不引用客户端类。
 */
@SuppressWarnings("deprecation")
public class MachineWailaProvider implements IWailaDataProvider {

    @Nonnull
    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, BlockPos pos) {
        if (te instanceof TileEtheriumStove) {
            TileEtheriumStove stove = (TileEtheriumStove) te;
            tag.setBoolean("Lit", stove.isLit());
            tag.setTag("Input", stove.getFurnaceInput().writeToNBT(new NBTTagCompound()));
            tag.setInteger("Progress", percent(stove.getFurnaceProgress(), stove.getFurnaceProgressTotal()));
            NBTTagList grill = new NBTTagList();
            for (int i = 0; i < TileEtheriumStove.GRILL_SLOTS; i++) {
                ItemStack stack = stove.getGrillStack(i);
                if (!stack.isEmpty()) {
                    NBTTagCompound entry = stack.writeToNBT(new NBTTagCompound());
                    entry.setInteger("Progress", percent(stove.getGrillCookTime(i), stove.getGrillCookTimeTotal(i)));
                    grill.appendTag(entry);
                }
            }
            tag.setTag("Grill", grill);
        } else if (te instanceof TileVoraciousPan) {
            tag.setInteger("Points", ((TileVoraciousPan) te).getPoints());
        }
        return tag;
    }

    @Nonnull
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        NBTTagCompound tag = accessor.getNBTData();
        if (accessor.getTileEntity() instanceof TileEtheriumStove) {
            tooltip.add(I18n.translateToLocal(tag.getBoolean("Lit") ? "hwyla.enigmaticdelicacy.lit" : "hwyla.enigmaticdelicacy.unlit"));
            ItemStack input = new ItemStack(tag.getCompoundTag("Input"));
            if (!input.isEmpty()) {
                tooltip.add(I18n.translateToLocalFormatted("hwyla.enigmaticdelicacy.smelting", input.getDisplayName(), tag.getInteger("Progress")));
            }
            NBTTagList grill = tag.getTagList("Grill", 10);
            for (int i = 0; i < grill.tagCount(); i++) {
                NBTTagCompound entry = grill.getCompoundTagAt(i);
                tooltip.add(I18n.translateToLocalFormatted("hwyla.enigmaticdelicacy.grill", new ItemStack(entry).getDisplayName(), entry.getInteger("Progress")));
            }
        } else if (accessor.getTileEntity() instanceof TileVoraciousPan) {
            tooltip.add(TextFormatting.DARK_PURPLE + I18n.translateToLocalFormatted("gui.enigmaticdelicacy.voracious_pan.forbidden_point", tag.getInteger("Points")));
        }
        return tooltip;
    }

    private static int percent(int progress, int total) {
        return total <= 0 ? 0 : Math.min(100, progress * 100 / total);
    }
}
