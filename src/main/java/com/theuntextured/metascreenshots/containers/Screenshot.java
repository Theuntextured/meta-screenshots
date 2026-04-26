package com.theuntextured.metascreenshots.containers;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import com.theuntextured.metascreenshots.util.ImageHelper;
import com.theuntextured.metascreenshots.util.WorldIdData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ScreenshotEvent;

import java.io.File;
import java.util.concurrent.CompletableFuture;

public class Screenshot {

    // --- METADATA ---
    public Vec3 position;
    public double yaw;
    public double pitch;
    public String dimension;
    public long dayTime;
    public String worldName =  "Unknown";
    public String worldId = "Unknown";

    private boolean isValid = false;

    public boolean isValid() { return isValid; }

    public Screenshot(ScreenshotEvent screenshotEvent){
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || player.level() == null) return;

        NativeImage nativeImage = screenshotEvent.getImage();
        targetFile = screenshotEvent.getScreenshotFile();
        thumbFile = new File(new File(targetFile.getParentFile(), "thumbnails"), targetFile.getName());

        position = new Vec3(player.position().x, player.position().y, player.position().z);
        yaw = player.getYRot();
        pitch = player.getXRot();
        dayTime = player.level().getDayTime();

        dimension = player.level().dimension().location().toString();

        if (mc.hasSingleplayerServer() && mc.getSingleplayerServer() != null) {
            worldName = mc.getSingleplayerServer().getWorldData().getLevelName();
        } else if (mc.getCurrentServer() != null) {
            worldName = mc.getCurrentServer().name;
        }

        if (WorldIdData.currentWorldUUID != null && !WorldIdData.currentWorldUUID.isEmpty()) {
            worldId = WorldIdData.currentWorldUUID;
        } else if (mc.getCurrentServer() != null) {
            worldId = "MP_" + mc.getCurrentServer().ip;
        } else {
            worldId = "SP_Fallback_" + worldName.replaceAll("[^a-zA-Z0-9.-]", "_");
        }

        isValid = true;
        var metadataPayload = getMetaData();

        // Pass the raw NativeImage directly to the asynchronous thread
        CompletableFuture.runAsync(() -> ImageHelper.writeImageWithMetadata(nativeImage, targetFile, metadataPayload));
    }

    public Screenshot(File targetFile) {
        this.targetFile = targetFile;
        this.thumbFile = new File(new File(targetFile.getParentFile(), "thumbnails"), targetFile.getName());

        String rawJson = ImageHelper.readMetadataFromImage(targetFile);
        if (rawJson == null || rawJson.isEmpty()) {
            this.isValid = false;
            return;
        }

        try {
            JsonObject data = com.google.gson.JsonParser.parseString(rawJson).getAsJsonObject();

            this.position = new Vec3(data.get("x").getAsDouble(), data.get("y").getAsDouble(), data.get("z").getAsDouble());
            this.yaw = data.get("yaw").getAsDouble();
            this.pitch = data.get("pitch").getAsDouble();
            this.dimension = data.get("dimension").getAsString();
            this.worldName = data.get("worldName").getAsString();
            this.worldId = data.get("worldId").getAsString();
            this.dayTime = data.get("dayTime").getAsLong();

            this.isValid = true;

            // --- ASYNC DDC GENERATION ---
            if (!this.thumbFile.exists()) {
                ImageHelper.generateMissingThumbnailAsync(this.targetFile, this.thumbFile, rawJson);
            }

        } catch (Exception e) {
            LogUtils.getLogger().error("Malformed metadata JSON in screenshot: " + targetFile.getName());
            this.isValid = false;
        }
    }

    public String getMetaData(){
        if(!isValid) return "";

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("x", position.x);
        jsonObject.addProperty("y", position.y);
        jsonObject.addProperty("z", position.z);
        jsonObject.addProperty("yaw", yaw);
        jsonObject.addProperty("pitch", pitch);
        jsonObject.addProperty("dimension", dimension);
        jsonObject.addProperty("worldName", worldName);
        jsonObject.addProperty("worldId", worldId);
        jsonObject.addProperty("dayTime", dayTime);

        return jsonObject.toString();
    }

    // --- TEXTURE VRAM MANAGEMENT ---

    public File targetFile;
    public File thumbFile;

    private ResourceLocation thumbTextureLocation = null;
    private ResourceLocation fullTextureLocation = null;

    public ResourceLocation getThumbnailTexture() {
        if (thumbTextureLocation == null && thumbFile != null && thumbFile.exists()) {
            thumbTextureLocation = loadTextureIntoVRAM(thumbFile, "_thumb");
        }
        return thumbTextureLocation;
    }

    public ResourceLocation getFullTexture() {
        if (fullTextureLocation == null && targetFile.exists()) {
            fullTextureLocation = loadTextureIntoVRAM(targetFile, "_full");
        }
        return fullTextureLocation;
    }

    private ResourceLocation loadTextureIntoVRAM(File file, String suffix) {
        try {
            NativeImage image = NativeImage.read(new java.io.FileInputStream(file));
            net.minecraft.client.renderer.texture.DynamicTexture texture = new net.minecraft.client.renderer.texture.DynamicTexture(image);
            return Minecraft.getInstance().getTextureManager().register(
                    "meta_screenshots/screenshots" + file.getName().toLowerCase().replace(".png", "") + suffix,
                    texture
            );
        } catch (Exception e) {
            LogUtils.getLogger().error("Failed to load texture into VRAM: " + file.getName(), e);
            return null;
        }
    }

    public void freeTextures() {
        if (thumbTextureLocation != null) {
            Minecraft.getInstance().getTextureManager().release(thumbTextureLocation);
            thumbTextureLocation = null;
        }
        if (fullTextureLocation != null) {
            Minecraft.getInstance().getTextureManager().release(fullTextureLocation);
            fullTextureLocation = null;
        }
    }
}
