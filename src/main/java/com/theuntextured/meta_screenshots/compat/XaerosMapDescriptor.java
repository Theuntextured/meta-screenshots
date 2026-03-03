package com.theuntextured.meta_screenshots.compat;

import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.Window;
import xaero.map.gui.GuiMap;
import com.theuntextured.meta_screenshots.mixin.XaeroFullscreenMapAccessor;

public class XaerosMapDescriptor {
    public final double camX;
    public final double camZ;
    public final double scale;
    public final int screenWidth;
    public final int screenHeight;

    // Frustum boundaries in world coordinates for zero-allocation culling
    private final double minWorldX;
    private final double maxWorldX;
    private final double minWorldZ;
    private final double maxWorldZ;

    public XaerosMapDescriptor(GuiMap screen) {
        XaeroFullscreenMapAccessor accessor = (XaeroFullscreenMapAccessor) (Object) screen;
        this.camX = accessor.meta_screenshots$getCameraX();
        this.camZ = accessor.meta_screenshots$getCameraZ();
        double mapScale = accessor.meta_screenshots$getScale();

        Window window = Minecraft.getInstance().getWindow();
        this.screenWidth = window.getGuiScaledWidth();
        this.screenHeight = window.getGuiScaledHeight();

        double guiScale = (double) window.getScreenWidth() / this.screenWidth;
        this.scale = mapScale / guiScale;

        // Calculate the world-space viewport boundaries once
        double halfWidth = (this.screenWidth / 2.0) / this.scale;
        double halfHeight = (this.screenHeight / 2.0) / this.scale;
        double margin = 32.0 / this.scale; // 32 screen pixels converted to world space

        this.minWorldX = this.camX - halfWidth - margin;
        this.maxWorldX = this.camX + halfWidth + margin;
        this.minWorldZ = this.camZ - halfHeight - margin;
        this.maxWorldZ = this.camZ + halfHeight + margin;
    }

    public boolean isWithinFrustum(double worldX, double worldZ) {
        return worldX >= minWorldX && worldX <= maxWorldX &&
                worldZ >= minWorldZ && worldZ <= maxWorldZ;
    }
}