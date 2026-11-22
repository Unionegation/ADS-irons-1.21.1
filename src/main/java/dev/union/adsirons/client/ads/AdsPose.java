package dev.union.adsirons.client.ads;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.cache.GeckoLibCache;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;

// Aimed pose: undo the item's first person rotation, then move the muzzle to screen center
// a fixed angle under the crosshair. offset goes before the vanilla hand offset, rotation after.
public record AdsPose(Vector3f offset, Quaternionf rotation) {
    // from ItemInHandRenderer.applyItemArmTransform (right hand)
    private static final float ARM_X = 0.56f;
    private static final float ARM_Y = -0.52f;
    private static final float ARM_Z = -0.72f;
    // geckolib nudges items up by 0.01 in preRender
    private static final float GECKO_Y = 0.01f;

    public static AdsPose compute(AbstractClientPlayer player, ItemStack stack, HumanoidArm arm) {
        // resolve separate_transforms etc to the first person model
        ItemTransform display = Minecraft.getInstance().getItemRenderer()
                .getModel(stack, player.level(), player, 0)
                .applyTransform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, new PoseStack(), false)
                .getTransforms().firstPersonRightHand;

        Quaternionf displayRot = new Quaternionf().rotationXYZ(
                (float) Math.toRadians(display.rotation.x()),
                (float) Math.toRadians(display.rotation.y()),
                (float) Math.toRadians(display.rotation.z()));
        Quaternionf extra = new Quaternionf()
                .rotateY((float) Math.toRadians(AdsConfig.YAW.get()))
                .rotateX((float) Math.toRadians(AdsConfig.PITCH.get()))
                .rotateZ((float) Math.toRadians(AdsConfig.ROLL.get()));
        Quaternionf rotation = new Quaternionf(extra).mul(displayRot.conjugate(new Quaternionf()));

        Vector3f displayOffset = rotation.transform(new Vector3f(display.translation));
        Vector3f muzzle = extra.transform(muzzlePosition(stack));

        float forward = AdsConfig.FORWARD_OFFSET.get().floatValue();
        float muzzleZ = ARM_Z + forward + displayOffset.z() + muzzle.z();
        float targetY = -(float) AdsConfig.sightDrop(BuiltInRegistries.ITEM.getKey(stack.getItem())) * Math.abs(muzzleZ);

        Vector3f offset = new Vector3f(
                AdsConfig.HORIZONTAL_OFFSET.get().floatValue() - ARM_X - displayOffset.x() - muzzle.x(),
                targetY - ARM_Y - displayOffset.y() - muzzle.y(),
                forward);

        if (arm == HumanoidArm.LEFT) {
            // mirror for left handed
            offset.x = -offset.x;
            rotation.y = -rotation.y;
            rotation.z = -rotation.z;
        }
        return new AdsPose(offset, rotation);
    }

    private static Vector3f muzzlePosition(ItemStack stack) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        BakedGeoModel model = GeckoLibCache.getBakedModels().get(id.withPath("geo/item/" + id.getPath() + ".geo.json"));
        GeoBone muzzle = model == null ? null : model.getBone("attachment_muzzle").orElse(null);
        if (muzzle == null) {
            return new Vector3f(0f, GECKO_Y, 0f);
        }
        return new Vector3f(muzzle.getPivotX() / 16f, muzzle.getPivotY() / 16f + GECKO_Y, muzzle.getPivotZ() / 16f);
    }
}
