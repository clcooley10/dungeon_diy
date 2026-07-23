package net.drdooley.dungeon_diy.Dungeon;

import net.drdooley.dungeon_diy.Dungeon.nodes.NodeContent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;

public class DungeonNode {
    private final BlockPos pos;
    private final List<NodeContent> possibleContent = new ArrayList<>();
    private NodeContent currentContent;

    public DungeonNode(BlockPos pos) {
        this.pos = pos;
    }
    public BlockPos getPos() {
        return pos;
    }

    public List<NodeContent> getPossibleContent() {
        return possibleContent;
    }
    public NodeContent getCurrentContent() {
        return currentContent;
    }

    public void generate(ServerLevel level, RandomSource random) {
        currentContent = rollContent(random);

        if (currentContent != null) {
            currentContent.place(level, pos);
        }
    }

    public void clear(ServerLevel level) {
        if (currentContent != null) {
            currentContent.remove(level, pos);
        }
        currentContent = null;
    }

    private NodeContent rollContent(RandomSource random) {
        int totalWeight = 0;
        for (NodeContent content : possibleContent) {
            totalWeight += content.getWeight();
        }

        if (totalWeight <= 0) return null;

        int roll = random.nextInt(totalWeight);
        int current = 0;
        for (NodeContent content : possibleContent) {
            current += content.getWeight();
            if (roll < current) {
                return content;
            }
        }
        return null;
    }


    public void onDungeonStart(DungeonInstance dungeon) {}
    public void onDungeonStop(DungeonInstance dungeon) {}
    public void tick(DungeonInstance dungeon) {}
}
