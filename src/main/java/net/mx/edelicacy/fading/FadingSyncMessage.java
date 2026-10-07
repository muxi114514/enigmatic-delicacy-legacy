package net.mx.edelicacy.fading;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.mx.edelicacy.fading.client.FadingClient;

/** 服务端 → 客户端：某实体进入 / 离开失色状态（客户端据此不渲染它） */
public class FadingSyncMessage implements IMessage {

    private int entityId;
    private boolean active;

    public FadingSyncMessage() {
    }

    public FadingSyncMessage(int entityId, boolean active) {
        this.entityId = entityId;
        this.active = active;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        entityId = buf.readInt();
        active = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeBoolean(active);
    }

    /** 网络线程收到后交给客户端主线程处理 */
    public static class Handler implements IMessageHandler<FadingSyncMessage, IMessage> {
        @Override
        public IMessage onMessage(FadingSyncMessage message, MessageContext ctx) {
            FadingClient.schedule(message.entityId, message.active);
            return null;
        }
    }
}
