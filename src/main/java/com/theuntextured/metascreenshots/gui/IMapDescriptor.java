package com.theuntextured.metascreenshots.gui;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface IMapDescriptor {
    double getCamX();
    double getCamZ();
    double getScale();
    int getScreenWidth();
    int getScreenHeight();

    // Frustum boundaries in world coordinates for zero-allocation culling
    double getMinWorldX();
    double getMaxWorldX();
    double getMinWorldZ();
    double getMaxWorldZ();
    double getMouseX();
    double getMouseY();
    String getDimension();
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    default boolean isWithinFrustum(double worldX, double worldZ){
        return worldX >= getMinWorldX() && worldX <= getMaxWorldX() &&
                worldZ >= getMinWorldZ() && worldZ <= getMaxWorldZ();
    }
}
