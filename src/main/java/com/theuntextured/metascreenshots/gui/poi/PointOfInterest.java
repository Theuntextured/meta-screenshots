package com.theuntextured.metascreenshots.gui.poi;

import com.theuntextured.metascreenshots.containers.Screenshot;
import com.theuntextured.metascreenshots.gui.IMapDescriptor;
import com.theuntextured.metascreenshots.util.MapDrawingHelper;
import com.theuntextured.metascreenshots.util.ModTextures;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

public class PointOfInterest {
    public double worldX;
    public double worldZ;
    public double screenX;
    public double screenY;

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
    }

    public PointOfInterest(Screenshot screenshot, IMapDescriptor mapDescriptor) {
        this.screenshots.add(screenshot);
        this.worldX = screenshot.position.x;
        this.worldZ = screenshot.position.z;

        // The exact mathematical projection to calculate screen pixels
        this.screenX = (mapDescriptor.getScreenWidth() / 2.0) + ((this.worldX - mapDescriptor.getCamX()) * mapDescriptor.getScale());
        this.screenY = (mapDescriptor.getScreenHeight() / 2.0) + ((this.worldZ - mapDescriptor.getCamZ()) * mapDescriptor.getScale());
    }

    public static PointOfInterest merge(PointOfInterest p1, PointOfInterest p2) {
        return new PointOfInterest(p1, p2);
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        // The drawn GUI size is fixed at 32x32 pixels
        final double width = 32.0;
        final double height = 32.0;

        // OriginX = 0.5 means the image extends 16 pixels left and 16 pixels right
        double minX = this.screenX - (width / 2.0);
        double maxX = this.screenX + (width / 2.0);

        // OriginY = 1.0 means the image extends 32 pixels upwards from the point
        double minY = this.screenY - height;
        double maxY = this.screenY;

        return mouseX >= minX && mouseX <= maxX && mouseY >= minY && mouseY <= maxY;
    }

    public boolean isCluster(){
        return this.screenshots.size() > 1;
    }

    public void render(GuiGraphics graphics, IMapDescriptor descriptor) {
        if (descriptor == null) return;

        boolean mouseOver = isMouseOver(descriptor.getMouseX(), descriptor.getMouseY());

        MapDrawingHelper.renderOnMap(
                graphics,
                descriptor,
                isCluster() ? ModTextures.MULTI_PIN : ModTextures.SINGLE_PIN,
                this.worldX,
                this.worldZ,
                0,
                Integer.MAX_VALUE,
                32,
                1,
                .5,
                mouseOver ? 1.05 : 1
        );
    }
}