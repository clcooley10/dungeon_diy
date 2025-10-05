package net.drDooley.dungeon_diy.dungeon;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// Based on McJTY and DimDungeons
public class DataManager extends SavedData {

    private final ConcurrentHashMap<UUID, DungeonConfig> dungeons = new ConcurrentHashMap<>();

    @Nonnull
    public static DataManager get(Level level) {
        if (level.isClientSide) { throw new RuntimeException("Accessed Level from client-side"); }

        DimensionDataStorage storage = ((ServerLevel)level).getDataStorage();
        return storage.computeIfAbsent(DataManager::new, DataManager::new, "datamanager");
    }

    public DataManager() { }

    public DataManager(CompoundTag tag) {
        ListTag listTag = tag.getList("Dungeons", ListTag.TAG_COMPOUND);
        for (Tag t : listTag) {
            CompoundTag dungeonTag = (CompoundTag) t;
            UUID uuid = dungeonTag.getUUID("DungeonId");
            DungeonConfig config = new DungeonConfig(uuid);
            dungeons.put(uuid, config);
        }
    }

    public UUID createDungeon(ServerLevel level) {
        UUID id = UUID.randomUUID();
        DungeonConfig config = new DungeonConfig(id);
        dungeons.put(id, config);
        setDirty();
        return id;
    }

    public DungeonConfig getDungeon(UUID id) {
        return dungeons.getOrDefault(id, null);
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag listTag = new ListTag();
        dungeons.forEach((uuid, config) -> {
            CompoundTag dungeonTag = config.save();
            dungeonTag.putUUID("DungeonId", uuid);
            listTag.add(dungeonTag);
        });

        tag.put("Dungeons", listTag);
        return tag;
    }
}
