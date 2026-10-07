package net.mx.edelicacy.module;

import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.potion.Potion;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.registries.IForgeRegistry;

/**
 * 功能模块：主类按 {@link DelicacyModules#ALL} 的顺序依次回调。
 * <p>客户端回调（registerModels / clientPreInit / clientInit）只允许调用本模块 XxxClient 类的静态方法，
 * 不得在模块类里直接出现客户端类型，否则专用服务端加载模块类时会找不到类。
 */
public interface IDelicacyModule {

    /** 读配置、注册事件处理器、方块实体、网络包、GUI */
    default void preInit(Configuration config) {
    }

    /** 物品已注册：矿辞、诅咒表、兼容 */
    default void init() {
    }

    default void postInit() {
    }

    /** 命令等 */
    default void serverStarting(FMLServerStartingEvent event) {
    }

    default void registerBlocks(IForgeRegistry<Block> registry) {
    }

    default void registerItems(IForgeRegistry<Item> registry) {
    }

    default void registerPotions(IForgeRegistry<Potion> registry) {
    }

    /** loadComplete 时调用（取最后的数字 ID，见主类），附魔的 @ObjectHolder 此后才注入 */
    default void registerEnchantments(IForgeRegistry<Enchantment> registry) {
    }

    default void registerEntities(IForgeRegistry<EntityEntry> registry) {
    }

    /** 自定义 IRecipe（如需佩戴七咒才能合成的配方）；普通合成配方写 JSON */
    default void registerRecipes(IForgeRegistry<IRecipe> registry) {
    }

    /** 客户端 ModelRegistryEvent 时调用 */
    default void registerModels() {
    }

    /** 客户端 preInit：实体渲染器、方块实体渲染器、客户端事件、GUI 工厂 */
    default void clientPreInit() {
    }

    default void clientInit() {
    }
}
