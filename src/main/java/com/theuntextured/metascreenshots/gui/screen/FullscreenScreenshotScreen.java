package com.theuntextured.metascreenshots.gui.screen;

import com.mojang.logging.LogUtils;
import com.theuntextured.metascreenshots.containers.Screenshot;
import com.theuntextured.metascreenshots.util.ImageTransferable;
import com.theuntextured.metascreenshots.util.ModTextures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.awt.datatransfer.Clipboard;
import java.awt.image.BufferedImage;

@OnlyIn(Dist.CLIENT)
public class FullscreenScreenshotScreen extends Screen {
    private final Screen parentScreen;
    private final Screenshot screenshot;

    EditBox renameBox;

    public static final double MAX_GUI_FILL = 0.8;
    public static final int PADDING = 4;

    public FullscreenScreenshotScreen(Screen parentScreen, Screenshot screenshot) {
        super(Component.literal("Screenshot Viewer: " + screenshot.targetFile.getName().replace(".png", "")));
        this.parentScreen = parentScreen;
        this.screenshot = screenshot;
    }

    @Override
    protected void init() {
        super.init();

        screenshot.getFullTexture();
        final double sf = getScreenshotScaleFactor();
        final int screenshotHeight = (int) (this.screenshot.height * sf);
        final int screenshotWidth = (int) (this.screenshot.width * sf);

        final int guiWidth = screenshotWidth + 2 * PADDING;

        final int imgY = (this.height - screenshotHeight) / 2;

        final int boxStartX = (this.width - guiWidth) / 2;
        final int boxStartY = imgY - PADDING * 2 - 16;
        final int boxEndX = boxStartX + guiWidth;
        final int boxEndY = imgY + screenshotHeight + PADDING * 2 + 16;

        this.addRenderableWidget(new IconButton(
                boxEndX - 16 - PADDING,
                boxStartY + PADDING,
                16,
                16,
                ModTextures.CROSS,
                this::closeRunnable,
                "Close window"
        ));

        int currentX = boxEndX - 16 - PADDING;

        this.addRenderableWidget(new IconButton(
                currentX,
                boxEndY - 16 - PADDING,
                16,16,
                ModTextures.TRASH,
                this::trashRunnable,
                "Move screenshot to trash"
        ));
        currentX -= PADDING + 16;

        this.addRenderableWidget(new IconButton(
                currentX,
                boxEndY - 16 - PADDING,
                16,16,
                ModTextures.OPEN_IN_EXPLORER,
                this::openInExplorerRunnable,
                "Show in folder"
        ));
        currentX -= PADDING + 16;

        this.addRenderableWidget(new IconButton(
                currentX,
                boxEndY - 16 - PADDING,
                16,16,
                ModTextures.CLIPBOARD,
                this::copyRunnable,
                "Copy screenshot to clipboard"
        ));
        currentX -= PADDING + 16;

        this.addRenderableWidget(new IconButton(
                currentX,
                boxEndY - 16 - PADDING,
                16,16,
                ModTextures.RENAME,
                this::renameRunnable,
                "Rename screenshot"
        ));

        int boxWidth = 200;
        currentX -= PADDING + 200;

        assert this.minecraft != null;
        this.renameBox = new EditBox(
                this.minecraft.font,
                currentX,
                boxEndY - 16 - PADDING,
                boxWidth,
                16,
                Component.literal("Rename Screenshot")
        );

        // Strip the ".png" extension so the user doesn't accidentally delete it
        this.renameBox.setValue(this.screenshot.targetFile.getName().replace(".png", ""));
        this.renameBox.setVisible(false); // Hidden by default

        this.addRenderableWidget(this.renameBox);
    }

