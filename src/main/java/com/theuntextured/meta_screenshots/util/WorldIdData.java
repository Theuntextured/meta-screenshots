package com.theuntextured.meta_screenshots.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;
import java.util.UUID;

public class WorldIdData extends SavedData {
    private String worldId;
    static public String currentWorldUUID;

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

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putString("meta_world_id", this.worldId);
        return tag;
    }

    // The factory method Forge requires to instantiate this from NBT
    public static WorldIdData load(CompoundTag tag) {
        return new WorldIdData(tag);
    }

    public String getWorldId() {
        return worldId;
    }
}