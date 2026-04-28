package com.theuntextured.metascreenshots.mixin;

import com.mojang.logging.LogUtils;
import com.theuntextured.metascreenshots.Config;
import com.theuntextured.metascreenshots.MetaScreenshots;
import com.theuntextured.metascreenshots.compat.xaero.GuiTexturedButtonDualState;
import com.theuntextured.metascreenshots.compat.xaero.XaeroIntegrationWrapper;
import com.theuntextured.metascreenshots.gui.MapOverlayManager;
import com.theuntextured.metascreenshots.util.ModTextures;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaero.lib.client.gui.widget.Tooltip;
import xaero.map.WorldMap;
import xaero.map.gui.GuiMap;
import xaero.map.gui.GuiTexturedButton;

@Mixin(value = GuiMap.class, remap = false)
public abstract class XaeroFullscreenMapMixin {

    @Unique
    private boolean meta_screenshots$failedToRender = false;
    @Unique
    private boolean meta_screenshots$renderedThisFrame = false;

    @Unique
    private GuiMap self() {
        return (GuiMap)(Object)this;
    }

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
            XaeroIntegrationWrapper.onRender(graphics, self(), mouseX, mouseY, pt);

        } catch (Exception e) {
            LogUtils.getLogger().error("Meta Screenshots: Failure rendering map overlay.", e);
            this.meta_screenshots$failedToRender = true; // Permanently lock out to prevent log spam
        }
    }
    @Inject(method = "mouseClicked(DDI)Z", at = @At("HEAD"), cancellable = true, remap = true, require = 0)
    private void meta_screenshots$interceptClick(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (MapOverlayManager.onClick(button, self())) {
            cir.setReturnValue(true);
        }
    }

    @Inject(
            method = "init()V",
            at = @At("RETURN"),
            remap = true,
            require = 0
    )
    private void meta_screenshots$init(CallbackInfo ci) {
        Tooltip toggleTooltipEnabled = new Tooltip(Component.literal("Disable Meta Screenshots Mod"));
        Tooltip toggleTooltipDisabled = new Tooltip(Component.literal("Enable Meta Screenshots Mod"));
        GuiTexturedButtonDualState toggleModButton = new GuiTexturedButtonDualState(
                20, self().height - 20,
                20, 20,
                0, 0,
                16, 16,
                ModTextures.MOD_TOGGLE_DISABLED, ModTextures.MOD_TOGGLE,
                (Button button) -> MetaScreenshots.setModEnabled(!Config.modEnabled),
                () -> toggleTooltipDisabled,
                () -> toggleTooltipEnabled);
        toggleModButton.state = () -> Config.modEnabled;
        self().addButton(toggleModButton);

        Tooltip reloadTooltip = new Tooltip(Component.literal("Enable Meta Screenshots Mod"));
        GuiTexturedButtonDualState reloadModButton = new GuiTexturedButtonDualState(
                20, self().height - 40,
                20, 20,
                0, 0,
                16, 16,
                ModTextures.RELOAD, ModTextures.RELOAD,
                (Button button) -> MetaScreenshots.setModEnabled(true),
                () -> reloadTooltip,
                () -> reloadTooltip);
        reloadModButton.visibility = () -> Config.modEnabled;
        self().addButton(reloadModButton);
    }
}