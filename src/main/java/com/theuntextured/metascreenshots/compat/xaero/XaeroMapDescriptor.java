package com.theuntextured.metascreenshots.compat.xaero;

import com.theuntextured.metascreenshots.gui.IMapDescriptor;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.Window;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import xaero.map.gui.GuiMap;
import com.theuntextured.metascreenshots.mixin.XaeroFullscreenMapAccessor;

@OnlyIn(Dist.CLIENT)
public class XaeroMapDescriptor implements IMapDescriptor {

    private final double camX;
    private final double camZ;
    private final double scale;
    private final int screenWidth;
    private final int screenHeight;

    // Frustum boundaries in world coordinates for zero-allocation culling
    private final double minWorldX;
    private final double maxWorldX;
    private final double minWorldZ;
    private final double maxWorldZ;
    private final double mouseX;
    private final double mouseY;
    private final String dimension;

    public XaeroMapDescriptor(GuiMap screen, double mX, double mY) {
        this.mouseX = mX;
        this.mouseY = mY;

        this.dimension = ((XaeroFullscreenMapAccessor)(Object) screen).meta_screenshots$getMapProcessor()
                .getMapWorld().getCurrentDimension().getDimId().location().toString();


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

    @Override
    public double getCamX() {
        return this.camX;
    }

    @Override
    public double getCamZ() {
        return this.camZ;
    }

    @Override
    public double getScale() {
        return this.scale;
    }

    @Override
    public int getScreenWidth() {
        return this.screenWidth;
    }

    @Override
    public int getScreenHeight() {
        return this.screenHeight;
    }

    @Override
    public double getMinWorldX() {
        return this.minWorldX;
    }

    @Override
    public double getMaxWorldX() {
        return this.maxWorldX;
    }

    @Override
    public double getMinWorldZ() {
        return this.minWorldZ;
    }

    @Override
    public double getMaxWorldZ() {
        return this.maxWorldZ;
    }

    @Override
    public double getMouseX() {
        return  this.mouseX;
    }

    @Override
    public double getMouseY() {
        return this.mouseY;
    }

    @Override
    public String getDimension() {
        return  this.dimension;
    }
}