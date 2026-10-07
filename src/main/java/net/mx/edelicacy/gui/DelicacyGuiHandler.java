package net.mx.edelicacy.gui;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

/**
 * 统一的 GUI 处理器。模块在 preInit 注册容器工厂，在 clientPreInit（经 XxxClient）注册界面工厂。
 * GUI 编号见下方常量。
 */
public class DelicacyGuiHandler implements IGuiHandler {

    public static final int ETHERIUM_STOVE = 0;
    public static final int VORACIOUS_PAN = 1;

    /** (玩家, 世界, 坐标) → 容器或界面；拿不到方块实体时返回 null */
    @FunctionalInterface
    public interface Factory {
        Object create(EntityPlayer player, World world, BlockPos pos);
    }

    private static final Map<Integer, Factory> SERVER = new ConcurrentHashMap<>();
    private static final Map<Integer, Factory> CLIENT = new ConcurrentHashMap<>();

    public static void registerContainer(int id, Factory factory) {
        SERVER.put(id, factory);
    }

    public static void registerScreen(int id, Factory factory) {
        CLIENT.put(id, factory);
    }

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        Factory factory = SERVER.get(id);
        return factory == null ? null : factory.create(player, world, new BlockPos(x, y, z));
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        Factory factory = CLIENT.get(id);
        return factory == null ? null : factory.create(player, world, new BlockPos(x, y, z));
    }
}
