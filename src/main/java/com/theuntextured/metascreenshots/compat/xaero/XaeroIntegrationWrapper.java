package com.theuntextured.metascreenshots.compat.xaero;

import com.mojang.blaze3d.systems.RenderSystem;
import com.theuntextured.metascreenshots.Config;
import com.theuntextured.metascreenshots.gui.IMapDescriptor;
import com.theuntextured.metascreenshots.gui.poi.POIManager;
import com.theuntextured.metascreenshots.gui.poi.PointOfInterest;
import com.theuntextured.metascreenshots.mixin.XaeroFullscreenMapAccessor;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;

public class XaeroIntegrationWrapper {
    public static void onRender(GuiGraphics graphics, xaero.map.gui.GuiMap screen, int mX, int mY, float pt) {
        if(!Config.modEnabled) return;

        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();

        // Cache the engine state exactly once per frame
        IMapDescriptor mapDescriptor = new XaeroMapDescriptor(screen, mX, mY);

        List<PointOfInterest> POIs = POIManager.calculateClusters(mapDescriptor);

        for (PointOfInterest POI : POIs) {
            POI.render(graphics, mapDescriptor);
        }
    }
}
