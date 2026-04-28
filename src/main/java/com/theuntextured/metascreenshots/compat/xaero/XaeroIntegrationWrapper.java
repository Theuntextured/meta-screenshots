package com.theuntextured.metascreenshots.compat.xaero;

import com.theuntextured.metascreenshots.Config;
import com.theuntextured.metascreenshots.gui.IMapDescriptor;
import com.theuntextured.metascreenshots.gui.MapOverlayManager;
import net.minecraft.client.gui.GuiGraphics;

public class XaeroIntegrationWrapper {
    public static void onRender(GuiGraphics graphics, xaero.map.gui.GuiMap screen, int mX, int mY, float pt) {
        if(!Config.modEnabled) return;

        IMapDescriptor mapDescriptor = new XaeroMapDescriptor(screen, mX, mY);

        MapOverlayManager.onRender(graphics, mapDescriptor);
    }
}
