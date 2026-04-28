package com.theuntextured.metascreenshots.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.theuntextured.metascreenshots.Config;
import com.theuntextured.metascreenshots.gui.poi.POIManager;
import com.theuntextured.metascreenshots.gui.poi.PointOfInterest;
import com.theuntextured.metascreenshots.gui.screen.FullscreenScreenshotScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MapOverlayManager {
    // The shared memory pointer, now centralised
    public static PointOfInterest activeHoveredPOI = null;

    public static void onRender(GuiGraphics graphics, IMapDescriptor mapDescriptor) {
        if (!Config.modEnabled) return;

        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();

        // The POIManager now strictly requires the abstracted map data
        POIManager manager = new POIManager(mapDescriptor);
        manager.render(graphics);
    }

    public static boolean onClick(int button, Screen parentScreen) {
        if (button == 0 && activeHoveredPOI != null) {
            PointOfInterest target = activeHoveredPOI;

            if (!target.isCluster()) {
                SimpleSoundInstance sound = SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F);
                Minecraft.getInstance().getSoundManager().play(sound);

                Minecraft.getInstance().setScreen(new FullscreenScreenshotScreen(parentScreen, target.screenshots.getFirst()));
            }
            return true;
        }
        return false;
    }
}