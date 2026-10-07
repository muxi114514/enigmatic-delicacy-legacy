package net.mx.edelicacy.charm;

import java.util.ArrayList;
import java.util.List;

import baubles.api.BaubleType;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.mx.eaddons.item.ItemAntiqueBag;
import net.mx.edelicacy.EnigmaticDelicacy;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.registries.IForgeRegistry;
import net.mx.edelicacy.charm.client.CharmClient;
import net.mx.edelicacy.charm.event.GlisteningBookEvents;
import net.mx.edelicacy.charm.event.GlisteningCharmEvents;
import net.mx.edelicacy.charm.event.HungryCharmEvents;
import net.mx.edelicacy.charm.event.SleepEvents;
import net.mx.edelicacy.charm.event.WeaponCharmEvents;
import net.mx.edelicacy.charm.item.AstralTeaItem;
import net.mx.edelicacy.charm.item.EnchantmentDuplicatorItem;
import net.mx.edelicacy.charm.item.GlisteningCharmItem;
import net.mx.edelicacy.charm.item.GlisteningTeaItem;
import net.mx.edelicacy.charm.item.HungryCharmItem;
import net.mx.edelicacy.charm.item.LuckBaubleItem;
import net.mx.edelicacy.charm.item.TheGlisteningItem;
import net.mx.edelicacy.charm.item.WeaponCharmItem;
import net.mx.edelicacy.charm.recipe.EnchantmentDuplicationRecipe;
import net.mx.edelicacy.item.base.DelicacyItem;
import net.mx.edelicacy.module.IDelicacyModule;

/** 护符模块：星花护符、安宁之戒、饥饿 / 武器 / 守护护符、两种茶、闪耀之书、辉光之心、全知之书 */
public class CharmModule implements IDelicacyModule {

    private final List<Item> items = new ArrayList<>();

    @Override
    public void preInit(Configuration config) {
        CharmConfig.load(config);
        MinecraftForge.EVENT_BUS.register(new SleepEvents());
        MinecraftForge.EVENT_BUS.register(new HungryCharmEvents());
        MinecraftForge.EVENT_BUS.register(new WeaponCharmEvents());
        MinecraftForge.EVENT_BUS.register(new GlisteningCharmEvents());
        MinecraftForge.EVENT_BUS.register(new GlisteningBookEvents());
    }

    @Override
    public void init() {
        // 与 1.21 一致：闪耀之书放在古旧书袋里也生效
        ItemAntiqueBag.registerAllowedItem(new ResourceLocation(EnigmaticDelicacy.MODID, "the_glistening"));
    }

    @Override
    public void registerItems(IForgeRegistry<Item> registry) {
        add(registry, new LuckBaubleItem("astral_charm", BaubleType.CHARM, 1.0D, "tooltip.enigmaticdelicacy.astralCharm1"));
        add(registry, new LuckBaubleItem("petal_ring", BaubleType.RING, 1.5D, "tooltip.enigmaticdelicacy.petalRing"));
        add(registry, new HungryCharmItem());
        add(registry, new WeaponCharmItem());
        add(registry, new GlisteningCharmItem());
        add(registry, new AstralTeaItem());
        add(registry, new GlisteningTeaItem());
        add(registry, new TheGlisteningItem());
        // 1.21 模型里的 off 谓词从未注册，模型已去掉该覆盖
        add(registry, new DelicacyItem("glistening_heart").rarity(EnumRarity.UNCOMMON).stackSize(1));
        add(registry, new EnchantmentDuplicatorItem());
    }

    private void add(IForgeRegistry<Item> registry, Item item) {
        registry.register(item);
        items.add(item);
    }

    @Override
    public void registerRecipes(IForgeRegistry<IRecipe> registry) {
        registry.register(new EnchantmentDuplicationRecipe());
    }

    @Override
    public void registerModels() {
        CharmClient.registerModels(items);
    }
}
