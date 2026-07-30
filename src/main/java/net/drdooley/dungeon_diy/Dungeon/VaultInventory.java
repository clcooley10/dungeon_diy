package net.drdooley.dungeon_diy.Dungeon;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.items.ItemStackHandler;

public class VaultInventory {
    private final VaultItemStackHandler itemStackHandler;

    public VaultInventory(int slots, Runnable dirtyCallback) {
        itemStackHandler = new VaultItemStackHandler(slots, dirtyCallback);
    }

    public ItemStackHandler getHandler() { return itemStackHandler; }

    public Tag serializeNBT(HolderLookup.Provider registries) {
        return itemStackHandler.serializeNBT(registries);
    }

    public void deserializeNBT(HolderLookup.Provider registries, CompoundTag vaultInventory) {
        itemStackHandler.deserializeNBT(registries, vaultInventory);
    }
}
