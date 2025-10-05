package net.drDooley.dungeon_diy.dungeon;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import org.checkerframework.checker.units.qual.C;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DungeonConfig {
    private final UUID dungeonId;
    private final List<BlockPos> blocks = new ArrayList<>();

    public DungeonConfig(UUID id) {
        dungeonId = id;
    }

    public void addBlock(BlockPos pos) {
        blocks.add(pos);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        ListTag listTag = new ListTag();

        for (BlockPos pos : blocks) {
            listTag.add(NbtUtils.writeBlockPos(pos));
        }
        tag.put("Blocks", listTag);
        tag.putUUID("DungeonId", dungeonId);
        return tag;
    }

    public static DungeonConfig load(CompoundTag tag) {
        UUID dungeonId = tag.getUUID("DungeonId");
        DungeonConfig config = new DungeonConfig(dungeonId);
        ListTag listTag = tag.getList("Blocks", Tag.TAG_COMPOUND);
        for (Tag t : listTag) {
            config.blocks.add(NbtUtils.readBlockPos((CompoundTag) t));
        }
        return config;
    }


}
