package com.theuntextured.metascreenshots.gui.poi;

import com.theuntextured.metascreenshots.Config;
import com.theuntextured.metascreenshots.containers.Screenshot;
import com.theuntextured.metascreenshots.containers.ScreenshotContainer;
import com.theuntextured.metascreenshots.gui.IMapDescriptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class POIManager {

    public static List<PointOfInterest> calculateClusters(
            IMapDescriptor mapDescriptor) {
        List<PointOfInterest> clusters = new ArrayList<>();
        final double MERGE_RADIUS_SQ = Config.pinMergeRadius * Config.pinMergeRadius;

        String renderDimension = mapDescriptor.getDimension();

        for (Screenshot screenshot : ScreenshotContainer.worldScreenshots) {
            if (!mapDescriptor.isWithinFrustum(screenshot.position.x, screenshot.position.z)
                    || !Objects.equals(screenshot.dimension, renderDimension)) {
                continue;
            }

            PointOfInterest newPoint = new PointOfInterest(screenshot, mapDescriptor);

            // Secondary Screen Boundary Check. Probably shouldn't be hit but oh well....
            if (newPoint.screenX < -50 || newPoint.screenX > mapDescriptor.getScreenWidth() + 50 ||
                    newPoint.screenY < -50 || newPoint.screenY > mapDescriptor.getScreenHeight() + 50) {
                continue;
            }

            boolean merged = false;

            // Greedy clustering
            PointOfInterest closestCluster = null;
            double closestDistanceSq = Double.MAX_VALUE;
            int closestIndex = -1;

            for (int i = 0; i < clusters.size(); i++) {
                PointOfInterest existing = clusters.get(i);

                double dx = existing.screenX - newPoint.screenX;
                double dy = existing.screenY - newPoint.screenY;
                double distSq = dx * dx + dy * dy;

                if (distSq < MERGE_RADIUS_SQ && distSq < closestDistanceSq) {
                    closestDistanceSq = distSq;
                    closestCluster = existing;
                    closestIndex = i;
                }
            }

            if (closestCluster != null) {
                clusters.set(closestIndex, PointOfInterest.merge(closestCluster, newPoint));
                merged = true;
            }

            if (!merged) clusters.add(newPoint);
        }

        return clusters;
    }
}
