package net.mx.edelicacy.data;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/** 服务端 → 本人：整份佳肴数据 */
public class DelicacyDataSyncMessage implements IMessage {

    private NBTTagCompound tag;

    public DelicacyDataSyncMessage() {
    }

    public DelicacyDataSyncMessage(NBTTagCompound tag) {
        this.tag = tag;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        tag = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, tag);
    }

    public static class Handler implements IMessageHandler<DelicacyDataSyncMessage, IMessage> {
        @Override
        public IMessage onMessage(DelicacyDataSyncMessage message, MessageContext ctx) {
            if (message.tag != null) {
                DelicacyDataClient.schedule(message.tag);
            }
            return null;
        }
    }
}
