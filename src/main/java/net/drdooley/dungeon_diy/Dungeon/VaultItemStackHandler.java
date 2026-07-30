package net.drdooley.dungeon_diy.Dungeon;

import net.neoforged.neoforge.items.ItemStackHandler;

public class VaultItemStackHandler extends ItemStackHandler {
    private final Runnable dirtyCallback;

    public VaultItemStackHandler(int slots, Runnable dirtyCallback) {
        super(slots);
        this.dirtyCallback = dirtyCallback;
    }

    @Override
    protected void onContentsChanged(int slot) {
        super.onContentsChanged(slot);
        dirtyCallback.run();
    }
}
