package net.drDooley.dungeon_diy.screen;

import net.drDooley.dungeon_diy.block.ChestTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class LootChestEditMenu extends AbstractContainerMenu {
    Container container;
    final ChestTypes type;

    public LootChestEditMenu(int id, Inventory playerInv, FriendlyByteBuf buffer) {
        this(ChestTypes.LOOT_CHEST, id, playerInv, new SimpleContainer(27));
    }

    public static LootChestEditMenu createLootChestEditMenu(int containerId, Inventory playerInv, Container inv) {
        return new LootChestEditMenu(ChestTypes.LOOT_CHEST, containerId, playerInv, inv);
    }

    public LootChestEditMenu(ChestTypes chestType, int syncId, Inventory playerInv) {
        this(chestType, syncId, playerInv, new SimpleContainer(chestType.size()));
    }

    public LootChestEditMenu(ChestTypes chestType, int syncId, Inventory playerInventory, Container inventory) {
        super(DDIY_Menus.LOOT_CHEST_EDIT_MENU.get(), syncId);
        checkContainerSize(inventory, 27);
        this.type = chestType;
        this.container = inventory;
        inventory.startOpen(playerInventory.player);

        //addChestInventory(inventory, 0, 0);
        //addPlayerInventoryAndHotbar(playerInventory, 0, 0);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot2 = (Slot)this.slots.get(slot);
        if (slot2 != null && slot2.hasItem()) {
            ItemStack itemStack2 = slot2.getItem();
            itemStack = itemStack2.copy();
            if (slot < type.rows * type.columns) {
                if (!this.moveItemStackTo(itemStack2, type.rows * type.columns, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemStack2, 0, type.rows * type.columns, false)) {
                return ItemStack.EMPTY;
            }

            if (itemStack2.isEmpty()) {
                slot2.set(ItemStack.EMPTY);
            } else {
                slot2.setChanged();
            }
        }
        return itemStack;
    }

    @Override
    public boolean stillValid(Player player) { return this.container.stillValid(player); }

    public Container getContainer() {
        return this.container;
    }



    @Override
    public void removed(Player player){
        super.removed(player);
        this.container.stopOpen(player);
    }

    void addChestInventory(Container inventory, int xOffset, int yOffset){
        int slotCount = 0;
        for(int j = 0; j < type.rows; ++j) {
            for(int k = 0; k < type.columns; ++k) {
                this.addSlot(new Slot(inventory, slotCount++, (8 + k * 18) + xOffset , (106 + j * 18) + yOffset));
            }
        }
    }

    void addPlayerInventoryAndHotbar(Inventory playerInventory, int xOffset, int yOffset){
        addPlayerHotbar(playerInventory,8 + xOffset, (type.rows * 18) + 31 + yOffset);
        addPlayerInventory(playerInventory, 8  + xOffset, (type.rows * 18) + 31 + yOffset);
    }

    private void addPlayerInventory(Inventory playerInventory, int x, int y) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, x + l * 18, y + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory, int x, int y) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, x + i * 18, y + 58));
        }
    }
}