package net.mx.edelicacy.effect;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.registries.IForgeRegistry;
import net.mx.edelicacy.effect.handler.AstralDrunkennessHandler;
import net.mx.edelicacy.effect.handler.PerseveranceHandler;
import net.mx.edelicacy.effect.handler.TenacityHandler;
import net.mx.edelicacy.effect.handler.WitheringSmiteHandler;
import net.mx.edelicacy.module.IDelicacyModule;

/**
 * 药水模块：星辉酩酊、坚毅、坚韧、亡灵凋谢，以及替代 1.13 原版的海豚的恩惠。
 * 失色、禁忌之印、生命诅咒由其他模块注册。
 */
public class EffectModule implements IDelicacyModule {

    @Override
    public void preInit(Configuration config) {
        MinecraftForge.EVENT_BUS.register(new AstralDrunkennessHandler());
        MinecraftForge.EVENT_BUS.register(new PerseveranceHandler());
        MinecraftForge.EVENT_BUS.register(new TenacityHandler());
        MinecraftForge.EVENT_BUS.register(new WitheringSmiteHandler());
    }

    @Override
    public void registerPotions(IForgeRegistry<Potion> registry) {
        registry.register(new PotionAstralDrunkenness());
        registry.register(new DelicacyPotion("perseverance", false, 0xC53439).beneficial());
        registry.register(new DelicacyPotion("tenacity", false, 0x1AB7BB).beneficial());
        registry.register(new DelicacyPotion("withering_smite", false, 0x431605).beneficial());
        // 1.13 海豚的恩惠的替代：水中移动加速度每级 ×2（乘总值）
        registry.register(new DelicacyPotion("dolphins_grace", false, 0x88A3BE).beneficial()
                .registerPotionAttributeModifier(EntityLivingBase.SWIM_SPEED, "5b6f0a3e-2d4c-4b8e-9f71-3c2a8d9e6b14", 1.0D, 2));
    }
}
