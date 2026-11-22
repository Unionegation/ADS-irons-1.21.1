package dev.union.adsirons.client.ads;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;

public final class AdsEvents {
    private AdsEvents() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(AdsKeybinds::register);
        NeoForge.EVENT_BUS.addListener(AdsState::onClientTick);
        NeoForge.EVENT_BUS.addListener(AdsState::onLoggingOut);
        NeoForge.EVENT_BUS.addListener(AdsEvents::onComputeFov);
        NeoForge.EVENT_BUS.addListener(AdsEvents::onRenderGuiLayer);
    }

    private static void onComputeFov(ComputeFovModifierEvent event) {
        if (AdsState.isAiming()) {
            // vanilla lerps the fov modifier already
            float t = AdsState.get(1f);
            event.setNewFovModifier(event.getNewFovModifier() * Mth.lerp(t, 1f, AdsConfig.FOV_MULTIPLIER.get().floatValue()));
        }
    }

    private static void onRenderGuiLayer(RenderGuiLayerEvent.Pre event) {
        if (event.getName().equals(VanillaGuiLayers.CROSSHAIR) && AdsConfig.HIDE_CROSSHAIR.get()
                && AdsState.get(Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true)) > 0.5f) {
            event.setCanceled(true);
        }
    }
}
