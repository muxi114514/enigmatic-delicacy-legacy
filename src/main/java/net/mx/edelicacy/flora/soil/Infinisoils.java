package net.mx.edelicacy.flora.soil;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.mx.edelicacy.registry.DelicacyBlocks;

/** 无尽沃土判定（供方块逻辑与 mixin 共用） */
public final class Infinisoils {

    private Infinisoils() {
    }

    /** 无尽沃土或无尽沃土耕地 */
    public static boolean isInfinisoil(IBlockState state) {
        return isInfinisoil(state.getBlock());
    }

    public static boolean isInfinisoil(Block block) {
        return block == DelicacyBlocks.INFINISOIL || block == DelicacyBlocks.INFINISOIL_FARMLAND;
    }

    /** 下一次计划刻延迟：6~12 tick（1.21 Mth.nextInt(6, 12)） */
    public static int nextTickDelay(java.util.Random rand) {
        return 6 + rand.nextInt(7);
    }
}
