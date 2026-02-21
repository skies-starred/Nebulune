package xyz.aerii.nebulune.mixin.mixins;

import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.aerii.athen.events.core.CancellableEvent;
import xyz.aerii.athen.handlers.Smoothie;
import xyz.aerii.athen.modules.impl.general.WardrobeKeybinds;
import xyz.aerii.nebulune.modules.WardrobeHelper;

@Mixin(WardrobeKeybinds.class)
public class WardrobeKeybindsMixin {
    @Inject(method = "fn", at = @At("TAIL"))
    private void nebulune$fn(CancellableEvent $this$fn, int key, CallbackInfo ci) {
        LocalPlayer player = Smoothie.getClient().player;
        if (player != null && WardrobeHelper.INSTANCE.getAutoClose()) player.closeContainer();
    }
}