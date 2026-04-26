package com.theuntextured.metascreenshots.network;

import com.theuntextured.metascreenshots.MetaScreenshots;
import com.theuntextured.metascreenshots.containers.ScreenshotContainer;
import com.theuntextured.metascreenshots.util.WorldIdData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SyncWorldIdPacket implements CustomPacketPayload {

    public static final Type<SyncWorldIdPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MetaScreenshots.MOD_ID, "sync_world_id"));

    public static final StreamCodec<FriendlyByteBuf, SyncWorldIdPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> packet.toBytes(buf),
            SyncWorldIdPacket::new
    );

    private final String worldId;

    public SyncWorldIdPacket(String worldId) {
        this.worldId = worldId;
    }

    // Decoding from the raw byte stream
    public SyncWorldIdPacket(FriendlyByteBuf buf) {
        this.worldId = buf.readUtf();
    }

    // Encoding into the raw byte stream
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(this.worldId);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // Execution on the destination side (The Client)
    public void handle(IPayloadContext context) {
        // Push the assignment to the main thread to prevent concurrency crashes
        context.enqueueWork(() -> {
            WorldIdData.currentWorldUUID = this.worldId;
            ScreenshotContainer.reconstructWorldScreenshots();
        });
    }
}