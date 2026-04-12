package xyz.aerii.nebulune.mixin.mixins.athen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.aerii.athen.modules.impl.kuudra.StunHelper;
import xyz.aerii.nebulune.modules.impl.kuudra.Stunner;

@Mixin(StunHelper.class)
public class StunHelperMixin {
    @Inject(method = "fn", at = @At("HEAD"))
    private void nebulune$fn(CallbackInfo ci) {
        Stunner.fn();
    }
}
