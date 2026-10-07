package net.mx.edelicacy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import keletu.enigmaticlegacy.event.EnigmaticEvents;
import net.minecraft.entity.player.EntityPlayer;
import net.mx.edelicacy.curse.item.gluttony.GluttonyLogic;

/**
 * 染血勇气徽记 × 无尽贪食徽记（1.21 MixinBerserkEmblem）：同时佩戴时，EL 的「缺失生命比例」改为 池×0.8 + 缺失饥饿×0.4，
 * 影响 EL 里所有读它的地方（攻击加成、减伤、徽记属性）。
 */
@Mixin(value = EnigmaticEvents.class, remap = false)
public abstract class MixinElBerserkPool {

    @Inject(method = "getMissingHealthPool", at = @At("RETURN"), cancellable = true, remap = false)
    private static void enigmaticdelicacy$mixGluttony(EntityPlayer player, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(GluttonyLogic.mixBerserkPool(player, cir.getReturnValueF()));
    }
}
