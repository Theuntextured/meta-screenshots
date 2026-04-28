package com.theuntextured.metascreenshots.containers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import com.theuntextured.metascreenshots.Config;
import com.theuntextured.metascreenshots.util.ImageHelper;
import com.theuntextured.metascreenshots.util.WorldIdData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.ScreenshotEvent;

import java.io.File;
import java.util.concurrent.CompletableFuture;

@OnlyIn(Dist.CLIENT)
public class Screenshot {

    public Vec3 position;
    public double yaw;
    public double pitch;
    public String dimension;
    public long dayTime;
    public String worldName =  "Unknown";
    public String worldId = "Unknown";
    public int width;
    public int height;
    public int thumbWidth;
    public int thumbHeight;

    private boolean isValid = false;

    public boolean isValid() { return isValid; }
    public double getAspectRatio(){
        return (double) width / (double) height;
    }
    public double getThumbnailAspectRatio(){
        return (double) thumbWidth / (double) thumbHeight;
    }

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
        this.width = nativeImage.getWidth();
        this.height = nativeImage.getHeight();
        //same logic as in helper
        float ratio = (float) this.width / this.height;
        this.thumbHeight = Config.thumbnailHeight;
        this.thumbWidth = Math.round(Config.thumbnailHeight * ratio);

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
            JsonObject data = JsonParser.parseString(rawJson).getAsJsonObject();

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

    public ResourceLocation getFullTexture() {
        if (fullTextureLocation == null && targetFile.exists()) {
            // The try-with-resources block strictly seals the memory leak
            try (java.io.FileInputStream stream = new java.io.FileInputStream(targetFile)) {
                NativeImage image = NativeImage.read(stream);
                net.minecraft.client.renderer.texture.DynamicTexture texture = new net.minecraft.client.renderer.texture.DynamicTexture(image);

                // Force bilinear filtering so downscaling the image to the GUI looks smooth
                texture.setFilter(true, false);

                width = image.getWidth();
                height = image.getHeight();
                fullTextureLocation = Minecraft.getInstance().getTextureManager().register(
                        "meta_screenshots/screenshots_" + targetFile.getName().toLowerCase().replace(".png", ""),
                        texture
                );
            } catch (Exception e) {
                LogUtils.getLogger().error("Failed to load texture into VRAM: " + targetFile.getName(), e);
                fullTextureLocation = null;
            }
        }
        return fullTextureLocation;
    }

    public ResourceLocation getThumbnailTexture() {
        if (thumbTextureLocation == null && thumbFile.exists()) {
            try (java.io.FileInputStream stream = new java.io.FileInputStream(thumbFile)) {
                NativeImage image = NativeImage.read(stream);
                net.minecraft.client.renderer.texture.DynamicTexture texture = new net.minecraft.client.renderer.texture.DynamicTexture(image);

                texture.setFilter(true, false);

                thumbWidth = image.getWidth();
                thumbHeight = image.getHeight();
                thumbTextureLocation = Minecraft.getInstance().getTextureManager().register(
                        "meta_screenshots/thumbnails_" + thumbFile.getName().toLowerCase().replace(".png", ""),
                        texture
                );
            } catch (Exception e) {
                LogUtils.getLogger().error("Failed to load texture into VRAM: " + thumbFile.getName(), e);
                thumbTextureLocation = null;
            }
        }
        return thumbTextureLocation;
    }
    public void freeFullTexture() {
        if (fullTextureLocation != null) {
            Minecraft.getInstance().getTextureManager().release(fullTextureLocation);
            fullTextureLocation = null;
        }
    }

    public void freeTextures() {
        if (thumbTextureLocation != null) {
            Minecraft.getInstance().getTextureManager().release(thumbTextureLocation);
            thumbTextureLocation = null;
        }
        freeFullTexture();
    }
}
