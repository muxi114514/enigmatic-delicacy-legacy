package net.mx.edelicacy.mixin;

import java.util.Random;

import com.wdcftgg.farmersdelightlegacy.common.block.BlockMushroomColony;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.mx.edelicacy.flora.soil.ColonyHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 农夫乐事菌落只认自家沃土/菌丝/灰化土，这里让它也认无尽沃土（存活不看亮度 + 随机刻生长）。
 * <p>下界乐事的菌类菌落继承本类：其 canBlockStay 落到父类 isMushroomGrowBlock，updateTick 先调 super，
 * 因此同一个 mixin 就覆盖两者。updateTick 在发布版 jar 中是 SRG 名 func_180650_b，开发环境是 MCP 名。
 */
@Mixin(value = BlockMushroomColony.class, remap = false)
public abstract class MixinFdMushroomColony {

    @Inject(method = "isMushroomGrowBlock", at = @At("HEAD"), cancellable = true, remap = false)
    private void edelicacy$acceptInfinisoil(IBlockState groundState, CallbackInfoReturnable<Boolean> cir) {
        if (ColonyHooks.isGrowBlock(groundState)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = {"func_180650_b", "updateTick"}, at = @At("TAIL"), remap = false)
    private void edelicacy$growOnInfinisoil(World world, BlockPos pos, IBlockState state, Random rand, CallbackInfo ci) {
        ColonyHooks.onColonyTick((Block) (Object) this, world, pos, rand);
    }
}
