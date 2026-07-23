package net.drdooley.dungeon_diy.Dungeon;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DungeonInstance {
    private final UUID id;
    private int tick;
    private final List<DungeonNode> nodes = new ArrayList<>();

    public DungeonInstance(UUID id) {
        this.id = id;
    }
    public UUID getId() {
        return id;
    }

    public void tick() {
        tick++;
        for (DungeonNode node : nodes) {
            node.tick(this);
        }
    }
    public int getTick() {
        return tick;
    }

    public List<DungeonNode> getNodes() {
        return nodes;
    }
}
