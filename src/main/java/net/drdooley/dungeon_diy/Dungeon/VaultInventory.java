package net.drdooley.dungeon_diy.Dungeon;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

public class VaultInventory {
    private final VaultItemStackHandler itemStackHandler;

    public VaultInventory(int slots, Runnable dirtyCallback) {
        itemStackHandler = new VaultItemStackHandler(slots, dirtyCallback);
    }

    public ItemStackHandler getHandler() { return itemStackHandler; }

    public boolean consumeItem(ItemStack required) {
        for (int slot = 0; slot < itemStackHandler.getSlots(); slot++) {
            ItemStack stack = itemStackHandler.getStackInSlot(slot);
            if (!stack.isEmpty() && ItemStack.isSameItemSameComponents(stack, required)) {
                ItemStack extracted = itemStackHandler.extractItem(slot, 1, false);
                return !extracted.isEmpty();
            }
        }
        return false;
    }

    public Tag serializeNBT(HolderLookup.Provider registries) {
        return itemStackHandler.serializeNBT(registries);
    }

    public void deserializeNBT(HolderLookup.Provider registries, CompoundTag vaultInventory) {
        itemStackHandler.deserializeNBT(registries, vaultInventory);
    }
}
