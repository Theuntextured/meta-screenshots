package com.theuntextured.meta_screenshots.compat;

import com.mojang.blaze3d.systems.RenderSystem;
import com.theuntextured.meta_screenshots.mixin.XaeroFullscreenMapAccessor;
import net.minecraft.resources.ResourceLocation;

public class XaeroIntegrationWrapper {
    public static void onRender(net.minecraft.client.gui.GuiGraphics graphics, xaero.map.gui.GuiMap screen, int mX, int mY, float pt) {
        if(!com.theuntextured.meta_screenshots.Config.modEnabled) return;

        String renderedDimension = ((XaeroFullscreenMapAccessor)(Object)screen).meta_screenshots$getMapProcessor().getMapWorld().getCurrentDimension().getDimId().location().toString();

        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();

        // Cache the engine state exactly once per frame
        XaerosMapDescriptor mapDescriptor = new XaerosMapDescriptor(screen);

        for(var screenshot : com.theuntextured.meta_screenshots.containers.ScreenshotContainer.worldScreenshots){
            if(!java.util.Objects.equals(screenshot.dimension, renderedDimension)) continue;

            ResourceLocation thumb = screenshot.getThumbnailTexture();
            if (thumb == null) continue;

            renderOnMap(graphics, mapDescriptor, screenshot.position.x, screenshot.position.z, 8, 64, 128, thumb);
        }
    }

    public static void renderOnMap(net.minecraft.client.gui.GuiGraphics graphics, XaerosMapDescriptor descriptor, double worldX, double worldZ, int minDrawSize, int maxDrawSize, int desiredHeight, ResourceLocation texture) {

        // 1. High-Performance Frustum Culling (Zero objects allocated)
        if (!descriptor.isWithinFrustum(worldX, worldZ)) {
            return;
        }

        // 2. Calculate the spatial offset from the camera's centre
        double deltaX = worldX - descriptor.camX;
        double deltaZ = worldZ - descriptor.camZ;

        // 3. Determine target size in world blocks
        double targetBlockHeight = desiredHeight / descriptor.scale;
        double clampedBlockHeight = net.minecraft.util.Mth.clamp(targetBlockHeight, minDrawSize, maxDrawSize);
        double clampedBlockWidth = clampedBlockHeight * (16.0 / 9.0);

        // 4. Convert the clamped block size back into native GUI pixels
        double guiPixelHeight = clampedBlockHeight * descriptor.scale;
        double guiPixelWidth = clampedBlockWidth * descriptor.scale;

        // 5. Execute the Render Matrix
        com.mojang.blaze3d.vertex.PoseStack pose = graphics.pose();
        pose.pushPose();

        pose.translate(descriptor.screenWidth / 2.0f, descriptor.screenHeight / 2.0f, 0);
        pose.scale((float) descriptor.scale, (float) descriptor.scale, 1);
        pose.translate(deltaX, deltaZ, 0);

        // Invert the map scale to prevent integer snapping on the blit call
        pose.scale(1.0f / (float)descriptor.scale, 1.0f / (float)descriptor.scale, 1.0f);

        graphics.blit(texture,
                (int) (-guiPixelWidth / 2.0),
                (int) (-guiPixelHeight / 2.0),
                0, 0,
                (int) guiPixelWidth, (int) guiPixelHeight,
                (int) guiPixelWidth, (int) guiPixelHeight);

        pose.popPose();
    }
}