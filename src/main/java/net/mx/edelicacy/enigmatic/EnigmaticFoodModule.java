package net.mx.edelicacy.enigmatic;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.mx.edelicacy.enigmatic.api.EnigmaticColor;
import net.mx.edelicacy.enigmatic.client.EnigmaticFoodClient;
import net.mx.edelicacy.enigmatic.item.EnigmaticFoodItem;
import net.mx.edelicacy.enigmatic.item.MagicPopsicleItem;
import net.mx.edelicacy.module.IDelicacyModule;
import net.mx.edelicacy.registry.DelicacyPotions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** 谜之食物模块：七种彩色料理、永久加成、清除命令（数值同 1.21 EnigmaticFoods） */
public class EnigmaticFoodModule implements IDelicacyModule {

    private static final Logger LOG = LogManager.getLogger("enigmaticdelicacy");
    private static final ResourceLocation ENDER_ROD = new ResourceLocation("enigmaticlegacy", "ender_rod");

    private final List<Item> items = new ArrayList<>();
    private MagicPopsicleItem popsicle;

    @Override
    public void preInit(Configuration config) {
        EnigmaticFoodBonuses.register();
    }

    @Override
    public void init() {
        Item rod = ForgeRegistries.ITEMS.getValue(ENDER_ROD);
        if (rod != null && popsicle != null) {
            popsicle.container(rod);
        } else {
            LOG.warn("enigmaticlegacy:ender_rod not found, magic popsicle returns no container");
        }
    }

    @Override
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new EnigmaticFoodCommand());
    }

    @Override
    public void registerItems(IForgeRegistry<Item> registry) {
        // 热情炒饭：1.21 装了 My Nether's Delight 时的辛辣效果已去掉（整合包另配 SimpleDifficulty 暖身）
        add(registry, new EnigmaticFoodItem("passion_fried_rice", 10, 0.8F, EnigmaticColor.RED)
                .effect(() -> DelicacyPotions.PERSEVERANCE, 1600, 1)
                .container(Items.BOWL).stackSize(16));
        add(registry, new EnigmaticFoodItem("sparkling_pastry", 4, 0.7F, EnigmaticColor.AQUA)
                .fast()
                .effect(() -> MobEffects.SPEED, 1200, 0)
                .effect(() -> MobEffects.FIRE_RESISTANCE, 1200, 0));
        add(registry, new EnigmaticFoodItem("melted_amethyst_cheese", 5, 0.8F, EnigmaticColor.VIOLET)
                .effect(() -> MobEffects.HASTE, 1200, 0)
                .effect(() -> MobEffects.RESISTANCE, 1200, 0)
                .container(Items.BUCKET).stackSize(1));
        add(registry, new EnigmaticFoodItem("celestial_custard", 3, 0.8F, EnigmaticColor.MAGENTA)
                .effect(() -> DelicacyPotions.ASTRAL_DRUNKENNESS, 2400, 0)
                .effect(() -> MobEffects.REGENERATION, 2400, 0)
                .container(Items.GLASS_BOTTLE).stackSize(16));
        add(registry, new EnigmaticFoodItem("sticky_pie_slice", 6, 0.6F, EnigmaticColor.GREEN)
                .fast()
                .effect(() -> MobEffects.NIGHT_VISION, 1200, 0)
                .effect(() -> MobEffects.LUCK, 1200, 0));
        add(registry, new EnigmaticFoodItem("withering_candy", 3, 0.4F, EnigmaticColor.BLACK)
                .fast()
                // 1.21 还给凋零 I 20 秒，但同时获得的亡灵凋谢在施加前就拦下凋零，删掉免得提示与实际不符
                .effect(() -> DelicacyPotions.WITHERING_SMITE, 1200, 1)
                .stackSize(32));
        popsicle = new MagicPopsicleItem();
        add(registry, popsicle);
    }

    private void add(IForgeRegistry<Item> registry, Item item) {
        registry.register(item);
        items.add(item);
    }

    @Override
    public void registerModels() {
        EnigmaticFoodClient.registerModels(items);
    }
}
