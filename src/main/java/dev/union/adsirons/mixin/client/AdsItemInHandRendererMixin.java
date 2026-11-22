package dev.union.adsirons.mixin.client;

import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.union.adsirons.client.ads.AdsPose;
import dev.union.adsirons.client.ads.AdsState;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;

@Mixin(ItemInHandRenderer.class)
public class AdsItemInHandRendererMixin {
    @Unique
    private AdsPose adsirons$pose;
    @Unique
    private float adsirons$t;

    @Inject(method = "renderArmWithItem", at = @At(value = "INVOKE",
            target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V", ordinal = 0, shift = At.Shift.AFTER))
    private void adsirons$adsTranslate(AbstractClientPlayer player, float partialTicks, float pitch, InteractionHand hand,
            float swingProgress, ItemStack stack, float equippedProgress, PoseStack poseStack, MultiBufferSource buffer,
            int combinedLight, CallbackInfo ci) {
        adsirons$pose = null;
        if (hand != InteractionHand.MAIN_HAND || !AdsState.isAiming() || !AdsState.supportsAds(stack)) {
            return;
        }
        adsirons$t = AdsState.get(partialTicks);
        if (adsirons$t <= 0f) {
            return;
        }
        HumanoidArm arm = player.getMainArm();
        adsirons$pose = AdsPose.compute(player, stack, arm);
        float t = adsirons$t;
        poseStack.translate(adsirons$pose.offset().x() * t, adsirons$pose.offset().y() * t, adsirons$pose.offset().z() * t);
    }

    @Inject(method = "renderArmWithItem", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;applyItemArmTransform(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/entity/HumanoidArm;F)V",
            shift = At.Shift.AFTER))
    private void adsirons$adsRotate(AbstractClientPlayer player, float partialTicks, float pitch, InteractionHand hand,
            float swingProgress, ItemStack stack, float equippedProgress, PoseStack poseStack, MultiBufferSource buffer,
            int combinedLight, CallbackInfo ci) {
        if (adsirons$pose != null && hand == InteractionHand.MAIN_HAND) {
            poseStack.mulPose(new Quaternionf().slerp(adsirons$pose.rotation(), adsirons$t));
        }
    }
}
