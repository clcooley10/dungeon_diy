package net.drdooley.dungeon_diy.Dungeon;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

public class DungeonInstance {
    private final UUID id;
    private int tick;
    private final Runnable markDirty;
    private final VaultInventory vaultInventory;
    private final Map<BlockPos, DungeonNode> nodes = new HashMap<>();
    private final List<ReplacementPrefab> replacementPrefabs = new ArrayList<>();

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

    public boolean addNode(BlockPos pos, BlockState state) {
        if (nodes.containsKey(pos)) {
            return false;
        }
        DungeonNode node = new DungeonNode(pos);
        node.addReplacement(new ReplacementEntry(state, 1));
        nodes.put(pos, node);
        markDirty.run();
        return true;
    }

    public boolean removeNode(BlockPos pos) {
        DungeonNode removed = nodes.remove(pos);
        markDirty.run();
        return removed != null;
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
        ListTag replacementList = new ListTag();
        for (ReplacementPrefab replacementPrefab : replacementPrefabs) {
            replacementList.add(replacementPrefab.save());
        }
        tag.put("ReplacementPrefabs", replacementList);

        return tag;
    }

    public static DungeonInstance load(CompoundTag tag, HolderLookup.Provider registries, Runnable stackHandlerRunnable) {
        UUID id = tag.getUUID("Id");
        DungeonInstance instance = new DungeonInstance(id, stackHandlerRunnable);
        instance.vaultInventory.deserializeNBT(registries, tag.getCompound("VaultInventory"));
        ListTag nodeList = tag.getList("Nodes", Tag.TAG_COMPOUND);
        for (Tag nodeTag : nodeList) {
            DungeonNode node = DungeonNode.load((CompoundTag) nodeTag, registries);
            instance.nodes.put(node.getPos(), node);
        }
        ListTag replacementList = tag.getList("ReplacementPrefabs", Tag.TAG_COMPOUND);
        for (Tag replTag : replacementList) {
            ReplacementPrefab prefab =  ReplacementPrefab.load((CompoundTag) replTag, registries);
            instance.replacementPrefabs.add(prefab);
        }
        return instance;
    }

    private void markDirty() {
        markDirty.run();
    }
}
