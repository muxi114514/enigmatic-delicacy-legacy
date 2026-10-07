package net.mx.edelicacy.machine;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import net.mx.edelicacy.EnigmaticDelicacy;
import net.mx.edelicacy.gui.DelicacyGuiHandler;
import net.mx.edelicacy.item.base.DelicacyItem;
import net.mx.edelicacy.machine.client.MachineClient;
import net.mx.edelicacy.machine.pan.BlockVoraciousPan;
import net.mx.edelicacy.machine.pan.ContainerVoraciousPan;
import net.mx.edelicacy.machine.pan.TileVoraciousPan;
import net.mx.edelicacy.machine.pan.VoraciousPanEvents;
import net.mx.edelicacy.machine.recipe.AbyssalCookingDefaults;
import net.mx.edelicacy.machine.stove.BlockEtheriumStove;
import net.mx.edelicacy.machine.stove.ContainerEtheriumStove;
import net.mx.edelicacy.machine.stove.TileEtheriumStove;
import net.mx.edelicacy.module.IDelicacyModule;

/**
 * 机器模块：以太炉灶（烤架 + 无燃料熔炉 + 烹饪锅加速）、饕餮之锅（并入神秘遗物的 eldritch_pan，放下后做禁忌转化）、禁忌转化配方表。
 */
public final class MachineModule implements IDelicacyModule {

    private BlockEtheriumStove stove;
    private BlockVoraciousPan pan;
    private Item stoveItem;

    @Override
    public void preInit(Configuration config) {
        MachineConfig.load(config);
        GameRegistry.registerTileEntity(TileEtheriumStove.class, new ResourceLocation(EnigmaticDelicacy.MODID, "etherium_stove"));
        GameRegistry.registerTileEntity(TileVoraciousPan.class, new ResourceLocation(EnigmaticDelicacy.MODID, "voracious_pan"));
        DelicacyGuiHandler.registerContainer(DelicacyGuiHandler.ETHERIUM_STOVE, (player, world, pos) -> {
            TileEntity tile = world.getTileEntity(pos);
            return tile instanceof TileEtheriumStove ? new ContainerEtheriumStove(player.inventory, (TileEtheriumStove) tile) : null;
        });
        DelicacyGuiHandler.registerContainer(DelicacyGuiHandler.VORACIOUS_PAN, (player, world, pos) -> {
            TileEntity tile = world.getTileEntity(pos);
            return tile instanceof TileVoraciousPan ? new ContainerVoraciousPan(player.inventory, (TileVoraciousPan) tile) : null;
        });
        MinecraftForge.EVENT_BUS.register(new VoraciousPanEvents(() -> pan));
    }

    @Override
    public void registerBlocks(IForgeRegistry<Block> registry) {
        stove = new BlockEtheriumStove();
        pan = new BlockVoraciousPan();
        registry.register(stove);
        registry.register(pan);
    }

    @Override
    public void registerItems(IForgeRegistry<Item> registry) {
        stoveItem = DelicacyItem.blockItem(stove);
        registry.register(stoveItem);
    }

    /** 默认优先级建表，CraftTweaker（LOWEST）之后再增删 */
    @Override
    public void registerRecipes(IForgeRegistry<IRecipe> registry) {
        AbyssalCookingDefaults.register();
    }

    @Override
    public void registerModels() {
        MachineClient.registerModels(stoveItem);
    }

    @Override
    public void clientPreInit() {
        MachineClient.preInit();
    }
}
