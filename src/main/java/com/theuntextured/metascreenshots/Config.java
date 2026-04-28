package com.theuntextured.metascreenshots;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = MetaScreenshots.MOD_ID)
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue MOD_ENABLED = BUILDER
            .comment("Whether the mod should be enabled")
            .define("modEnabled", true);
    private static final ModConfigSpec.BooleanValue COPY_TO_CLIPBOARD = BUILDER
            .comment("Copy screenshots to clipboard")
            .define("copyToClipboard", true);
    private static final ModConfigSpec.IntValue THUMBNAIL_HEIGHT = BUILDER
            .comment("Screenshot thumbnail height in pixels")
            .defineInRange("thumbnailHeight", 240, 16, 1080);
    private static final ModConfigSpec.DoubleValue PIN_MERGE_RADIUS = BUILDER
            .comment("Maximum distance between ping in a map to merge.")
            .defineInRange("pinMergeRadius", 24.0, 0.0, Double.MAX_VALUE);
    public static final ModConfigSpec SPEC = BUILDER.build();

    public static boolean modEnabled = true;
    public static boolean copyToClipboard = true;
    public static int thumbnailHeight = 240;
    public static double pinMergeRadius = 0.0;
    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        modEnabled = MOD_ENABLED.get();
        copyToClipboard = COPY_TO_CLIPBOARD.get();
        thumbnailHeight = THUMBNAIL_HEIGHT.get();
        pinMergeRadius = PIN_MERGE_RADIUS.get();
    }

    // --- SETTERS ---

    public static void setModEnabled(boolean value) {
        modEnabled = value;
        MOD_ENABLED.set(value);
    }

    public static void setCopyToClipboard(boolean value) {
        copyToClipboard = value;
        COPY_TO_CLIPBOARD.set(value);
    }

    public static void setThumbnailHeight(int value) {
        thumbnailHeight = value;
        THUMBNAIL_HEIGHT.set(value);
    }

    public static void setPinMergeRadius(double value) {
        pinMergeRadius = value;
        PIN_MERGE_RADIUS.set(value);
    }
}
