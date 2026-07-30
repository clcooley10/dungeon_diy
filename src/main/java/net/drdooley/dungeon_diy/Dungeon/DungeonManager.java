package net.drdooley.dungeon_diy.Dungeon;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import java.util.Collection;
import java.util.UUID;

public class DungeonManager {

    public static DungeonInstance createDungeon(ServerLevel level) {
        DungeonSavedData data = DungeonSavedData.get(level);
        return data.createDungeon();
    }

    public static DungeonInstance getDungeon(ServerLevel level, UUID id) {
        DungeonSavedData data = DungeonSavedData.get(level);
        return data.getDungeon(id);
    }

    public static Collection<DungeonInstance> getDungeons(ServerLevel level) {
        DungeonSavedData data = DungeonSavedData.get(level);
        return data.getDungeons();
    }

    public static void tickDungeons(ServerLevel level) {
        for (DungeonInstance dungeon : getDungeons(level)) {
            dungeon.tick();
        }
    }

    public static void printDungeons(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        for (DungeonInstance dungeon : getDungeons(level)) {
            source.sendSystemMessage(Component.literal("Dungeon " + dungeon.getId() + " tick=" + dungeon.getTick()));
        }
    }
}