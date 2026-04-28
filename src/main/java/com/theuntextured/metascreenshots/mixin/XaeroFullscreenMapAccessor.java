package com.theuntextured.metascreenshots.mixin;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import xaero.map.MapProcessor;
import xaero.map.gui.GuiMap;

@Mixin(value = GuiMap.class)
public interface XaeroFullscreenMapAccessor {

    @Accessor(value = "cameraX", remap = false)
    double meta_screenshots$getCameraX();

    @Accessor(value = "cameraZ", remap = false)
    double meta_screenshots$getCameraZ();

    @Accessor(value = "scale", remap = false)
    double meta_screenshots$getScale();

    @Accessor(value = "mapProcessor", remap = false)
    MapProcessor meta_screenshots$getMapProcessor();
}