package net.drdooley.dungeon_diy.Screen;

import net.drdooley.dungeon_diy.Dungeon.*;
import net.drdooley.dungeon_diy.DungeonDIY;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DungeonCodexMenu extends AbstractContainerMenu {

    public static final int PEDESTAL_SLOT_X = 44;
    public static final int PEDESTAL_SLOT_Y = 108;
    public static final int PEDESTAL_SLOT_COUNT = 5;

    private final UUID dungeonId;
    private final List<DungeonNode> nodes;
    private final List<ReplacementPrefab> replacementPrefabs;
    private final List<ItemStack> vaultStacks;
    private SimpleContainer acceptedPedestalItems = new SimpleContainer(5);
    private int selectedNodeIndex = 0;
    private int selectedReplacementIndex = 0;
    private int selectedReplacementPrefabIndex = 0;

    public CodexPageEnum activePage = CodexPageEnum.NODE_VIEW_EDIT;

    // Client
    public DungeonCodexMenu(int containerId, Inventory inv, RegistryFriendlyByteBuf buf) {
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

        int vaultCount = buf.readInt();
        this.vaultStacks = new ArrayList<>(vaultCount);
        for (int i = 0; i < vaultCount; i++) {
            this.vaultStacks.add(ItemStack.STREAM_CODEC.decode(buf));
        }

        int acceptedPedestalCount = buf.readInt();
        this.acceptedPedestalItems = new SimpleContainer(PEDESTAL_SLOT_COUNT);
        for  (int i = 0; i < acceptedPedestalCount; i++) {
            this.acceptedPedestalItems.addItem(ItemStack.OPTIONAL_STREAM_CODEC.decode(buf));
        }

        addPlayerInventory(inv);
        addAcceptedPedestalSlots();
    }

    // Server
    public DungeonCodexMenu(int containerId, Inventory inv, UUID dungeonId, List<ItemStack> vaultStacks) {
        super(DDIYMenus.DUNGEON_CODEX_MENU.get(), containerId);

        this.dungeonId = dungeonId;
        this.vaultStacks = vaultStacks;

        DungeonInstance instance = DungeonManager.getDungeon((ServerLevel) inv.player.level(), dungeonId);
        this.nodes = new ArrayList<>(instance.getNodes().values());
        this.replacementPrefabs = new ArrayList<>(instance.getReplPrefabs());
        this.acceptedPedestalItems = new SimpleContainer(PEDESTAL_SLOT_COUNT);
        NonNullList<ItemStack> acceptedStacks = NonNullList.copyOf(instance.getAcceptedPedestalStacks());
        for (ItemStack acceptedStack : acceptedStacks) {
            this.acceptedPedestalItems.addItem(acceptedStack);
        }

        addPlayerInventory(inv);
        addAcceptedPedestalSlots();
    }

    public UUID getDungeonId() { return this.dungeonId; }

    public CodexPageEnum getActivePage() { return this.activePage; }

    public void setActivePage(CodexPageEnum activePage) { this.activePage = activePage; }

    public List<DungeonNode> getNodes() { return nodes; }

    public DungeonNode getNode(int index) { return nodes.get(index); }

    public int getNodeCount() { return nodes.size(); }

    public List<ItemStack> getVaultStacks() { return vaultStacks; }

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

    public List<ItemStack> getAcceptedPedestalItems() { return acceptedPedestalItems.getItems(); }

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

    public void addReplacementEntryToNode(BlockPos nodePos, ReplacementEntry replacementEntry) {
        DungeonNode node = nodes.stream()
          .filter(n -> n.getPos().equals(nodePos))
          .findFirst()
          .orElse(null);
        if (node == null) {
            return;
        }
        node.addReplacement(replacementEntry);
    }

    public void removeReplacementEntryFromNode(BlockPos nodePos, int index) {
        DungeonNode node = nodes.stream()
          .filter(n -> n.getPos().equals(nodePos))
          .findFirst()
          .orElse(null);
        if (node == null) {
            return;
        }
        node.removeReplacement(index);
        selectedReplacementIndex = 0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (activePage == CodexPageEnum.PEDESTAL_EDIT) {
            Slot sourceSlot = this.slots.get(index);
            // Only allow shift-clicks originating from the player's inventory.
            if (!sourceSlot.hasItem() || index < 0 || index >= 36) {
                return ItemStack.EMPTY;
            }
            ItemStack sourceStack = sourceSlot.getItem();
            // Find the first available accepted-item slot.
            for (int i = 0; i < PEDESTAL_SLOT_COUNT; i++) {
                Slot acceptedSlot = this.slots.get(36 + i);
                if (!acceptedSlot.hasItem()) {
                    // Put a single copy into the accepted-item slot.
                    acceptedSlot.set(sourceStack.copyWithCount(1));
                    // Return empty to prevent this being called again
                    return ItemStack.EMPTY;
                }
            }
            // All five accepted-item slots are full.
            return ItemStack.EMPTY;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId >= 36 && slotId < 36 + PEDESTAL_SLOT_COUNT) {
            if (clickType == ClickType.PICKUP) {
                Slot slot = this.slots.get(slotId);
                ItemStack carriedStack = this.getCarried();
                if (carriedStack.isEmpty()) {
                    slot.set(ItemStack.EMPTY);
                } else {
                    slot.set(carriedStack.copyWithCount(1));
                }
                return;
            }
            // Do not allow dragging, illogical in this context
            if (clickType == ClickType.QUICK_CRAFT) {
                return;
            }
        }
        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public boolean stillValid(Player player) {
        return player.isAlive();
    }

    private void addPlayerInventory(Inventory playerInventory) {
        // Main inventory: slots 9-35
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 140 + row * 18) {
                    @Override
                    public boolean isActive() { return activePage == CodexPageEnum.PEDESTAL_EDIT; }
                });
            }
        }
        // Hotbar: slots 0-8
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 198) {
                @Override
                public boolean isActive() { return activePage == CodexPageEnum.PEDESTAL_EDIT; }
            });
        }
    }

    private void addAcceptedPedestalSlots() {
        for (int i = 0; i < PEDESTAL_SLOT_COUNT; i++) {
            addSlot(new Slot(acceptedPedestalItems, i, PEDESTAL_SLOT_X + i * 18, PEDESTAL_SLOT_Y) {
                @Override
                public boolean mayPlace(ItemStack stack) { return !stack.isEmpty(); }
                @Override
                public int getMaxStackSize() { return 1; }
                @Override
                public boolean mayPickup(Player player) { return false; }
                @Override
                public void set(ItemStack stack) { super.set(stack.copyWithCount(1)); }
                @Override
                public boolean isActive() { return activePage == CodexPageEnum.PEDESTAL_EDIT; }
            });
        }
    }
}
