package com.theuntextured.meta_screenshots.util;

import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

import java.util.UUID;

public class MetaDataHelper {
    static public String currentWorldUUID;
    static public JsonObject getMetadataJson() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        JsonObject metaData = new JsonObject();

        if (player == null || player.level() == null) return metaData;


        metaData.addProperty("x", player.getX());
        metaData.addProperty("y", player.getY());
        metaData.addProperty("z", player.getZ());

        metaData.addProperty("dimension", player.level().dimension().location().toString());

        metaData.addProperty("yaw", player.getYRot());
        metaData.addProperty("pitch", player.getXRot());

        metaData.addProperty("daytime", player.level().getDayTime());

        String worldName = "Unknown";
        String worldId = "Unknown";

        if (mc.hasSingleplayerServer() && mc.getSingleplayerServer() != null) {
            worldName = mc.getSingleplayerServer().getWorldData().getLevelName();
        } else if (mc.getCurrentServer() != null) {
            worldName = mc.getCurrentServer().name;
        }

        if (currentWorldUUID != null && !currentWorldUUID.isEmpty()) {
            worldId = currentWorldUUID;
        } else if (mc.getCurrentServer() != null) {
            // The UUID is null. The mod is missing on the server.
            // Fallback to the IP address.
            worldId = "MP_" + mc.getCurrentServer().ip;
        } else {
            // Extreme failsafe if the UUID failed to generate in Singleplayer
            worldId = "SP_Fallback_" + worldName.replaceAll("[^a-zA-Z0-9.-]", "_");
        }

        metaData.addProperty("world_name", worldName);
        metaData.addProperty("world_id", worldId);

        return metaData;
    }
}
