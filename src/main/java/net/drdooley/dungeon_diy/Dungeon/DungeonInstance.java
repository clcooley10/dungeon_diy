package net.drdooley.dungeon_diy.Dungeon;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.*;

public class DungeonInstance {
    private final UUID id;
    private int tick;
    private final Map<BlockPos, DungeonNode> nodes = new HashMap<>();
    private final VaultInventory vaultInventory;
    private final Runnable markDirty;

    public DungeonInstance(UUID id, Runnable markDirty) {
        this.id = id;
        this.markDirty = markDirty;
        this.vaultInventory = new VaultInventory(27, markDirty);
    }
    public UUID getId() {
        return id;
    }

    public VaultInventory getVaultInventory() { return vaultInventory; }

    public void tick() {
        tick++;
        for (DungeonNode node : nodes.values()) {
            node.tick(this);
        }
    }
    public int getTick() {
        return tick;
    }

    public Map<BlockPos, DungeonNode> getNodes() {
        return nodes;
    }

    // Returns true on successful add, returns false on remove
    public boolean toggleNode(BlockPos pos) {
        if (nodes.containsKey(pos)) {
            nodes.remove(pos);
            markDirty.run();
            return false;
        }
        nodes.put(pos, new DungeonNode(pos));
        markDirty.run();
        return true;
    }

    public CompoundTag save(HolderLookup.Provider registries)  {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("Id", id);
        tag.put("VaultInventory", vaultInventory.serializeNBT(registries));

        ListTag nodeList = new ListTag();
        for (DungeonNode node : nodes.values()) {
            nodeList.add(node.save());
        }
        tag.put("Nodes", nodeList);

        return tag;
    }

    public static DungeonInstance load(CompoundTag tag, HolderLookup.Provider registries, Runnable stackHandlerRunnable) {
        UUID id = tag.getUUID("Id");
        DungeonInstance instance = new DungeonInstance(id, stackHandlerRunnable);
        instance.vaultInventory.deserializeNBT(registries, tag.getCompound("VaultInventory"));
        ListTag nodeList = tag.getList("Nodes", Tag.TAG_COMPOUND);
        for (Tag nodeTag : nodeList) {
            DungeonNode node = DungeonNode.load((CompoundTag) nodeTag);
            instance.nodes.put(node.getPos(), node);
        }
        return instance;
    }

    private void markDirty() {
        markDirty.run();
    }
}
