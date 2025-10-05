package net.drDooley.dungeon_diy.dungeon;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DungeonClientCache {
    private static final ConcurrentHashMap<UUID, DungeonConfig> DUNGEONS = new ConcurrentHashMap<>();

    public static void setDungeon(UUID id, DungeonConfig config) {
        DUNGEONS.put(id, config);
    }

    public static DungeonConfig getDungeon(UUID id) {
        return DUNGEONS.getOrDefault(id, null);
    }
}
