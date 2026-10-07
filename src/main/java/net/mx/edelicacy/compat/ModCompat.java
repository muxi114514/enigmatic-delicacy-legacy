package net.mx.edelicacy.compat;

import java.lang.reflect.Method;

import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.Loader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 软依赖开关（preInit 时确定）。调用对方 API 的代码必须隔离在单独的类里，并先判这里的开关。
 */
public final class ModCompat {

    private static final Logger LOG = LogManager.getLogger("enigmaticdelicacy");

    public static boolean jei;
    public static boolean craftTweaker;
    public static boolean simpleDifficulty;
    public static boolean nethersDelight;
    public static boolean waila;
    public static boolean trinkets;
    public static boolean baubleVault;
    public static boolean iceAndFire;
    public static boolean harvestCraft;
    public static boolean futureMc;
    public static boolean netherized;

    private static Method refillMana;
    private static boolean manaLookupDone;

    private ModCompat() {
    }

    public static void init() {
        jei = Loader.isModLoaded("jei");
        craftTweaker = Loader.isModLoaded("crafttweaker");
        simpleDifficulty = Loader.isModLoaded("simpledifficulty");
        nethersDelight = Loader.isModLoaded("nethers_delight_legacy");
        waila = Loader.isModLoaded("waila");
        trinkets = Loader.isModLoaded("xat");
        baubleVault = Loader.isModLoaded("unlockablebauble");
        iceAndFire = Loader.isModLoaded("iceandfire");
        harvestCraft = Loader.isModLoaded("harvestcraft");
        futureMc = Loader.isModLoaded("futuremc");
        netherized = Loader.isModLoaded("netherized");
    }

    /** Trinkets and Baubles 的魔力回复（替代原版的 Iron's Spells 瞬间回魔）；没装就什么都不做 */
    public static void refillMana(Entity entity, float amount) {
        if (!trinkets) {
            return;
        }
        if (!manaLookupDone) {
            manaLookupDone = true;
            try {
                refillMana = Class.forName("xzeroair.trinkets.util.helpers.MagicHelper")
                        .getMethod("refillMana", Entity.class, float.class);
            } catch (ReflectiveOperationException e) {
                LOG.warn("Trinkets and Baubles mana API not found, mana refill disabled", e);
            }
        }
        if (refillMana != null) {
            try {
                refillMana.invoke(null, entity, amount);
            } catch (ReflectiveOperationException e) {
                LOG.warn("Trinkets and Baubles mana refill failed", e);
                refillMana = null;
            }
        }
    }
}
