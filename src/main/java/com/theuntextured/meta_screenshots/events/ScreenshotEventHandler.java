package com.theuntextured.meta_screenshots.events;

import com.theuntextured.meta_screenshots.Config;
import com.theuntextured.meta_screenshots.MetaScreenshots;
import com.theuntextured.meta_screenshots.containers.Screenshot;
import com.theuntextured.meta_screenshots.containers.ScreenshotContainer;
import com.theuntextured.meta_screenshots.util.MetaDataHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenshotEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

import java.io.File;

@Mod.EventBusSubscriber(modid = MetaScreenshots.MOD_ID, bus = Bus.FORGE, value = Dist.CLIENT)
public class ScreenshotEventHandler {

    @SubscribeEvent
    public static void onScreenshot(ScreenshotEvent event) {
        if (!Config.modEnabled) return;

        File targetFile = event.getScreenshotFile();

        Screenshot screenshot = new Screenshot(event);
        if(!screenshot.isValid()) return;
        ScreenshotContainer.allScreenshots.add(screenshot);
        if(MetaDataHelper.currentWorldUUID != null) ScreenshotContainer.worldScreenshots.add(screenshot);

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
