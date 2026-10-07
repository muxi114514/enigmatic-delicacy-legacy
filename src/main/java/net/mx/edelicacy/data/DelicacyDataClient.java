package net.mx.edelicacy.data;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** 客户端：把同步来的数据写进本地玩家（调度到主线程） */
@SideOnly(Side.CLIENT)
final class DelicacyDataClient {

    private DelicacyDataClient() {
    }

    static void schedule(NBTTagCompound tag) {
        Minecraft mc = Minecraft.getMinecraft();
        mc.addScheduledTask(() -> {
            if (mc.player != null) {
                DelicacyData data = mc.player.getCapability(DelicacyData.CAPABILITY, null);
                if (data != null) {
                    data.deserialize(tag);
                }
            }
        });
    }
}
