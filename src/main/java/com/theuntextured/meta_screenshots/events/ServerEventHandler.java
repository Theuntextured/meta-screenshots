package com.theuntextured.meta_screenshots.events;

import com.theuntextured.meta_screenshots.MetaScreenshots;
import com.theuntextured.meta_screenshots.network.ModMessages;
import com.theuntextured.meta_screenshots.network.SyncWorldIdPacket;
import com.theuntextured.meta_screenshots.util.WorldIdData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MetaScreenshots.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerEventHandler {

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            MinecraftServer server = player.getServer();
            if (server != null) {
                // Always anchor global data to the Overworld, even if the player logs into the Nether
                ServerLevel overworld = server.getLevel(Level.OVERWORLD);
                if (overworld != null) {
                    DimensionDataStorage storage = overworld.getDataStorage();

                    // Fetch the UUID file, or create it if it doesn't exist
                    WorldIdData data = storage.computeIfAbsent(
                            WorldIdData::load,
                            WorldIdData::new,
                            "meta_screenshot_id"
                    );

                    // Blast the payload to the client
                    ModMessages.sendToPlayer(new SyncWorldIdPacket(data.getWorldId()), player);
                }
            }
        }
    }
}