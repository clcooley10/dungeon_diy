package net.drdooley.dungeon_diy.Dungeon;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DungeonInstance {
    private final UUID id;
    private int tick;
    private final List<DungeonNode> nodes = new ArrayList<>();
    private final VaultInventory vaultInventory;

    public DungeonInstance(UUID id, Runnable stackHandlerRunnable) {
        this.id = id;
        this.vaultInventory = new VaultInventory(27, stackHandlerRunnable);
    }
    public UUID getId() {
        return id;
    }

    public VaultInventory getVaultInventory() { return vaultInventory; }

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

    public CompoundTag save(HolderLookup.Provider registries)  {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("Id", id);
        tag.put("VaultInventory", vaultInventory.serializeNBT(registries));

        ListTag nodeList = new ListTag();
        for (DungeonNode node : nodes) {
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
            instance.nodes.add(
              DungeonNode.load((CompoundTag) nodeTag)
            );
        }
        return instance;
    }
}
