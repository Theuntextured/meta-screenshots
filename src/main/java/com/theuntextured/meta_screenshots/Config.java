package com.theuntextured.meta_screenshots;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = MetaScreenshots.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue MOD_ENABLED = BUILDER.comment("Whether the mod should be enabled").define("modEnabled", true);
    private static final ForgeConfigSpec.BooleanValue COPY_TO_CLIPBOARD = BUILDER.comment("Copy screenshots to clipboard").define("copyToClipboard", true);

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean modEnabled = true;
    public static boolean copyToClipboard = true;
    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        modEnabled = MOD_ENABLED.get();
        copyToClipboard = COPY_TO_CLIPBOARD.get();
    }
}
