package dev.union.adsirons.ads;

import dev.union.adsirons.AdsIrons;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record AimPayload(boolean aiming) implements CustomPacketPayload {
    public static final Type<AimPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AdsIrons.MODID, "aim"));
    public static final StreamCodec<ByteBuf, AimPayload> STREAM_CODEC = ByteBufCodecs.BOOL.map(AimPayload::new, AimPayload::aiming);

    @Override
    public Type<AimPayload> type() {
        return TYPE;
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        // optional so vanilla/unmodded servers still let you join
        event.registrar("1").optional().playToServer(TYPE, STREAM_CODEC, AimPayload::handle);
    }

    private static void handle(AimPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> AimTracker.setServerAiming(context.player(), payload.aiming()));
    }
}
