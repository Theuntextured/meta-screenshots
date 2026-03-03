package com.theuntextured.meta_screenshots.events;

import com.theuntextured.meta_screenshots.MetaScreenshots;
import com.theuntextured.meta_screenshots.containers.ScreenshotContainer;
import com.theuntextured.meta_screenshots.util.WorldIdData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(modid = MetaScreenshots.MOD_ID, bus = Bus.FORGE, value = Dist.CLIENT)
public class ClientEventHandler {

    @SubscribeEvent
    public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        WorldIdData.currentWorldUUID = null;
        ScreenshotContainer.worldScreenshots.clear();
    }
}