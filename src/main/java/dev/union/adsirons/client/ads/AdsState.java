package dev.union.adsirons.client.ads;

import dev.union.adsirons.ads.AimPayload;
import dev.union.adsirons.ads.AimTracker;
import io.redspace.irons_artifice.item.GunItem;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public final class AdsState {
    private static float progress;
    private static float prevProgress;
    private static boolean sentAiming;

    private AdsState() {
    }

    public static boolean supportsAds(ItemStack stack) {
        return stack.getItem() instanceof GunItem;
    }

    public static float get(float partialTick) {
        float t = Mth.lerp(partialTick, prevProgress, progress);
        return t * t * (3f - 2f * t);
    }

    public static boolean isAiming() {
        return progress > 0f;
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        prevProgress = progress;
        Minecraft mc = Minecraft.getInstance();
        boolean wants = wantsToAim(mc);
        float step = 1f / AdsConfig.TRANSITION_TICKS.get();
        progress = Mth.clamp(progress + (wants ? step : -step), 0f, 1f);
        syncAiming(mc, wants);
    }

    private static void syncAiming(Minecraft mc, boolean aiming) {
        ClientPacketListener connection = mc.getConnection();
        boolean serverHasMod = connection != null && connection.hasChannel(AimPayload.TYPE);
        // no tighter crosshair unless server applies bonus
        AimTracker.setLocalAiming(aiming && serverHasMod);
        if (aiming != sentAiming && serverHasMod) {
            PacketDistributor.sendToServer(new AimPayload(aiming));
            sentAiming = aiming;
        }
    }

    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        sentAiming = false;
        AimTracker.setLocalAiming(false);
        progress = prevProgress = 0f;
    }

    private static boolean wantsToAim(Minecraft mc) {
        LocalPlayer player = mc.player;
        if (!AdsConfig.ENABLED.get() || player == null || mc.screen != null || player.isSpectator()
                || !mc.options.getCameraType().isFirstPerson()) {
            return false;
        }
        ItemStack stack = player.getMainHandItem();
        // using the item (bayonet, scope) priority over aiming
        return supportsAds(stack)
                && !player.isUsingItem()
                && !GunItem.isReloading(stack)
                && AdsKeybinds.isAimHeld(mc);
    }
}
