package com.theuntextured.metascreenshots.gui.screen;

import com.theuntextured.metascreenshots.containers.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class FullscreenScreenshotScreen extends Screen {
    private final Screen parentScreen;
    private final Screenshot screenshot;

    public FullscreenScreenshotScreen(Screen parentScreen, Screenshot screenshot) {
        super(Component.literal("Screenshot Viewer"));
        this.parentScreen = parentScreen;
        this.screenshot = screenshot;
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Draw a dark, semi-transparent background to isolate the image
        this.renderBackground(graphics, mouseX, mouseY, partialTick);

        ResourceLocation texture = this.screenshot.getFullTexture();
        if (texture != null) {
            // Keep the 16:9 aspect ratio intact without stretching
            float screenRatio = (float) this.width / this.height;
            float imageRatio = 16.0f / 9.0f; // Assuming standard 1080p screenshots

            int drawWidth = this.width;
            int drawHeight = this.height;

            if (screenRatio > imageRatio) {
                drawWidth = (int) (this.height * imageRatio);
            } else {
                drawHeight = (int) (this.width / imageRatio);
            }

            int xPos = (this.width - drawWidth) / 2;
            int yPos = (this.height - drawHeight) / 2;

            graphics.blit(texture, xPos, yPos, 0, 0, drawWidth, drawHeight, drawWidth, drawHeight);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        // Return seamlessly to the map
        assert this.minecraft != null;
        this.minecraft.setScreen(this.parentScreen);
    }
}