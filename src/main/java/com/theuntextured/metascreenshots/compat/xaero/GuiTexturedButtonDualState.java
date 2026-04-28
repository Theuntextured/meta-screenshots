package com.theuntextured.metascreenshots.compat.xaero;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import xaero.lib.client.gui.widget.Tooltip;
import xaero.map.gui.TooltipButton;

import java.util.function.Supplier;

public class GuiTexturedButtonDualState extends TooltipButton {
    protected int textureX;
    protected int textureY;
    protected int textureW;
    protected int textureH;
    protected ResourceLocation textureA;
    protected ResourceLocation textureB;
    protected Supplier<Tooltip> altTooltip;
    public boolean state = true;

    public GuiTexturedButtonDualState(int x, int y, int w, int h, int textureX, int textureY, int textureW, int textureH, ResourceLocation textureA, ResourceLocation textureB, Button.OnPress onPress, Supplier<Tooltip> tooltip, Supplier<Tooltip> altTooltip) {
        super(x, y, w, h, Component.literal(""), onPress, tooltip);
        this.textureX = textureX;
        this.textureY = textureY;
        this.textureW = textureW;
        this.textureH = textureH;
        this.textureA = textureA;
        this.textureB = textureB;
        this.altTooltip = altTooltip;
    }
    @Override
    public Component getMessage() {
        if (state) {
            return (Component)(this.altTooltip != null ? Component.literal(((Tooltip)this.altTooltip.get()).getPlainText()) : super.getMessage());
        }
        return (Component)(this.tooltipSupplier != null ? Component.literal(((Tooltip)this.tooltipSupplier.get()).getPlainText()) : super.getMessage());
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int p_renderButton_1_, int p_renderButton_2_, float p_renderButton_3_) {
        int iconX = this.getX() + this.width / 2 - this.textureW / 2;
        int iconY = this.getY() + this.height / 2 - this.textureH / 2;
        if (this.active) {
            if (this.isHovered) {
                --iconY;
                RenderSystem.setShaderColor(0.9F, 0.9F, 0.9F, 1.0F);
            } else {
                RenderSystem.setShaderColor(0.9882F, 0.9882F, 0.9882F, 1.0F);
            }
        } else {
            RenderSystem.setShaderColor(0.25F, 0.25F, 0.25F, 1.0F);
        }

        if (this.isFocused()) {
            guiGraphics.fill(iconX, iconY, iconX + this.textureW, iconY + this.textureH, 1442840575);
        }

        guiGraphics.blit(
                this.state ? this.textureB : this.textureA,
                iconX, iconY,
                this.textureX, this.textureY,
                this.textureW, this.textureH,
                this.textureW, this.textureH
        );
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
