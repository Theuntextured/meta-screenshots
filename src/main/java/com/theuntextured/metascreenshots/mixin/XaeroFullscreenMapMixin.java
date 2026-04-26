package com.theuntextured.metascreenshots.mixin;

import com.mojang.logging.LogUtils;
import com.theuntextured.metascreenshots.compat.xaero.XaeroIntegrationWrapper;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.map.gui.GuiMap;

@Mixin(value = GuiMap.class, remap = false)
public abstract class XaeroFullscreenMapMixin {

    @Unique
    private boolean meta_screenshots$failedToRender = false;

    @Unique
    private boolean meta_screenshots$renderedThisFrame = false;

    @Inject(method = "render", at = @At("HEAD"), require = 0)
    private void meta_screenshots$resetLatch(GuiGraphics graphics, int mouseX, int mouseY, float pt, CallbackInfo ci) {
        this.meta_screenshots$renderedThisFrame = false;
    }

    // The GuiGraphics blit target signature remains identical in 1.21.1.
    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V"
            ),
            require = 0
    )
    private void meta_screenshots$xaeroMapFullscreenRender(GuiGraphics graphics, int mouseX, int mouseY, float pt, CallbackInfo ci) {
        if (this.meta_screenshots$renderedThisFrame || this.meta_screenshots$failedToRender) {
            return;
        }

        try {
            this.meta_screenshots$renderedThisFrame = true;
            XaeroIntegrationWrapper.onRender(graphics, (GuiMap) (Object) this, mouseX, mouseY, pt);

        } catch (Exception e) {
            LogUtils.getLogger().error("Meta Screenshots: Failure rendering map overlay.", e);
            this.meta_screenshots$failedToRender = true; // Permanently lock out to prevent log spam
        }
    }
}