package net.drdooley.dungeon_diy.Dungeon.nodes;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public abstract class NodeContent {
    private final int weight;

    public NodeContent(int weight) {
        this.weight = weight;
    }

    public int getWeight() {
        return weight;
    }

    public abstract void place(ServerLevel level, BlockPos pos);
    public abstract void remove(ServerLevel level, BlockPos pos);
}
