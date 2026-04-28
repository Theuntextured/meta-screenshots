package com.theuntextured.metascreenshots.containers;

import com.mojang.logging.LogUtils;
import com.theuntextured.metascreenshots.Config;
import com.theuntextured.metascreenshots.util.WorldIdData;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.io.File;
import java.util.*;

@OnlyIn(Dist.CLIENT)
public class ScreenshotContainer {
    static public List<Screenshot> allScreenshots = new ArrayList<>();
    static public List<Screenshot> worldScreenshots = new ArrayList<>();

    public static void init() {
        File screenshotDir = new File(Minecraft.getInstance().gameDirectory, "screenshots");

        LogUtils.getLogger().info("Searching screenshots in " + screenshotDir.getAbsolutePath());

        if (screenshotDir.exists() && screenshotDir.isDirectory()) {
            File[] files = screenshotDir.listFiles();

            if (files != null) for (File file : files)
                if (file.isFile() && file.getName().toLowerCase().endsWith(".png")) {
                    Screenshot screenshot = new Screenshot(file);
                    if (screenshot.isValid()) {
                        allScreenshots.add(screenshot);
                        LogUtils.getLogger().info("Found screenshot: " + file);
                    }
                }
        }
    }

    public static void reconstructWorldScreenshots() {
        worldScreenshots.clear();
        String currentWorldUUID = WorldIdData.currentWorldUUID;
        if(currentWorldUUID == null || !Config.modEnabled) return;
        LogUtils.getLogger().info("Searching screenshots for world " + currentWorldUUID);
        for (Screenshot screenshot : allScreenshots)
            if (screenshot.isValid() && Objects.equals(screenshot.worldId, currentWorldUUID))
            {
                worldScreenshots.add(screenshot);
                LogUtils.getLogger().info("Found screenshot: " + screenshot.targetFile.toString());
            }
        // Annoying dude asked for this for stable POI sorting
        worldScreenshots.sort(Comparator.comparingLong((Screenshot s) -> s.dayTime));
    }

    public static void setModEnabled(boolean enabled) {
        //reset
        worldScreenshots.clear();
        allScreenshots.forEach(Screenshot::freeTextures);
        allScreenshots.clear();

        Config.setModEnabled(enabled);

        if (Config.modEnabled) {
            init();
            reconstructWorldScreenshots();
        }
    }
}