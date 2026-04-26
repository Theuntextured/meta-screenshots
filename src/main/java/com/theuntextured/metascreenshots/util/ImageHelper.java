package com.theuntextured.metascreenshots.util;

import com.mojang.logging.LogUtils;
import com.theuntextured.metascreenshots.Config;

import javax.imageio.*;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Iterator;

public class ImageHelper {
    public static void writeImageWithMetadata(com.mojang.blaze3d.platform.NativeImage nativeImage, File targetFile, String jsonPayload) {
        try (nativeImage) {
            int width = nativeImage.getWidth();
            int height = nativeImage.getHeight();
            BufferedImage fullImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

            // Transcribe the raw off-heap pixels directly into the BufferedImage
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int rgba = nativeImage.getPixelRGBA(x, y);
                    // NativeImage returns ABGR, we must shift the bits to ARGB for Java
                    int a = (rgba >> 24) & 0xFF;
                    int b = (rgba >> 16) & 0xFF;
                    int g = (rgba >> 8) & 0xFF;
                    int r = rgba & 0xFF;
                    int argb = (a << 24) | (r << 16) | (g << 8) | b;
                    fullImage.setRGB(x, y, argb);
                }
            }

            // --- CLIPBOARD INJECTION START ---
            if (Config.copyToClipboard) {
                try {
                    java.awt.datatransfer.Clipboard clipboard = java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(new ImageTransferable(fullImage), null);
                } catch (Exception e) {
                    LogUtils.getLogger().error("Failed to copy screenshot to clipboard", e);
                }
            }
            // --- CLIPBOARD INJECTION END ---

            savePngWithMetadata(fullImage, targetFile, jsonPayload);

            File thumbDir = new File(targetFile.getParentFile(), "thumbnails");
            if (!thumbDir.exists()) {
                thumbDir.mkdirs();
            }

            BufferedImage thumbImage = createThumbnail(fullImage, Config.thumbnailHeight);
            File thumbFile = new File(thumbDir, targetFile.getName());
            savePngWithMetadata(thumbImage, thumbFile, jsonPayload);

        } catch (Exception e) {
            LogUtils.getLogger().error("Failed to process screenshot", e);
        }
    }

    private static BufferedImage createThumbnail(BufferedImage original, int targetHeight) {
        float ratio = (float) original.getWidth() / original.getHeight();
        int targetWidth = Math.round(targetHeight * ratio);

        java.awt.Image tmp = original.getScaledInstance(targetWidth, targetHeight, java.awt.Image.SCALE_SMOOTH);
        BufferedImage resized = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g2d = resized.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        return resized;
    }

    private static void savePngWithMetadata(BufferedImage image, File file, String jsonPayload) throws Exception {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("png");
        if (!writers.hasNext()) throw new IllegalStateException("No PNG ImageWriter found");

        ImageWriter writer = writers.next();
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(file)) {
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

            LogUtils.getLogger().info("Successfully wrote: " + file.getName());
        } finally {
            writer.dispose();
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

    public static String readMetadataFromImage(File targetFile) {
        try (javax.imageio.stream.ImageInputStream iis = ImageIO.createImageInputStream(targetFile)) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            if (!readers.hasNext()) return null;

            ImageReader reader = readers.next();
            reader.setInput(iis);

            IIOMetadata metadata = reader.getImageMetadata(0);
            String nativeFormat = metadata.getNativeMetadataFormatName();
            IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(nativeFormat);

            for (int i = 0; i < root.getLength(); i++) {
                org.w3c.dom.Node node = root.item(i);
                if (node.getNodeName().equals("tEXt")) {
                    for (int j = 0; j < node.getChildNodes().getLength(); j++) {
                        org.w3c.dom.Node textEntry = node.getChildNodes().item(j);
                        if (textEntry.getNodeName().equals("tEXtEntry")) {
                            String keyword = ((IIOMetadataNode) textEntry).getAttribute("keyword");
                            if ("screenshot_meta".equals(keyword)) {
                                return ((IIOMetadataNode) textEntry).getAttribute("value");
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LogUtils.getLogger().error("Failed to read metadata from {}", targetFile.getName(), e);
        }
        return null;
    }

    public static void generateMissingThumbnailAsync(File originalFile, File thumbFile, String jsonPayload) {
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                // Ensure the subdirectory exists before writing
                File thumbDir = thumbFile.getParentFile();
                if (!thumbDir.exists()) {
                    thumbDir.mkdirs();
                }

                // Read the massive original off the disk
                BufferedImage fullImage = ImageIO.read(originalFile);
                if (fullImage == null) return;

                // Generate and seal the new thumbnail
                BufferedImage thumbImage = createThumbnail(fullImage, Config.thumbnailHeight);
                savePngWithMetadata(thumbImage, thumbFile, jsonPayload);

                LogUtils.getLogger().info("Retroactively generated missing thumbnail for: " + originalFile.getName());
            } catch (Exception e) {
                LogUtils.getLogger().error("Failed to retroactively generate thumbnail for {}", originalFile.getName(), e);
            }
        });
    }
}
