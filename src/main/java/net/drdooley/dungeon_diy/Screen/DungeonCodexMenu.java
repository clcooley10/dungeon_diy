package net.drdooley.dungeon_diy.Screen;

import net.drdooley.dungeon_diy.Dungeon.DungeonInstance;
import net.drdooley.dungeon_diy.Dungeon.DungeonManager;
import net.drdooley.dungeon_diy.Dungeon.DungeonNode;
import net.drdooley.dungeon_diy.Dungeon.ReplacementEntry;
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
    private int selectedNodeIndex = 0;
    private int selectedReplacementIndex = 0;

    public String activePage = "temp_codex_menu_base";
    public DungeonCodexMenu(int containerId, Inventory inv, FriendlyByteBuf buf) {
        super(DDIYMenus.DUNGEON_CODEX_MENU.get(), containerId);
        this.dungeonId = buf.readUUID();
        int nodeCount = buf.readInt();
        this.nodes = new ArrayList<>();
        for (int i = 0; i < nodeCount; i++) {
            this.nodes.add(DungeonNode.readNetworkData(buf));
        }
    }

    public DungeonCodexMenu(int containerId, Inventory inv, UUID dungeonId) {
        super(DDIYMenus.DUNGEON_CODEX_MENU.get(), containerId);

        this.dungeonId = dungeonId;
        DungeonInstance instance = DungeonManager.getDungeon((ServerLevel) inv.player.level(), dungeonId);

        this.nodes = new ArrayList<>(instance.getNodes().values());

        // Load DungeonInstance here if needed
    }

    public UUID getDungeonId() { return this.dungeonId; }
    public List<DungeonNode> getNodes() { return nodes; }
    public DungeonNode getNode(int index) { return nodes.get(index); }
    public int getNodeCount() { return nodes.size();}
    public void setSelectedNodeIndex(int index) {
        this.selectedNodeIndex = index;
        // When the node changes, the replacement index should reset
        this.selectedReplacementIndex = 0;
    }
    public int getSelectedNodeIndex() { return selectedNodeIndex; }
    public DungeonNode getSelectedNode() { return nodes.get(selectedNodeIndex); }
    public void setSelectedReplacementIndex(int index) { this.selectedReplacementIndex = index; }
    public int getSelectedReplacementIndex() { return selectedReplacementIndex; }
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
