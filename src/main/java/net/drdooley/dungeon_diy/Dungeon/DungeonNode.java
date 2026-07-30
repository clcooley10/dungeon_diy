package net.drdooley.dungeon_diy.Dungeon;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

public class DungeonNode {

    private final BlockPos pos;

    public DungeonNode(BlockPos pos) {
        this.pos = pos;
    }

    public void tick(DungeonInstance dungeon) {}

    public BlockPos getPos() {
        return pos;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("X", pos.getX());
        tag.putInt("Y", pos.getY());
        tag.putInt("Z", pos.getZ());
        return tag;
    }

    public static DungeonNode load(CompoundTag tag) {
        BlockPos pos = new BlockPos(
          tag.getInt("X"),
          tag.getInt("Y"),
          tag.getInt("Z")
        );
        return new DungeonNode(pos);
    }
}
