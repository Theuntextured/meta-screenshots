package com.theuntextured.meta_screenshots.events;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import com.theuntextured.meta_screenshots.Config;
import com.theuntextured.meta_screenshots.MetaScreenshots;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenshotEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(modid = MetaScreenshots.MOD_ID, bus = Bus.FORGE, value = Dist.CLIENT)
public class ScreenshotEventHandler {

    @SubscribeEvent
    public static void onScreenshot(ScreenshotEvent event) {
        if (!Config.modEnabled) return;

        NativeImage nativeImage = event.getImage();
        File targetFile = event.getScreenshotFile();

        String jsonPayload = com.theuntextured.meta_screenshots.util.MetaDataHelper.getMetadataJson().toString();

        byte[] rawPngBytes;
        try {
            rawPngBytes = nativeImage.asByteArray();
        } catch (Exception e) {
            LogUtils.getLogger().error("Failed to extract raw screenshot bytes.", e);
            return;
        }

        event.setCanceled(true);

        Component fileLink = Component.literal(targetFile.getName())
                .withStyle(ChatFormatting.UNDERLINE)
                .withStyle(style -> style.withClickEvent(
                        new ClickEvent(ClickEvent.Action.OPEN_FILE, targetFile.getAbsolutePath())
                ));

        Component successMessage = Component.literal("Saved mapped screenshot: ").append(fileLink);
        event.setResultMessage(successMessage);

        CompletableFuture.runAsync(() -> writeImageWithMetadata(rawPngBytes, targetFile, jsonPayload));
    }

    private static void writeImageWithMetadata(byte[] imageData, File targetFile, String jsonPayload) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(imageData)) {
            BufferedImage image = ImageIO.read(bis);

            // --- CLIPBOARD INJECTION START ---
            if (Config.copyToClipboard) {
                try {
                    java.awt.datatransfer.Clipboard clipboard = java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(new com.theuntextured.meta_screenshots.util.ImageTransferable(image), null);
                } catch (Exception e) {
                    LogUtils.getLogger().error("Failed to copy screenshot to clipboard", e);
                }
            }
            // --- CLIPBOARD INJECTION END ---

            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("png");
            if (!writers.hasNext()) {
                LogUtils.getLogger().error("No PNG ImageWriter found!");
                return;
            }
            ImageWriter writer = writers.next();

            try (ImageOutputStream ios = ImageIO.createImageOutputStream(targetFile)) {
                writer.setOutput(ios);
                ImageWriteParam writeParam = writer.getDefaultWriteParam();
                IIOMetadata metadata = writer.getDefaultImageMetadata(new ImageTypeSpecifier(image), writeParam);

                String nativeFormat = metadata.getNativeMetadataFormatName();
                IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(nativeFormat);

                IIOMetadataNode textNode = getOrCreateChildNode(root, "tEXt");

                IIOMetadataNode textEntry = new IIOMetadataNode("tEXtEntry");
                textEntry.setAttribute("keyword", "screenshot_meta");
                textEntry.setAttribute("value", jsonPayload);
                textNode.appendChild(textEntry);

                metadata.mergeTree(nativeFormat, root);
                writer.write(null, new IIOImage(image, null, metadata), writeParam);

                LogUtils.getLogger().info("Successfully injected telemetry into: " + targetFile.getName());
            } finally {
                writer.dispose();
            }
        } catch (Exception e) {
            LogUtils.getLogger().error("Failed to write custom screenshot with metadata", e);
        }
    }

    // brute-force helper to navigate Java's ancient XML nodes
    private static IIOMetadataNode getOrCreateChildNode(IIOMetadataNode parentNode, String nodeName) {
        for (int i = 0; i < parentNode.getLength(); i++) {
            if (parentNode.item(i).getNodeName().equals(nodeName)) {
                return (IIOMetadataNode) parentNode.item(i);
            }
        }
        IIOMetadataNode node = new IIOMetadataNode(nodeName);
        parentNode.appendChild(node);
        return node;
    }
}