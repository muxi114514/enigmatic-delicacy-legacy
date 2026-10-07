package net.mx.edelicacy.network;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

/**
 * 网络通道。各模块在 preInit 里按固定顺序调用 {@link #register}，编号自增，两端一致。
 * 处理器运行在网络线程：服务端用 world.addScheduledTask、客户端交给 XxxClient 辅助类调度到主线程。
 */
public final class DelicacyNetwork {

    public static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel("enigmaticdelicacy");
    private static int nextId = 0;

    private DelicacyNetwork() {
    }

    public static <REQ extends IMessage, REPLY extends IMessage> void register(
            Class<? extends IMessageHandler<REQ, REPLY>> handler, Class<REQ> message, Side receivingSide) {
        CHANNEL.registerMessage(handler, message, nextId++, receivingSide);
    }
}
