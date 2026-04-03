package xyz.aerii.nebulune.mixin.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Camera;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.aerii.nebulune.modules.impl.render.CameraHelper;

@Mixin(Camera.class)
public class CameraMixin {
    @WrapOperation(method = "setup", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getAttributeValue(Lnet/minecraft/core/Holder;)D"))
    private double nebulune$setup(LivingEntity instance, Holder<Attribute> attribute, Operation<Double> original) {
        return CameraHelper.getDist() ? CameraHelper.getDistance() : instance.getAttributeValue(attribute);
    }

    @Inject(method = "getMaxZoom", at = @At("HEAD"), cancellable = true)
    private void nebulune$getMaxZoom(float maxZoom, CallbackInfoReturnable<Float> cir) {
        if (CameraHelper.getClip()) cir.setReturnValue(maxZoom);
    }
}