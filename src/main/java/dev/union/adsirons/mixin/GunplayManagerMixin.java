package dev.union.adsirons.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import dev.union.adsirons.ads.AimTracker;
import io.redspace.irons_artifice.gun.ShotProfile;
import io.redspace.irons_artifice.item.GunplayManager;
import net.minecraft.world.entity.Entity;

@Mixin(value = GunplayManager.class, remap = false)
public class GunplayManagerMixin {
    // crouch is 0.667, aiming takes 1.5x that reduction. doesn't stack with crouching
    @Unique
    private static final float ADSIRONS$AIM_SPREAD_MULTIPLIER = 0.5f;

    @WrapOperation(method = "getSpreadForEntity", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;isCrouching()Z"))
    private static boolean adsirons$aimingCountsAsCrouching(Entity entity, Operation<Boolean> original) {
        return original.call(entity) || AimTracker.isAiming(entity);
    }

    @ModifyExpressionValue(method = "getSpreadForEntity", at = @At(value = "CONSTANT", args = "floatValue=0.667"))
    private static float adsirons$aimSpreadMultiplier(float crouchMultiplier, ShotProfile profile, Entity entity) {
        return AimTracker.isAiming(entity) ? ADSIRONS$AIM_SPREAD_MULTIPLIER : crouchMultiplier;
    }
}
