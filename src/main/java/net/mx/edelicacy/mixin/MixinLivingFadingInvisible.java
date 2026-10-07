package net.mx.edelicacy.mixin;

import net.minecraft.entity.EntityLivingBase;
import net.mx.edelicacy.fading.FadingInvisibility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 原版 updatePotionMetadata（func_175135_B）只按隐身药水重设隐身标记，会把失色给的隐身清掉；
 * 星醉每 4 tick 强制刷新一次药水元数据，不处理会闪烁（对应 1.21 MixinLivingEntity#updateInvisibilityStatusMix）。
 * <ul>
 *   <li>ModifyArg：直接把失色并进 setInvisible（func_82142_c）的参数，标记不会先变 false 再变 true，也就不会每次刷新都重发实体元数据</li>
 *   <li>RETURN：兜底，参数改写未命中时仍在末尾补上隐身</li>
 * </ul>
 */
@Mixin(value = EntityLivingBase.class, remap = false)
public abstract class MixinLivingFadingInvisible {

    @ModifyArg(method = "func_175135_B",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;func_82142_c(Z)V"),
            remap = false)
    private boolean edelicacy$keepFadingInvisible(boolean invisible) {
        return invisible || FadingInvisibility.hasFading((EntityLivingBase) (Object) this);
    }

    @Inject(method = "func_175135_B", at = @At("RETURN"), remap = false)
    private void edelicacy$applyFadingInvisibility(CallbackInfo ci) {
        FadingInvisibility.apply((EntityLivingBase) (Object) this);
    }
}
