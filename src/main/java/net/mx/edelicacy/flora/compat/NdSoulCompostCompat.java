package net.mx.edelicacy.flora.compat;

import net.minecraft.block.Block;
import xy177.nethersdelightlegacy.common.block.BlockSoulCompost;

/**
 * 下界乐事接缝：只有 ModCompat.nethersDelight 为真时才会加载本类（对应 1.21 的 MND 莱提奥斯堆肥）。
 */
public final class NdSoulCompostCompat {

    private NdSoulCompostCompat() {
    }

    public static boolean isSoulCompost(Block block) {
        return block instanceof BlockSoulCompost;
    }
}
