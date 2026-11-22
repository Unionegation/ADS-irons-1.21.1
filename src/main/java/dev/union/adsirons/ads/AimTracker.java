package dev.union.adsirons.ads;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public final class AimTracker {
    private static final Set<UUID> SERVER_AIMING = ConcurrentHashMap.newKeySet();
    // client side, for the local player only
    private static volatile boolean localAiming;

    private AimTracker() {
    }

    public static boolean isAiming(Entity entity) {
        if (!(entity instanceof Player player)) {
            return false;
        }
        if (player.level().isClientSide()) {
            return player.isLocalPlayer() && localAiming;
        }
        return SERVER_AIMING.contains(player.getUUID());
    }

    public static void setLocalAiming(boolean aiming) {
        localAiming = aiming;
    }

    static void setServerAiming(Player player, boolean aiming) {
        if (aiming) {
            SERVER_AIMING.add(player.getUUID());
        } else {
            SERVER_AIMING.remove(player.getUUID());
        }
    }

    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        SERVER_AIMING.remove(event.getEntity().getUUID());
    }
}
