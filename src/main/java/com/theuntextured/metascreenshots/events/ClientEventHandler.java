package com.theuntextured.metascreenshots.events;

import com.theuntextured.metascreenshots.MetaScreenshots;
import com.theuntextured.metascreenshots.containers.ScreenshotContainer;
import com.theuntextured.metascreenshots.util.WorldIdData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

@EventBusSubscriber(modid = MetaScreenshots.MOD_ID, value = Dist.CLIENT)
public class ClientEventHandler {

    @SubscribeEvent
    public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        WorldIdData.currentWorldUUID = null;
        ScreenshotContainer.worldScreenshots.clear();
    }
}