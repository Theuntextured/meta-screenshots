package com.theuntextured.metascreenshots.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.theuntextured.metascreenshots.gui.tooltip.TextTooltip;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class IconButton extends AbstractButton {
    private final ResourceLocation texture;
    private final Runnable onPressAction;
    private final TextTooltip textTooltip;

    public IconButton(int x, int y, int width, int height, ResourceLocation texture, Runnable onPressAction, String tooltipText) {
        super(x, y, width, height, Component.empty());
        this.texture = texture;
        this.onPressAction = onPressAction;
        if(tooltipText != null)
            textTooltip = new TextTooltip(tooltipText);
        else textTooltip = null;
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // The AbstractButton parent class automatically updates this boolean! No manual math required.
        if (this.isHovered) {
            // Apply a slight visual tint when hovered so the user knows it is interactive
            RenderSystem.setShaderColor(0.9F, 0.9F, 0.9F, 1.0F);
            graphics.blit(this.texture, this.getX(), this.getY() - 1, 0, 0, this.width, this.height, this.width, this.height);
            if (textTooltip != null) textTooltip.renderImage(mouseX, mouseY, graphics);
        } else {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            graphics.blit(this.texture, this.getX(), this.getY(), 0, 0, this.width, this.height, this.width, this.height);
        }

        RenderSystem.enableBlend();

        // Draw the icon dynamically scaled to whatever width/height you passed to the constructor

        // Clean up the shader colour so we don't contaminate the rest of the UI
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public void onPress() {
        // Execute whatever custom action you assigned to this button
        this.onPressAction.run();
    }

    @Override
    protected void updateWidgetNarration(net.minecraft.client.gui.narration.NarrationElementOutput output) {
        // Required by the engine's accessibility features, though irrelevant for a simple icon
        this.defaultButtonNarrationText(output);
    }
}