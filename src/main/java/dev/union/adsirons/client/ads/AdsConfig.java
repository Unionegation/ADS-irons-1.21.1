package dev.union.adsirons.client.ads;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class AdsConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue ENABLED;
    public static final ModConfigSpec.DoubleValue SIGHT_DROP;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> SIGHT_DROP_OVERRIDES;
    public static final ModConfigSpec.DoubleValue HORIZONTAL_OFFSET;
    public static final ModConfigSpec.DoubleValue FORWARD_OFFSET;
    public static final ModConfigSpec.DoubleValue PITCH;
    public static final ModConfigSpec.DoubleValue YAW;
    public static final ModConfigSpec.DoubleValue ROLL;
    public static final ModConfigSpec.DoubleValue FOV_MULTIPLIER;
    public static final ModConfigSpec.IntValue TRANSITION_TICKS;
    public static final ModConfigSpec.BooleanValue HIDE_CROSSHAIR;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        b.comment("Aim-down-sights pose. Every gun is centered on its muzzle automatically; these adjust all guns.",
                "Edit while in game to tune; changes apply on save.").push("ads");
        ENABLED = b.comment("Allow aiming down sights with guns.")
                .define("enabled", true);
        SIGHT_DROP = b.comment("How far the muzzle sits below the crosshair, as a slope (drop per block of distance).",
                "Larger lowers the gun.")
                .defineInRange("sightDrop", 0.154, -1.0, 1.0);
        SIGHT_DROP_OVERRIDES = b.comment("Per-gun sightDrop, as \"item_id=value\". Smaller raises that gun.")
                .defineListAllowEmpty("sightDropOverrides", List.of("irons_artifice:musket=0.12"), () -> "irons_artifice:flintlock=0.154",
                        AdsConfig::isValidOverride);
        HORIZONTAL_OFFSET = b.comment("Sideways shift from centered, in blocks. Positive moves right.")
                .defineInRange("horizontalOffset", 0.0, -2.0, 2.0);
        FORWARD_OFFSET = b.comment("Shift in blocks. Positive moves the gun toward the camera.")
                .defineInRange("forwardOffset", 0.0, -2.0, 2.0);
        PITCH = b.comment("Extra rotation in degrees (X axis). 0 points the barrel straight ahead.")
                .defineInRange("pitch", 0.0, -180.0, 180.0);
        YAW = b.comment("Extra rotation in degrees (Y axis).")
                .defineInRange("yaw", 0.0, -180.0, 180.0);
        ROLL = b.comment("Extra rotation in degrees (Z axis).")
                .defineInRange("roll", 0.0, -180.0, 180.0);
        FOV_MULTIPLIER = b.comment("Field of view multiplier while fully aimed.")
                .defineInRange("fovMultiplier", 0.85, 0.1, 1.0);
        TRANSITION_TICKS = b.comment("Ticks to go from hip to fully aimed.")
                .defineInRange("transitionTicks", 4, 1, 40);
        HIDE_CROSSHAIR = b.comment("Hide the crosshair while aiming.")
                .define("hideCrosshair", false);
        b.pop();
        SPEC = b.build();
    }

    private AdsConfig() {
    }

    public static void save() {
        SPEC.save();
    }

    private static List<? extends String> parsedFrom;
    private static Map<ResourceLocation, Double> parsed = Map.of();

    public static double sightDrop(ResourceLocation itemId) {
        List<? extends String> raw = SIGHT_DROP_OVERRIDES.get();
        if (raw != parsedFrom) {
            Map<ResourceLocation, Double> map = new HashMap<>();
            for (String entry : raw) {
                String[] parts = entry.split("=", 2);
                map.put(ResourceLocation.parse(parts[0].trim()), Double.parseDouble(parts[1].trim()));
            }
            parsed = map;
            parsedFrom = raw;
        }
        return parsed.getOrDefault(itemId, SIGHT_DROP.get());
    }

    private static boolean isValidOverride(Object o) {
        if (!(o instanceof String s)) {
            return false;
        }
        String[] parts = s.split("=", 2);
        if (parts.length != 2 || ResourceLocation.tryParse(parts[0].trim()) == null) {
            return false;
        }
        try {
            Double.parseDouble(parts[1].trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
