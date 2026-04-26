package com.theuntextured.metascreenshots.events;

import com.theuntextured.metascreenshots.Config;
import com.theuntextured.metascreenshots.MetaScreenshots;
import com.theuntextured.metascreenshots.containers.Screenshot;
import com.theuntextured.metascreenshots.containers.ScreenshotContainer;
import com.theuntextured.metascreenshots.util.WorldIdData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenshotEvent;

import java.io.File;

@EventBusSubscriber(modid = MetaScreenshots.MOD_ID, value = Dist.CLIENT)
public class ScreenshotEventHandler {

    @SubscribeEvent
    public static void onScreenshot(ScreenshotEvent event) {
        if (!Config.modEnabled) return;

        File targetFile = event.getScreenshotFile();

        Screenshot screenshot = new Screenshot(event);
        if(!screenshot.isValid()) return;
        ScreenshotContainer.allScreenshots.add(screenshot);
        if(WorldIdData.currentWorldUUID != null) ScreenshotContainer.worldScreenshots.add(screenshot);

        event.setCanceled(true);

        Component fileLink = Component.literal(targetFile.getName())
                .withStyle(ChatFormatting.UNDERLINE)
                .withStyle(style -> style.withClickEvent(
                        new ClickEvent(ClickEvent.Action.OPEN_FILE, targetFile.getAbsolutePath())
                ));

        Component successMessage = Component.literal("Saved mapped screenshot: ").append(fileLink);
        event.setResultMessage(successMessage);
    }


}
