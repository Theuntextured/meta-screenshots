package com.theuntextured.meta_screenshots;

import com.mojang.logging.LogUtils;
import com.theuntextured.meta_screenshots.network.ModMessages;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(MetaScreenshots.MOD_ID)
public class MetaScreenshots {
    public static final String MOD_ID = "meta_screenshots";

    public MetaScreenshots(FMLJavaModLoadingContext context) {
        LogUtils.getLogger().info("[MetaScreenshots] Loading Meta Screenshots");

        // We exclude macOS, as AWT + GLFW on Mac causes an instant thread deadlock.
        if (!System.getProperty("os.name").toLowerCase().contains("mac")) {
            System.setProperty("java.awt.headless", "false");
        }

        IEventBus modBus = context.getModEventBus();
        modBus.addListener(this::commonSetup);

        context.registerConfig(ModConfig.Type.CLIENT, Config.SPEC);
    }

    public MetaScreenshots(IEventBus modEventBus) {
        this(FMLJavaModLoadingContext.get());
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Enqueue the network registration to ensure thread safety during boot
        event.enqueueWork(ModMessages::register);
    }
}
