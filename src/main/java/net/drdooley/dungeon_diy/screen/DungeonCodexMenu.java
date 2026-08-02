package net.drdooley.dungeon_diy.screen;

import net.drdooley.dungeon_diy.Dungeon.DungeonInstance;
import net.drdooley.dungeon_diy.Dungeon.DungeonManager;
import net.drdooley.dungeon_diy.Dungeon.DungeonNode;
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

    public String activePage = "menu_base";
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




    public List<DungeonNode> getNodes() {
        return nodes;
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
