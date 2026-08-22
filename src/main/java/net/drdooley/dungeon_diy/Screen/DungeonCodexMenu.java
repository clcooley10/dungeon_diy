package net.drdooley.dungeon_diy.Screen;

import net.drdooley.dungeon_diy.Dungeon.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DungeonCodexMenu extends AbstractContainerMenu {
    private final UUID dungeonId;
    private final List<DungeonNode> nodes;
    private final List<ReplacementPrefab> replacementPrefabs;
    private int selectedNodeIndex = 0;
    private int selectedReplacementIndex = 0;
    private int selectedReplacementPrefabIndex = 0;

    public CodexPageEnum activePage = CodexPageEnum.NODE_VIEW_EDIT;

    // Client
    public DungeonCodexMenu(int containerId, Inventory inv, FriendlyByteBuf buf) {
        super(DDIYMenus.DUNGEON_CODEX_MENU.get(), containerId);
        this.dungeonId = buf.readUUID();

        int nodeCount = buf.readInt();
        this.nodes = new ArrayList<>();
        for (int i = 0; i < nodeCount; i++) {
            this.nodes.add(DungeonNode.readNetworkData(buf));
        }
        int prefabCount = buf.readInt();
        this.replacementPrefabs = new ArrayList<>();
        for (int i = 0; i < prefabCount; i++) {
            this.replacementPrefabs.add(ReplacementPrefab.readNetworkData(buf));
        }
    }

    // Server
    public DungeonCodexMenu(int containerId, Inventory inv, UUID dungeonId) {
        super(DDIYMenus.DUNGEON_CODEX_MENU.get(), containerId);

        this.dungeonId = dungeonId;
        DungeonInstance instance = DungeonManager.getDungeon((ServerLevel) inv.player.level(), dungeonId);

        this.nodes = new ArrayList<>(instance.getNodes().values());
        this.replacementPrefabs = new ArrayList<>(instance.getReplPrefabs());
    }

    public UUID getDungeonId() { return this.dungeonId; }

    public CodexPageEnum getActivePage() { return this.activePage; }

    public void setActivePage(CodexPageEnum activePage) { this.activePage = activePage; }

    public List<DungeonNode> getNodes() { return nodes; }

    public DungeonNode getNode(int index) { return nodes.get(index); }

    public int getNodeCount() { return nodes.size(); }

    public void setSelectedNodeIndex(int index) {
        this.selectedNodeIndex = index;
        // When the node changes, the replacement index should reset
        this.selectedReplacementIndex = 0;
    }
    public void setSelectedReplacementPrefabIndex(int index) {
        this.selectedReplacementPrefabIndex = index;
        // When the prefab changes, the replacement index should reset
        this.selectedReplacementIndex = 0;
    }

    public int getSelectedNodeIndex() { return selectedNodeIndex; }

    public DungeonNode getSelectedNode() { return nodes.get(selectedNodeIndex); }

    public void setSelectedReplacementIndex(int index) { this.selectedReplacementIndex = index; }

    public int getSelectedReplacementIndex() { return selectedReplacementIndex; }

    public int getSelectedReplacementPrefabIndex() { return selectedReplacementPrefabIndex; }


    public List<ReplacementPrefab> getReplacementPrefabs() { return replacementPrefabs; }

    public ReplacementPrefab getReplacementPrefab(int index) { return replacementPrefabs.get(index); }

    public ReplacementEntry getSelectedReplacement() {
        if (selectedNodeIndex < 0 || selectedNodeIndex >= nodes.size()) {
            return null;
        }
        DungeonNode node = nodes.get(selectedNodeIndex);
        if (selectedReplacementIndex < 0 || selectedReplacementIndex >= node.getReplacements().size()) {
            return null;
        }
        return node.getReplacements().get(selectedReplacementIndex);
    }

    public ReplacementPrefab getSelectedReplacementPrefab() {
        if (selectedReplacementPrefabIndex < 0 || selectedReplacementPrefabIndex >= replacementPrefabs.size()) {
            return null;
        }
        return replacementPrefabs.get(selectedReplacementPrefabIndex);
    }

    public void updateNodeReplacements(BlockPos nodePos, List<ReplacementEntry> replacements) {
        DungeonNode node = nodes.stream()
          .filter(n -> n.getPos().equals(nodePos))
          .findFirst()
          .orElse(null);
        if (node == null) {
            return;
        }
        List<ReplacementEntry> copiedReplacements = new ArrayList<>();
        for (ReplacementEntry entry : replacements) {
            copiedReplacements.add(new ReplacementEntry(entry));
        }
        node.setReplacements(copiedReplacements);
    }

    public void updateReplacementWeight(BlockPos nodePos, int replacementIndex, int weight) {
        DungeonNode node = nodes.stream()
          .filter(n -> n.getPos().equals(nodePos))
          .findFirst()
          .orElse(null);
        if (node == null) {
            return;
        }
        List<ReplacementEntry> replacements = node.getReplacements();

        if (replacementIndex < 0 || replacementIndex >= replacements.size()) {
            return;
        }
        replacements.get(replacementIndex).setWeight(weight);
    }

    public void updateReplacementPrefabs(List<ReplacementPrefab> prefabs) {
        this.replacementPrefabs.clear();
        this.replacementPrefabs.addAll(prefabs);
        if (replacementPrefabs.isEmpty()) {
            selectedReplacementPrefabIndex = 0;
        } else {
            selectedReplacementPrefabIndex = Math.min(selectedReplacementPrefabIndex, replacementPrefabs.size() - 1);
        }
    }

    // This may change based on the page context the menu is opened in
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.isAlive();
    }
}
