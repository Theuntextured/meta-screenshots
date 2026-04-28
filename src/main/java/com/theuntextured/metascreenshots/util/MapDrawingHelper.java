package com.theuntextured.metascreenshots.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.theuntextured.metascreenshots.gui.IMapDescriptor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MapDrawingHelper {
    public static void renderOnMap(GuiGraphics graphics, IMapDescriptor descriptor, ResourceLocation texture,
                                   double worldX, double worldZ,
                                   int minDrawSize, int maxDrawSize,
                                   int desiredHeight, double aspectRatio,
                                   double OriginX, double OriginY) {

        if (!descriptor.isWithinFrustum(worldX, worldZ)) return;

        double deltaX = worldX - descriptor.getCamX();
        double deltaZ = worldZ - descriptor.getCamZ();

        final double scale = descriptor.getScale();

        double targetBlockHeight = desiredHeight / scale;
        double clampedBlockHeight = Mth.clamp(targetBlockHeight, minDrawSize, maxDrawSize);
        double clampedBlockWidth = clampedBlockHeight * aspectRatio;

        double guiPixelHeight = clampedBlockHeight * scale;
        double guiPixelWidth = clampedBlockWidth * scale;

        int guiPixelPosX = (int) -(guiPixelWidth * OriginX);
        int guiPixelPosY = (int) -(guiPixelHeight * OriginY);

        PoseStack pose = graphics.pose();
        pose.pushPose();

        pose.translate(descriptor.getScreenWidth() / 2.0f, descriptor.getScreenHeight() / 2.0f, 0);
        pose.scale((float) scale, (float) scale, 1);
        pose.translate(deltaX, deltaZ, 0);

        pose.scale(1.0f / (float)scale, 1.0f / (float)scale, 1.0f);

        graphics.blit(texture,
                guiPixelPosX,
                guiPixelPosY,
                0, 0,
                (int) guiPixelWidth, (int) guiPixelHeight,
                (int) guiPixelWidth, (int) guiPixelHeight);

        pose.popPose();
    }
}
