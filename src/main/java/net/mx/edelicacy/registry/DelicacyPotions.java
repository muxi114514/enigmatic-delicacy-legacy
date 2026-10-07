package net.mx.edelicacy.registry;

import net.minecraft.potion.Potion;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * 全部药水效果的引用。
 * <p>由 Forge 在对应注册事件之后注入（字段名小写即注册名），注入前为 null，不能在静态初始化里使用。
 */
@GameRegistry.ObjectHolder("enigmaticdelicacy")
public final class DelicacyPotions {

    public static final Potion ASTRAL_DRUNKENNESS = null;
    public static final Potion FADING = null;
    public static final Potion FORBIDDEN_IMPRINT = null;
    public static final Potion HEALTH_CURSE = null;
    public static final Potion PERSEVERANCE = null;
    public static final Potion TENACITY = null;
    public static final Potion WITHERING_SMITE = null;
    public static final Potion DOLPHINS_GRACE = null;

    private DelicacyPotions() {
    }
}
