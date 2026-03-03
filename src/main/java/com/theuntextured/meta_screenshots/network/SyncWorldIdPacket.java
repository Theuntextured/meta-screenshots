package com.theuntextured.meta_screenshots.network;

import com.theuntextured.meta_screenshots.containers.ScreenshotContainer;
import com.theuntextured.meta_screenshots.util.WorldIdData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public class SyncWorldIdPacket {
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
        buf.writeUtf(worldId);
    }

    // Execution on the destination side (The Client)
    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();

        // Push the assignment to the main thread to prevent concurrency crashes
        ctx.enqueueWork(() -> {
            WorldIdData.currentWorldUUID = this.worldId;
            ScreenshotContainer.reconstructWorldScreenshots();
        });

        ctx.setPacketHandled(true);
        return true;
    }
}