package net.mx.edelicacy.registry;

import net.minecraft.enchantment.Enchantment;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * 附魔的引用。
 * <p>附魔在 loadComplete 才注册（见主类），此前为 null（字段名小写即注册名），不能在静态初始化里使用。
 */
@GameRegistry.ObjectHolder("enigmaticdelicacy")
public final class DelicacyEnchantments {

    public static final Enchantment SOUL_DEVOURING_CURSE = null;

    private DelicacyEnchantments() {
    }
}