    private double getScreenshotScaleFactor() {
        final int maxHeight = (int)(MAX_GUI_FILL * this.height) - 3 * PADDING - 16 * 2;
        final int maxWidth = (int)(MAX_GUI_FILL * this.width) - 2 * PADDING;

        final int originalWidth = this.screenshot.width;
        final int originalHeight = this.screenshot.height;

        if (originalWidth > maxWidth || originalHeight > maxHeight) {
            return Math.min((double) maxHeight / originalHeight, (double) maxWidth / originalWidth);
        }
        return 1;
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Draw the blurry world background
        this.renderBackground(graphics, mouseX, mouseY, partialTick);

        ResourceLocation texture = this.screenshot.getFullTexture();
        if (texture != null) {
            final double sf = getScreenshotScaleFactor();
            final int screenshotHeight = (int) (screenshot.height * sf);
            final int screenshotWidth = (int) (screenshot.width * sf);

            final int guiHeight = screenshotHeight + 2 * PADDING;
            final int guiWidth = screenshotWidth + 2 * PADDING;

            // Calculate absolute top-left coordinates for the image
            final int imgX = (this.width - screenshotWidth) / 2;
            final int imgY = (this.height - screenshotHeight) / 2;

            // Calculate absolute coordinates for the background box
            // You extended the top by 16 + INTERNAL_PADDING to make room for headers/buttons
            final int boxStartX = (this.width - guiWidth) / 2;
            final int boxStartY = imgY - PADDING * 2 - 16;
            final int boxEndX = boxStartX + guiWidth;
            final int boxEndY = imgY + screenshotHeight + PADDING * 2 + 16;

            // Draw the background panel
            graphics.fill(boxStartX, boxStartY, boxEndX, boxEndY, -939524096);

            // Draw the scaled image
            graphics.blit(
                    texture,
                    imgX, imgY,
                    screenshotWidth, screenshotHeight,
                    0, 0,
                    screenshot.width, screenshot.height,
                    screenshot.width, screenshot.height
            );
        }

        for (Renderable renderable : this.renderables) {
            renderable.render(graphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public void onClose() {
        if (screenshot != null) {
            // Note: Ensure freeTextures() or your specific cleanup method is called here
            screenshot.freeFullTexture();
        }
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parentScreen);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // If the rename box is active, intercept the keyboard inputs
        if (this.renameBox != null && this.renameBox.isVisible() && this.renameBox.isFocused()) {

            // If they press Enter (main keyboard or numpad)
            if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER || keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_KP_ENTER) {
                this.playClickSound();
                this.attemptRename();
                return true;
            }

            // If they press Escape, hide the box but DO NOT close the whole viewer
            if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE) {
                this.renameRunnable(); // Toggles it off
                return true;
            }
        }

        // Let the engine handle normal keys
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void attemptRename() {
        String newName = this.renameBox.getValue().trim();

        // Regex validation: Prevent illegal OS filename characters
        if (newName.isEmpty() || !newName.matches("^[^\\\\/:*?\"<>|]+$")) {
            LogUtils.getLogger().warn("Invalid filename entered.");
            return;
        }

        java.io.File currentTarget = this.screenshot.targetFile;
        java.io.File currentThumb = this.screenshot.thumbFile;

        java.io.File newTarget = new java.io.File(currentTarget.getParent(), newName + ".png");
        java.io.File newThumb = new java.io.File(currentThumb.getParent(), newName + ".png");

        // Prevent overwriting an existing screenshot
        if (newTarget.exists() || newThumb.exists()) {
            LogUtils.getLogger().warn("A screenshot with this name already exists.");
            return;
        }

        try {
            if (currentTarget.renameTo(newTarget)) {
                this.screenshot.targetFile = newTarget;
            }
            if (currentThumb.exists() && currentThumb.renameTo(newThumb)) {
                this.screenshot.thumbFile = newThumb;
            }

            LogUtils.getLogger().info("Successfully renamed screenshot to " + newName);

            // Hide the box upon success
            this.renameBox.setVisible(false);
            this.renameBox.setFocused(false);
            this.setFocused(null);

        } catch (Exception e) {
            LogUtils.getLogger().error("Failed to rename files on the hard drive.", e);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Let the widgets (like your EditBox and buttons) handle the click first
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        // If they clicked the empty background, close the viewer
        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT)
        {
            this.onClose();
            playClickSound();
        }
        return true;
    }

    private void playClickSound() {
        SimpleSoundInstance sound = SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F);
        Minecraft.getInstance().getSoundManager().play(sound);
    }
    private void closeRunnable() {
        playClickSound();
        this.onClose();
    }
    private void trashRunnable() {
        this.playClickSound();
        try {
            java.awt.Desktop desktop = java.awt.Desktop.getDesktop();

            if (desktop.isSupported(java.awt.Desktop.Action.MOVE_TO_TRASH)) {

                // Bin the main image and its thumbnail
                if (this.screenshot.targetFile.exists()) {
                    desktop.moveToTrash(this.screenshot.targetFile);
                }
                if (this.screenshot.thumbFile.exists()) {
                    this.screenshot.thumbFile.delete();
                }

                com.theuntextured.metascreenshots.containers.ScreenshotContainer.allScreenshots.remove(this.screenshot);
                com.theuntextured.metascreenshots.containers.ScreenshotContainer.worldScreenshots.remove(this.screenshot);

                this.onClose();

            } else {
                LogUtils.getLogger().warn("The operating system does not support moving files to the trash.");
            }
        } catch (Exception e) {
            LogUtils.getLogger().error("Failed to move screenshot to the trash.", e);
        }
    }
    private void openInExplorerRunnable() {
        this.playClickSound();

        if (this.screenshot.targetFile.exists()) {
            net.minecraft.Util.OS os = net.minecraft.Util.getPlatform();

            try {
                if (os == net.minecraft.Util.OS.WINDOWS) {
                    new ProcessBuilder("explorer.exe", "/select,", this.screenshot.targetFile.getAbsolutePath()).start();
                }
                else if (os == net.minecraft.Util.OS.OSX) {
                    new ProcessBuilder("open", "-R", this.screenshot.targetFile.getAbsolutePath()).start();
                }
                else {
                    // Linux / Universal Fallback
                    java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
                    if (desktop.isSupported(java.awt.Desktop.Action.OPEN)) {
                        desktop.open(this.screenshot.targetFile.getParentFile());
                    } else {
                        LogUtils.getLogger().warn("The operating system does not support opening file directories.");
                    }
                }
            } catch (Exception e) {
                LogUtils.getLogger().error("Failed to open file manager.", e);
            }
        }
    }
    private void renameRunnable() {
        this.playClickSound();
        if (this.renameBox != null) {
            boolean isVisible = !this.renameBox.isVisible();
            this.renameBox.setVisible(isVisible);

            if (isVisible) {
                // Focus the box so the user can immediately start typing
                this.renameBox.setFocused(true);
                this.setFocused(this.renameBox);
            } else {
                // Remove focus and reset the text if they cancelled
                this.renameBox.setFocused(false);
                this.setFocused(null);
                this.renameBox.setValue(this.screenshot.targetFile.getName().replace(".png", ""));
            }
        }
    }
    private void copyRunnable() {
        this.playClickSound();

        if (this.screenshot.targetFile != null && this.screenshot.targetFile.exists()) {
            try {
                BufferedImage image = javax.imageio.ImageIO.read(this.screenshot.targetFile);

                if (image != null) {
                    ImageTransferable transferable = new ImageTransferable(image);

                    Clipboard clipboard = java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(transferable, null);

                    LogUtils.getLogger().info("Screenshot successfully copied to the clipboard.");
                } else {
                    LogUtils.getLogger().warn("Failed to decode the screenshot for clipboard injection.");
                }
            } catch (Exception e) {
                LogUtils.getLogger().error("An error occurred whilst attempting to copy the image to the clipboard.", e);
            }
        }
    }
}
