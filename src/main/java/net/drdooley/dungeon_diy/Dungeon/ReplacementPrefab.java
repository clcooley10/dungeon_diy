package net.drdooley.dungeon_diy.Dungeon;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ReplacementPrefab {
    UUID id;
    public String name;
    public List<ReplacementEntry> entries;

    ReplacementPrefab(UUID id, String name, List<ReplacementEntry> entries) {
        this.id = id;
        this.name = name;
        this.entries = entries;
    }

    public ItemStack heavyWeightReplacementStack() {
        int heaviestWeight = 0;
        ReplacementEntry heavyEntry = this.entries.getFirst();
        for (ReplacementEntry entry : this.entries) {
            if (entry.getWeight() > heaviestWeight) {
                heaviestWeight = entry.getWeight();
                heavyEntry = entry;
            }
        }
        return new ItemStack(heavyEntry.getState().getBlock().asItem());
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("Id", id);
        tag.putString("Name", name);
        ListTag replacementList = new ListTag();
        for  (ReplacementEntry entry : entries) {
            replacementList.add(entry.save());
        }
        tag.put("Replacements", replacementList);
        return tag;
    }

    public static ReplacementPrefab load(CompoundTag tag, HolderLookup.Provider registries) {
        UUID id = tag.getUUID("Id");
        String name = tag.getString("Name");
        ListTag entryList = tag.getList("Replacements", Tag.TAG_COMPOUND);
        List<ReplacementEntry> newEntries = new ArrayList<>();
        for (Tag entryTag : entryList) {
            ReplacementEntry e = ReplacementEntry.load((CompoundTag) entryTag, registries);
            newEntries.add(e);
        }
        return new ReplacementPrefab(id, name, newEntries);
    }

    public static ReplacementPrefab readNetworkData(FriendlyByteBuf buf) {
        UUID id = buf.readUUID();
        String name = buf.readUtf();
        int size = buf.readVarInt();
        List<ReplacementEntry> entries = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            entries.add(ReplacementEntry.readNetworkData(buf));
        }
        return new ReplacementPrefab(id, name, entries);
    }

    public void writeNetworkData(FriendlyByteBuf buf) {
        buf.writeUUID(id);
        buf.writeUtf(name);
        buf.writeVarInt(entries.size());
        for (ReplacementEntry entry : entries) {
            entry.writeNetworkData(buf);
        }
    }
}
