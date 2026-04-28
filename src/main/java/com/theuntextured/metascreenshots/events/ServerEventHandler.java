package com.theuntextured.metascreenshots.events;

import com.theuntextured.metascreenshots.MetaScreenshots;
import com.theuntextured.metascreenshots.network.ModMessages;
import com.theuntextured.metascreenshots.network.SyncWorldIdPacket;
import com.theuntextured.metascreenshots.util.WorldIdData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = MetaScreenshots.MOD_ID)
public class ServerEventHandler {

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (!player.connection.hasChannel(SyncWorldIdPacket.TYPE)) return;
            MinecraftServer server = player.getServer();
            if (server != null) {
                // Always anchor global data to the Overworld, even if the player logs into the Nether
                ServerLevel overworld = server.getLevel(Level.OVERWORLD);
                if (overworld != null) {
                    WorldIdData data = overworld.getDataStorage().computeIfAbsent(
                            WorldIdData.factory(),
                            "meta_screenshot_id"
                    );
                    ModMessages.sendToPlayer(new SyncWorldIdPacket(data.getWorldId()), player);
                }
            }
        }
    }
}