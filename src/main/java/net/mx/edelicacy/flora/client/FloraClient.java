package net.mx.edelicacy.flora.client;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockLeaves;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mx.edelicacy.flora.tree.AstralSaplingBlock;
import net.mx.edelicacy.flora.wood.AstralSlabBlock;
import net.mx.edelicacy.registry.DelicacyBlocks;

/**
 * 植物模块的客户端注册：物品模型、需要忽略属性的方块状态映射、粒子贴图。
 */
@SideOnly(Side.CLIENT)
public final class FloraClient {

    private FloraClient() {
    }

    public static void preInit() {
        MinecraftForge.EVENT_BUS.register(new AstralLeafFx.SpriteRegistrar());
    }

    public static void registerModels(Iterable<Item> items) {
        for (Item item : items) {
            ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
        }
        // 与原版一致：树叶不区分凋落属性，门不区分红石充能，树苗不区分阶段，台阶的占位变种不进模型
        ignore(DelicacyBlocks.ASTRAL_LEAVES, new StateMap.Builder().ignore(BlockLeaves.CHECK_DECAY, BlockLeaves.DECAYABLE));
        ignore(DelicacyBlocks.BLOSSOMING_ASTRAL_LEAVES, new StateMap.Builder().ignore(BlockLeaves.CHECK_DECAY, BlockLeaves.DECAYABLE));
        ignore(DelicacyBlocks.ASTRAL_DOOR, new StateMap.Builder().ignore(BlockDoor.POWERED));
        ignore(DelicacyBlocks.ASTRAL_SAPLING, new StateMap.Builder().ignore(AstralSaplingBlock.STAGE));
        ignore(DelicacyBlocks.ASTRAL_SLAB, new StateMap.Builder().ignore(AstralSlabBlock.VARIANT));
        ignore(DelicacyBlocks.ASTRAL_DOUBLE_SLAB, new StateMap.Builder().ignore(AstralSlabBlock.VARIANT));
    }

    private static void ignore(Block block, StateMap.Builder builder) {
        if (block != null) {
            ModelLoader.setCustomStateMapper(block, builder.build());
        }
    }
}
