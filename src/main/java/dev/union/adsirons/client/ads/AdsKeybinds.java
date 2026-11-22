package dev.union.adsirons.client.ads;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyConflictContext;

public final class AdsKeybinds {
    // shares right click with use item by default, so never report it as a conflict
    private static final IKeyConflictContext SHARED_IN_GAME = new IKeyConflictContext() {
        @Override
        public boolean isActive() {
            return KeyConflictContext.IN_GAME.isActive();
        }

        @Override
        public boolean conflicts(IKeyConflictContext other) {
            return false;
        }
    };

    public static final KeyMapping AIM_DOWN_SIGHTS = new KeyMapping("key.ads_irons.aim_down_sights", SHARED_IN_GAME,
            InputConstants.Type.MOUSE, GLFW.GLFW_MOUSE_BUTTON_RIGHT, "key.categories.ads_irons");

    private AdsKeybinds() {
    }

    public static void register(RegisterKeyMappingsEvent event) {
        event.register(AIM_DOWN_SIGHTS);
    }

    // isDown() covers controller mods (Controlify etc), the raw glfw check covers the shared right click
    public static boolean isAimHeld(Minecraft mc) {
        if (AIM_DOWN_SIGHTS.isDown()) {
            return true;
        }
        InputConstants.Key key = AIM_DOWN_SIGHTS.getKey();
        if (key.equals(InputConstants.UNKNOWN) || !AIM_DOWN_SIGHTS.getKeyModifier().isActive(SHARED_IN_GAME)) {
            return false;
        }
        long window = mc.getWindow().getWindow();
        return switch (key.getType()) {
            case MOUSE -> GLFW.glfwGetMouseButton(window, key.getValue()) == GLFW.GLFW_PRESS;
            case KEYSYM -> InputConstants.isKeyDown(window, key.getValue());
            case SCANCODE -> false;
        };
    }
}
