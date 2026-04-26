package com.theuntextured.metascreenshots.util;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class WorldIdData extends SavedData {
    private String worldId;
    static public String currentWorldUUID;

    public static SavedData.Factory<WorldIdData> factory() {
        return new SavedData.Factory<>(
                WorldIdData::new,
                WorldIdData::load,
                null // DataFixTypes is generally null for simple modded data
        );
    }

    public static WorldIdData load(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        return new WorldIdData(tag);
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        tag.putString("meta_world_id", this.worldId);
        return tag;
    }

    // Called when generating a brand-new world ID
    public WorldIdData() {
        this.worldId = UUID.randomUUID().toString();
        this.setDirty();
    }

    // Called by the engine when loading from the disk
    private WorldIdData(CompoundTag tag) {
        if (tag.contains("meta_world_id")) {
            this.worldId = tag.getString("meta_world_id");
        } else {
            this.worldId = UUID.randomUUID().toString();
            this.setDirty();
        }
    }

    // The factory method Forge requires to instantiate this from NBT
    public static WorldIdData load(CompoundTag tag) {
        return new WorldIdData(tag);
    }

    public String getWorldId() {
        return worldId;
    }
}