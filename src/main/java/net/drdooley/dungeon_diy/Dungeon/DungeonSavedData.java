package net.drdooley.dungeon_diy.Dungeon;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DungeonSavedData extends SavedData {
    private static final String DATA_NAME = "dungeons";
    private final Map<UUID, DungeonInstance> dungeons = new HashMap<>();
    private static final SavedData.Factory<DungeonSavedData> FACTORY = new SavedData.Factory<>(DungeonSavedData::new, DungeonSavedData::load);

    public DungeonSavedData() {
    }

    public static DungeonSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag dungeonList = new ListTag();
        for (DungeonInstance dungeon : dungeons.values()) {
            dungeonList.add(dungeon.save(registries));
        }
        tag.put("Dungeons", dungeonList);
        return tag;
    }

    public static DungeonSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        DungeonSavedData data = new DungeonSavedData();
        ListTag dungeonList = tag.getList("Dungeons", Tag.TAG_COMPOUND);
        for (Tag element : dungeonList) {
            CompoundTag dungeonTag = (CompoundTag) element;
            DungeonInstance dungeon = DungeonInstance.load(dungeonTag, registries, data::setDirty);
            data.dungeons.put(dungeon.getId(), dungeon);
        }
        return data;
    }

    public DungeonInstance createDungeon() {
        UUID id = UUID.randomUUID();
        DungeonInstance dungeon = new DungeonInstance(id, this::setDirty);
        dungeons.put(id, dungeon);
        setDirty();
        return dungeon;
    }

    public DungeonInstance getDungeon(UUID id) {
        return dungeons.get(id);
    }

    public Collection<DungeonInstance> getDungeons() {
        return dungeons.values();
    }

    public void removeDungeon(UUID id) {
        if (dungeons.remove(id) != null) {
            setDirty();
        }
    }

    public void clear() {
        dungeons.clear();
        setDirty();
    }
}