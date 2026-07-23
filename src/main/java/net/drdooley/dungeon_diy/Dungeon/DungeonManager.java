package net.drdooley.dungeon_diy.Dungeon;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DungeonManager {
    private static final Map<UUID, DungeonInstance> DUNGEONS = new HashMap<>();

    public static DungeonInstance createDungeon() {
        UUID id = UUID.randomUUID();
        DungeonInstance instance = new DungeonInstance(id);
        DUNGEONS.put(id, instance);
        return instance;
    }

    public static DungeonInstance getDungeon(UUID id) {
        return DUNGEONS.get(id);
    }

    public static Collection<DungeonInstance> getDungeons() {
        return DUNGEONS.values();
    }

    public static void tickDungeons() {
        for (DungeonInstance d : DUNGEONS.values()) {
            d.tick();
        }
    }

    public static void printDungeons(CommandSourceStack source) {
        for (DungeonInstance d : DUNGEONS.values()) {
            source.sendSystemMessage(Component.literal("Dungeon " + d.getId() + " tick=" + d.getTick()));
        }
    }
}
