package com.theuntextured.metascreenshots.gui.tooltip;

import com.theuntextured.metascreenshots.containers.Screenshot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ThumbnailTooltipComponent {
    private final Screenshot screenshot;
    List<Component> strings = new ArrayList<>();

    public ThumbnailTooltipComponent(@NotNull Screenshot screenshot) {
        this.screenshot = screenshot;
        this.strings.add(Component.literal(screenshot.targetFile.getName().replace(".png", "")));
    }

    public void renderImage(int x, int y, @NotNull GuiGraphics graphics) {
        ResourceLocation thumbResource = screenshot.getThumbnailTexture();
        if (thumbResource == null) {
            return;
        }

        final int desiredHeight = 90;
        final int desiredWidth = (int) (screenshot.getThumbnailAspectRatio() * desiredHeight);

        // Calculate total tooltip dimensions first
        final int boxWidth = desiredWidth + 20;
        int h = 5 + strings.size() * 10 + desiredHeight + 5;

        // Fetch the actual screen boundaries from the graphics engine
        int screenWidth = graphics.guiWidth();
        int screenHeight = graphics.guiHeight();

        // Default positioning: slightly offset to the bottom-right of the cursor
        int drawX = x + 12;
        int drawY = y + 10;

        // --- EDGE DETECTION X ---
        if (drawX + boxWidth > screenWidth) {
            // If it bleeds off the right edge, flip it to the left side of the cursor
            drawX = x - 12 - boxWidth;
        }
        // Hard clamp to ensure it never leaves the screen under any circumstance
        if (drawX < 0) {
            drawX = 0;
        } else if (drawX + boxWidth > screenWidth) {
            drawX = screenWidth - boxWidth;
        }

        // --- EDGE DETECTION Y ---
        if (drawY + h > screenHeight) {
            // If it bleeds off the bottom edge, flip it above the cursor
            drawY = y - 10 - h;
        }
        // Hard clamp to ensure it never leaves the screen
        if (drawY < 0) {
            drawY = 0;
        } else if (drawY + h > screenHeight) {
            drawY = screenHeight - h;
        }

        // --- RENDERING ---
        graphics.fill(drawX, drawY, drawX + boxWidth, drawY + h, -939524096);

        for(int i = 0; i < this.strings.size(); ++i) {
            Component s = this.strings.get(i);
            graphics.drawString(Minecraft.getInstance().font, s, drawX + 10, drawY + 5 + 10 * i, 16777215);
        }

        graphics.blit(
                thumbResource,
                drawX + 10,
                drawY + 5 + 10 * this.strings.size(),
                desiredWidth, desiredHeight,
                0, 0,
                screenshot.thumbWidth, screenshot.thumbHeight,
                screenshot.thumbWidth, screenshot.thumbHeight
        );
    }
}
