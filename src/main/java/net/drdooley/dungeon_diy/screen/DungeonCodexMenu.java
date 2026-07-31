package net.drdooley.dungeon_diy.screen;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class DungeonCodexMenu extends AbstractContainerMenu {
    public String activePage = "menu_base";
    public DungeonCodexMenu(int containerId, Inventory inv, FriendlyByteBuf buf) {
        this(containerId, inv, buf.readUUID());
    }

    public DungeonCodexMenu(int containerId, Inventory inv, UUID dungeonId) {
        super(DDIYMenus.DUNGEON_CODEX_MENU.get(), containerId);

        // Load DungeonInstance here if needed
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
