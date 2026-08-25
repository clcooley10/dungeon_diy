package net.drdooley.dungeon_diy.Dungeon;

import net.drdooley.dungeon_diy.DungeonDIY;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import java.util.*;

import static net.drdooley.dungeon_diy.Screen.DungeonCodexMenu.PEDESTAL_SLOT_COUNT;

public class DungeonInstance {
    private final UUID id;
    private int tick;
    private final Runnable markDirty;
    private final VaultInventory vaultInventory;
    private final Map<BlockPos, DungeonNode> nodes = new HashMap<>();
    private final List<ReplacementPrefab> replacementPrefabs = new ArrayList<>();
    private NonNullList<ItemStack> acceptedPedestalStacks = NonNullList.withSize(PEDESTAL_SLOT_COUNT, ItemStack.EMPTY);

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

    public void removeExistingState(ServerLevel level) {
        for (DungeonNode node : nodes.values()) {
            BlockPos pos = node.getPos();
            Block block = level.getBlockState(pos).getBlock();
            if (block == Blocks.AIR) continue;

            ItemStack blockStack = new ItemStack(block.asItem());
            boolean dropBlock = false;
            if (!blockStack.isEmpty()) {
                ItemStack remainder = ItemHandlerHelper.insertItemStacked(vaultInventory.getHandler(), blockStack, false);
                if (!remainder.isEmpty()) {
                    dropBlock = true;
                    DungeonDIY.LOGGER.warn("Failed to return '{}' to the Dungeon's Vault, dropped at {}", block, pos.toShortString());
                }
            }
            level.destroyBlock(pos, dropBlock, null);
        }
    }

    public void generate(ServerLevel level) {
        removeExistingState(level);
        RandomSource random = level.getRandom();
        for (DungeonNode node : nodes.values()) {
            BlockPos pos = node.getPos();
            BlockState state = node.generateState(random, vaultInventory);
            level.setBlockAndUpdate(pos, state);
        }
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

    public ReplacementPrefab getReplPrefab(int index) {
        return replacementPrefabs.get(index);
    }

    public List<ReplacementPrefab> getReplPrefabs() {
        return replacementPrefabs;
    }

    public void addReplPrefab(String name, List<ReplacementEntry> replacements) {
        boolean exists = false;
        for (ReplacementPrefab prefab : replacementPrefabs) {
            if (prefab.name.equals(name)) {
                exists = true;
                prefab.entries = replacements;
            }
        }
        if (exists) return;
        ReplacementPrefab prefab = new ReplacementPrefab(UUID.randomUUID(), name, replacements);
        replacementPrefabs.add(prefab);
    }

    public void removeReplPrefab(String name) {
        replacementPrefabs.removeIf(prefab -> prefab.name.equals(name));
    }

    public NonNullList<ItemStack> getAcceptedPedestalStacks() { return acceptedPedestalStacks; }
    public void setAcceptedPedestalStacks(NonNullList<ItemStack> acceptedPedestalStacks) {
        this.acceptedPedestalStacks = acceptedPedestalStacks;
        markDirty();
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
        ContainerHelper.saveAllItems(tag, acceptedPedestalStacks, registries);

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
        ContainerHelper.loadAllItems(tag, instance.acceptedPedestalStacks, registries);
        return instance;
    }

    public void markDirty() {
        markDirty.run();
    }
}
