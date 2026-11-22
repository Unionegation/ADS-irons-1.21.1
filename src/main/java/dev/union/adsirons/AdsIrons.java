package dev.union.adsirons;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import dev.union.adsirons.ads.AimPayload;
import dev.union.adsirons.ads.AimTracker;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(AdsIrons.MODID)
public class AdsIrons {
    public static final String MODID = "ads_irons";
    public static final Logger LOGGER = LogUtils.getLogger();

    public AdsIrons(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(AimPayload::register);
        NeoForge.EVENT_BUS.addListener(AimTracker::onLogout);
    }
}
