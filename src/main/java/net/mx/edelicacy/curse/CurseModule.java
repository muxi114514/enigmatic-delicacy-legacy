package net.mx.edelicacy.curse;

import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.registries.IForgeRegistry;
import net.mx.eaddons.attribute.AttributeSources;
import net.mx.edelicacy.EnigmaticDelicacy;
import net.mx.edelicacy.curse.client.CurseClient;
import net.mx.edelicacy.curse.enchant.EnchantmentSoulDevouring;
import net.mx.edelicacy.curse.enchant.SoulDevouringHandler;
import net.mx.edelicacy.curse.item.abyss.AbyssalStewHandler;
import net.mx.edelicacy.curse.item.blade.CurseBladeHandler;
import net.mx.edelicacy.curse.item.blade.CursedRingCutter;
import net.mx.edelicacy.curse.item.divine.DivineFruitPieHandler;
import net.mx.edelicacy.curse.item.divine.EntityThrownDivineFruitPie;
import net.mx.edelicacy.curse.item.divine.ItemDivineFruitPie;
import net.mx.edelicacy.curse.item.forbidden.ForbiddenCharmHandler;
import net.mx.edelicacy.curse.item.forbidden.ForbiddenKnifeHandler;
import net.mx.edelicacy.curse.item.gluttony.GluttonyCharmHandler;
import net.mx.edelicacy.curse.item.gluttony.GluttonyCharmRecipe;
import net.mx.edelicacy.curse.item.grail.ForbiddenGrailHandler;
import net.mx.edelicacy.curse.item.soul.CrystalKnifeResetRecipe;
import net.mx.edelicacy.curse.item.soul.CurseAnvilHandler;
import net.mx.edelicacy.curse.item.soul.CurseCrystalKnifeHandler;
import net.mx.edelicacy.curse.item.soul.CurseCrystalPendantHandler;
import net.mx.edelicacy.curse.item.soul.SoulAbsorbHandler;
import net.mx.edelicacy.curse.potion.AbyssCorruptionHandler;
import net.mx.edelicacy.curse.potion.CursePotions;
import net.mx.edelicacy.curse.potion.ForbiddenImprintHandler;
import net.mx.edelicacy.curse.potion.HealthCurseHandler;
import net.mx.edelicacy.curse.potion.PotionAbyssCorruption;
import net.mx.edelicacy.curse.potion.PotionForbiddenImprint;
import net.mx.edelicacy.curse.potion.PotionHealthCurse;
import net.mx.edelicacy.curse.potion.StackedEffects;
import net.mx.edelicacy.module.IDelicacyModule;
import net.mx.edelicacy.util.EnigmaticBridge;

/**
 * 诅咒 / 禁忌 / 深渊线：诅咒刃片与斩断七咒之戒、咒魂晶系列、禁忌食物与器物、调谐圣杯、无尽贪食徽记、深渊乱炖、神圣果派，
 * 以及生命诅咒 / 禁忌之印 / 深渊腐蚀三个效果和噬魂诅咒附魔。
 */
public class CurseModule implements IDelicacyModule {

    @Override
    public void preInit(Configuration config) {
        CurseConfig.load(config);
        Object[] handlers = {
                new HealthCurseHandler(), new ForbiddenImprintHandler(), new AbyssCorruptionHandler(), new StackedEffects(),
                new SoulDevouringHandler(), new CurseBladeHandler(), new CursedRingCutter(), new SoulAbsorbHandler(),
                new CurseCrystalKnifeHandler(), new CurseCrystalPendantHandler(), new CurseAnvilHandler(),
                new CrystalKnifeResetRecipe.XpHandler(), new ForbiddenKnifeHandler(), new ForbiddenCharmHandler(),
                new ForbiddenGrailHandler(), new GluttonyCharmHandler(), new AbyssalStewHandler(), new DivineFruitPieHandler()
        };
        for (Object handler : handlers) {
            MinecraftForge.EVENT_BUS.register(handler);
        }
        ForbiddenGrailHandler.registerAttributeSources();
        AttributeSources.register("enigmaticdelicacy.divine_fruit_pie_health", SharedMonsterAttributes.MAX_HEALTH, 0, 20,
                ItemDivineFruitPie::fallbackHealth);
    }

    /** 诅咒 / 深渊 / 祝福物品登记到 EL 的配置表（EL 负责使用限制与提示） */
    @Override
    public void init() {
        EnigmaticBridge.addCursedItem(id("gluttony_charm"));
        EnigmaticBridge.addEldritchItem(id("abyssal_stew"));
        for (String name : new String[]{"divine_fruit_pie", "divine_fruit_pie_block"}) {
            EnigmaticBridge.addCursedItem(id(name));
            EnigmaticBridge.addBlessedItem(id(name));
        }
    }

    private static ResourceLocation id(String name) {
        return new ResourceLocation(EnigmaticDelicacy.MODID, name);
    }

    @Override
    public void registerBlocks(IForgeRegistry<Block> registry) {
        CurseContent.registerBlocks(registry);
    }

    @Override
    public void registerItems(IForgeRegistry<Item> registry) {
        CurseContent.registerItems(registry);
    }

    @Override
    public void registerPotions(IForgeRegistry<Potion> registry) {
        CursePotions.HEALTH_CURSE = new PotionHealthCurse();
        CursePotions.FORBIDDEN_IMPRINT = new PotionForbiddenImprint();
        CursePotions.ABYSS_CORRUPTION = new PotionAbyssCorruption();
        registry.registerAll(CursePotions.HEALTH_CURSE, CursePotions.FORBIDDEN_IMPRINT, CursePotions.ABYSS_CORRUPTION);
    }

    @Override
    public void registerEnchantments(IForgeRegistry<Enchantment> registry) {
        registry.register(new EnchantmentSoulDevouring());
    }

    @Override
    public void registerEntities(IForgeRegistry<EntityEntry> registry) {
        registry.register(EntityEntryBuilder.create()
                .entity(EntityThrownDivineFruitPie.class)
                .id(id("divine_fruit_pie"), 4)
                .name(EnigmaticDelicacy.MODID + ".divine_fruit_pie")
                .tracker(64, 10, true)
                .build());
    }

    @Override
    public void registerRecipes(IForgeRegistry<IRecipe> registry) {
        registry.register(new CrystalKnifeResetRecipe());
        Item gluttony = CurseContent.items().stream()
                .filter(item -> id("gluttony_charm").equals(item.getRegistryName())).findFirst().orElse(null);
        IRecipe recipe = gluttony == null ? null : GluttonyCharmRecipe.create(gluttony);
        if (recipe != null) {
            registry.register(recipe);
        }
    }

    @Override
    public void registerModels() {
        CurseClient.registerModels(CurseContent.items());
    }

    @Override
    public void clientPreInit() {
        CurseClient.preInit();
    }
}
