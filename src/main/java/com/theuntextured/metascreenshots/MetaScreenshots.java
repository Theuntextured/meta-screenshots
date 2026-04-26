package com.theuntextured.metascreenshots;

import com.mojang.logging.LogUtils;
import com.theuntextured.metascreenshots.containers.ScreenshotContainer;
import com.theuntextured.metascreenshots.network.ModMessages;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(MetaScreenshots.MOD_ID)
public class MetaScreenshots {
    public static final String MOD_ID = "metascreenshots";

    public MetaScreenshots(IEventBus modEventBus, ModContainer modContainer) {
        LogUtils.getLogger().info("[MetaScreenshots] Loading Meta Screenshots");

        // We exclude macOS, as AWT + GLFW on Mac causes an instant thread deadlock.
        if (!System.getProperty("os.name").toLowerCase().contains("mac")) {
            System.setProperty("java.awt.headless", "false");
        }

        modEventBus.addListener(this::commonSetup);

        modContainer.registerConfig(ModConfig.Type.CLIENT, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(ScreenshotContainer::Initialize);
    }
}
