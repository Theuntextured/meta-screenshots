package com.theuntextured.metascreenshots;

import com.mojang.logging.LogUtils;
import com.theuntextured.metascreenshots.containers.ScreenshotContainer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(MetaScreenshots.MOD_ID)
public class MetaScreenshots {
    public static final String MOD_ID = "metascreenshots";

    public MetaScreenshots(IEventBus modEventBus, ModContainer modContainer) {
        LogUtils.getLogger().info("[MetaScreenshots] Hello from Meta Screenshots!");

        // AWT + GLFW on Mac causes an instant thread deadlock.
        if (!System.getProperty("os.name").toLowerCase().contains("mac")) {
            System.setProperty("java.awt.headless", "false");
        }

        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener(this::clientSetup);
            modContainer.registerConfig(ModConfig.Type.CLIENT, Config.SPEC);
        }

    }

    static public void setModEnabled(boolean enabled) {
        ScreenshotContainer.setModEnabled(enabled);
    }

    private void clientSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(ScreenshotContainer::init);
    }
}
