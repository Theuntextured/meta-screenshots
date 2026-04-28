package com.theuntextured.metascreenshots.gui.poi;

import com.mojang.blaze3d.systems.RenderSystem;
import com.theuntextured.metascreenshots.containers.Screenshot;
import com.theuntextured.metascreenshots.gui.IMapDescriptor;
import com.theuntextured.metascreenshots.gui.tooltip.TextTooltip;
import com.theuntextured.metascreenshots.gui.tooltip.ThumbnailTooltipComponent;
import com.theuntextured.metascreenshots.util.MapDrawingHelper;
import com.theuntextured.metascreenshots.util.ModTextures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class PointOfInterest {
    static final int ICON_SIZE = 16;
    public double worldX;
    public double worldZ;
    public double screenX;
    public double screenY;
    public boolean bIsMouseOver;

    public final List<Screenshot> screenshots = new ArrayList<>();

    public PointOfInterest(PointOfInterest p1, PointOfInterest p2) {
        final int scrCount1 = p1.screenshots.size();
        final int scrCount2 = p2.screenshots.size();

        this.screenshots.addAll(p1.screenshots);
        this.screenshots.addAll(p2.screenshots);

        final int scrCount = this.screenshots.size();

        final double weight1 = (double) scrCount1 / scrCount;
        final double weight2 = (double) scrCount2 / scrCount;

        this.worldX = p1.worldX * weight1 + p2.worldX * weight2;
        this.worldZ = p1.worldZ * weight1 + p2.worldZ * weight2;
        this.screenX = p1.screenX * weight1 + p2.screenX * weight2;
        this.screenY = p1.screenY * weight1 + p2.screenY * weight2;
        bIsMouseOver = false;
    }

    public PointOfInterest(Screenshot screenshot, IMapDescriptor mapDescriptor) {
        this.screenshots.add(screenshot);
        this.worldX = screenshot.position.x;
        this.worldZ = screenshot.position.z;

        // The exact mathematical projection to calculate screen pixels
        this.screenX = (mapDescriptor.getScreenWidth() / 2.0) + ((this.worldX - mapDescriptor.getCamX()) * mapDescriptor.getScale());
        this.screenY = (mapDescriptor.getScreenHeight() / 2.0) + ((this.worldZ - mapDescriptor.getCamZ()) * mapDescriptor.getScale());
        bIsMouseOver = false;
    }

    public static PointOfInterest merge(PointOfInterest p1, PointOfInterest p2) {
        return new PointOfInterest(p1, p2);
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        // OriginX = 0.5 means the image extends ICON_SIZE pixels left and ICON_SIZE pixels right
        double minX = this.screenX - ((double)ICON_SIZE / 2.0);
        double maxX = this.screenX + ((double)ICON_SIZE / 2.0);

        // OriginY = 1.0 means the image extends ICON_SIZE pixels upwards from the point
        double minY = this.screenY - (double)ICON_SIZE;
        double maxY = this.screenY;

        return mouseX >= minX && mouseX <= maxX && mouseY >= minY && mouseY <= maxY;
    }

    public boolean isCluster(){
        return this.screenshots.size() > 1;
    }

    public void render(GuiGraphics graphics, IMapDescriptor descriptor) {
        if (descriptor == null) return;
        if (bIsMouseOver) RenderSystem.setShaderColor(0.9F, 0.9F, 0.9F, 1.0F);

        ResourceLocation resource = (isCluster() ? (screenshots.size() == 2 ? ModTextures.DOUBLE_PIN : ModTextures.MULTI_PIN) : ModTextures.SINGLE_PIN);

        MapDrawingHelper.renderOnMap(
                graphics,
                descriptor,
                resource,
                this.worldX,
                this.worldZ,
                0,
                Integer.MAX_VALUE,
                ICON_SIZE,
                1,
                .5,
                bIsMouseOver ? 1.1 : 1
        );

        if (!bIsMouseOver) return;

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        if (isCluster()) {
            var tooltip = new TextTooltip(screenshots.size() + " Screenshots");
            tooltip.renderImage(
                    (int) descriptor.getMouseX(),
                    (int) descriptor.getMouseY(),
                    graphics
            );
        } else {
            ThumbnailTooltipComponent tooltip = new ThumbnailTooltipComponent(screenshots.getFirst());
            tooltip.renderImage((int) descriptor.getMouseX(), (int) descriptor.getMouseY(), graphics);
        }
    }
}
