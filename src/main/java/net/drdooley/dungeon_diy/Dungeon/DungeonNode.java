package net.drdooley.dungeon_diy.Dungeon;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;

public class DungeonNode {

    private final BlockPos pos;
    private List<ReplacementEntry> replacements = new ArrayList<>();

    public DungeonNode(BlockPos pos) {
        this.pos = pos;
    }

    public void tick(DungeonInstance dungeon) {}

    public BlockPos getPos() {
        return pos;
    }

    public List<ReplacementEntry> getReplacements() {
        return replacements;
    }

    public void addReplacement(ReplacementEntry replacement) {
        this.replacements.add(replacement);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putLong("Pos", pos.asLong());
        ListTag replacementList = new ListTag();
        for  (ReplacementEntry entry : replacements) {
            replacementList.add(entry.save());
        }
        tag.put("Replacements", replacementList);
        return tag;
    }

    public static DungeonNode load(CompoundTag tag, HolderLookup.Provider registries) {
        BlockPos pos = BlockPos.of(tag.getLong("Pos"));
        DungeonNode node = new DungeonNode(pos);
        ListTag list = tag.getList("Replacements", Tag.TAG_COMPOUND);
        for (Tag replacementTag : list) {
            node.replacements.add(ReplacementEntry.load((CompoundTag) replacementTag, registries));
        }
        return node;
    }

    public void writeNetworkData(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeInt(replacements.size());
        for (ReplacementEntry replacement : replacements) {
            replacement.writeNetworkData(buf);
        }
    }

    public static DungeonNode readNetworkData(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        DungeonNode node = new DungeonNode(pos);
        int replacementCount = buf.readInt();
        for (int i = 0; i < replacementCount; i++) {
            node.replacements.add(ReplacementEntry.readNetworkData(buf));
        }
        return node;
    }
}
