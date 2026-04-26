package com.theuntextured.metascreenshots.containers;

import com.mojang.logging.LogUtils;
import com.theuntextured.metascreenshots.util.WorldIdData;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.util.HashSet;
import java.util.Objects;

public class ScreenshotContainer {
    static public HashSet<Screenshot> allScreenshots = new HashSet<>();
    static public HashSet<Screenshot> worldScreenshots = new HashSet<>();

    public static void Initialize() {
        // Access the default screenshots directory in the Minecraft instance
        File screenshotDir = new File(Minecraft.getInstance().gameDirectory, "screenshots");

        LogUtils.getLogger().info("Searching screenshots in " + screenshotDir.getAbsolutePath());

        if (screenshotDir.exists() && screenshotDir.isDirectory()) {
            File[] files = screenshotDir.listFiles();

            // Filter for PNG files and skip directories (such as 'thumbnails')
            if (files != null) for (File file : files)
                if (file.isFile() && file.getName().toLowerCase().endsWith(".png")) {
                    Screenshot screenshot = new Screenshot(file);
                    if (screenshot.isValid()) {
                        allScreenshots.add(screenshot);
                        LogUtils.getLogger().info("Found screenshot: " + file.toString());
                    }
                }
        }
    }

    public static void reconstructWorldScreenshots() {
        worldScreenshots.clear();
        String currentWorldUUID = WorldIdData.currentWorldUUID;
        if(currentWorldUUID == null) return;
        LogUtils.getLogger().info("Searching screenshots for world " + currentWorldUUID);
        for (Screenshot screenshot : allScreenshots)
            if (screenshot.isValid() && Objects.equals(screenshot.worldId, currentWorldUUID))
            {
                worldScreenshots.add(screenshot);
                LogUtils.getLogger().info("Found screenshot: " + screenshot.targetFile.toString());
            }
    }
}