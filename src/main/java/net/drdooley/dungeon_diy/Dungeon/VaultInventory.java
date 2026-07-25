package net.drdooley.dungeon_diy.Dungeon;

import net.neoforged.neoforge.items.ItemStackHandler;

public class VaultInventory {
    private final ItemStackHandler itemStackHandler;

    public VaultInventory() {
        itemStackHandler = new ItemStackHandler(27);
    }

    public ItemStackHandler getHandler() { return itemStackHandler; }
}
