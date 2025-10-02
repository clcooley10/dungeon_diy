package net.drDooley.dungeon_diy.screen;

import net.drDooley.dungeon_diy.DDIY;
import net.drDooley.dungeon_diy.block.entity.LootChestEntity;
import net.drDooley.dungeon_diy.networking.DDIY_Packets;
import net.drDooley.dungeon_diy.networking.InitLootChest_S2C;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LootChestEditMenu extends AbstractContainerMenu {
    public final LootChestEntity blockEntity;
    private final Level level;
    public List<ItemStack> lootItems;
    private final ContainerData data;

    private boolean showItemScreen = false;
    private boolean hasActive = false;

    // The inventories screen's display area
    private static final int COLS = 9;
    private static final int ROWS = 6;

    public LootChestEditMenu(int id, Inventory playerInv, FriendlyByteBuf buffer) {
        this(id, playerInv, playerInv.player.level.getBlockEntity(buffer.readBlockPos()), new SimpleContainerData(1010));
    }

    public LootChestEditMenu(int syncId, Inventory playerInventory, BlockEntity entity, ContainerData data) {
        super(DDIY_Menus.LOOT_CHEST_EDIT_MENU.get(), syncId);
        blockEntity = (LootChestEntity) entity;
        this.level = playerInventory.player.level;
        this.data = data;
        this.lootItems = blockEntity.getLootItems();

        if (!level.isClientSide()) {
            syncLootItems();
        }
        // Try optimizing this - Similar to items, we might only need to add the data slots for the ints related to the active pool.
        addDataSlots(data);
    }

    private void syncLootItems() {
        //DDIY.LOGGER.info("[Client="+level.isClientSide()+"] Sending sync packet. stacks: " + this.lootItems);
        DDIY_Packets.sendToClients(new InitLootChest_S2C(this.blockEntity.getBlockPos(), this.lootItems));
    }

    private void updateItemSlots() {
        // The only slots should be the ones for the active pool. Others can be removed.
        this.slots.clear();
        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            // There are 28 interactable items displayed at a time. 1 for icon + 27 for pool entries
            int itemIndex = this.getActivePool() * 28;
            // Pool Entries
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 9; j++) {
                    this.addSlot(new SlotItemHandler(handler, itemIndex++, 8 + j * 18, 106 + i * 18));
                }
            }
            // Icon
            this.addSlot(new SlotItemHandler(handler, itemIndex, 108, 42));
        });
    }

    public boolean displayItemScreen() { return showItemScreen; }

    public int getActivePool() { return this.data.get(this.data.getCount() - 2); }
    public int getActiveItemLocal() { return this.data.get(this.data.getCount() - 1); }
    public int getActiveItem() { return getActivePool() * 28 + getActiveItemLocal(); }

    public int[] getActiveRolls() {
        // Returns the min, max, bonus rolls for the currently active pool
        int index = getActivePoolStart();
        return new int[]{this.data.get(index), this.data.get(index + 1), this.data.get(index + 2)};
    }

    public boolean hasActive() {
        return hasActive;
    }

    public int getActivePoolStart() {
        // There are 84 ints per pool: min/max/bonus rolls + (min/max count, weight) * 27 items
        return getActivePool() * 84;
    }

    public int[] getActiveCounts() {
        // Returns the min count, max count, and weight for the active item
        int itemIndex = getActiveItemLocal();

        // If we opened the Item Screen due to clicking on the Icon Slot, it doesn't have counts.
        if (itemIndex == 27) return new int[]{0,0,0};

        // Rolls for the pool are 0-2
        int dataIndex = getActivePoolStart() + 3;
        // Each item has its 3 ints
        dataIndex += itemIndex * 3;
        return new int[]{this.data.get(dataIndex), this.data.get(dataIndex + 1), this.data.get(dataIndex + 2)};
    }

    // Hopefully this is okay here, since we don't have any real/functioning slots.
    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        return ItemStack.EMPTY;
        /*
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

         */
    }

    @Override
    public boolean clickMenuButton(Player pPlayer, int pId) {
        // We're keeping track of slotClicks as well as buttons
        // The screen will do fixed buttons first, then slots.
        //DDIY.LOGGER.info(Arrays.toString(this.blockEntity.dataArr));

        if (!this.showItemScreen) {
            // Change pool
            if (pId < 12) {
                this.data.set(this.data.getCount() - 2, pId);
                hasActive = true;
                updateItemSlots();
                return true;
            }
            // Arrows
            if (pId < 18) {
                // 0 1 2 == min max bonus
                int offset = (pId - 12) / 2;
                int index = this.getActivePoolStart() + offset;
                int value = this.data.get(index);
                // Even buttons are Decrement
                if (pId % 2 == 0) {
                    value--;
                } else {
                    value++;
                }
                // Clamp [0,16]
                value = Math.max(0, Math.min(16, value));
                this.data.set(index, value);

                // QOL: If min increases above max, max should increase as well, and vice versa.
                if (index % 3 == 0) {
                    int max = this.data.get(index + 1);
                    if (value > max) {
                        this.data.set(index + 1, ++max);
                    }
                }
                if (index % 3 == 1) {
                    int min = this.data.get(index - 1);
                    if (value < min) {
                        this.data.set(index - 1, --min);
                    }
                }
                return true;
            }
            // Reset Pool
            if (pId == 18) {
                // Reset data
                int start = getActivePoolStart();
                for (int i = start; i < start + 84; i++) {
                    int value = 1;
                    if (i % 84 <= 2) {
                        value = 0;
                    }
                    this.data.set(i, value);
                }
                // Reset ItemHandler
                this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
                    final int start1 = getActivePool() * 28;
                    for (int i = start1; i < start1 + 28; i++) {
                        handler.extractItem(i, 65, false);
                    }
                });
                return true;
            }
            // Open Inventories Screen
            if (pId >= 19) {
                // Sub 18 because we don't care about the other buttons when considering the active Item.
                this.data.set(this.data.getCount() - 1, pId - 19);
                // Flag used in Screen class to determine layout
                this.showItemScreen = true;
                // Remove the slots that displayed Pool level items
                this.slots.clear();
                return true;
            }
        } else {
            // Arrows
            if (pId < 6) {
                // 0 1 2 == min max weight
                int offset = pId / 2;

                // 0-26 (27 is the pool's icon which should never get here)
                int itemIndex = getActiveItemLocal();

                // Rolls for the pool are 0-2
                int dataIndex = getActivePoolStart() + 3;
                // Each item has its 3 ints
                int index = dataIndex + itemIndex * 3 + offset;
                int value = this.data.get(index);
                // Even buttons are Decrement
                if (pId % 2 == 0) {
                    value--;
                } else {
                    value++;
                }

                // Set min count
                if (index % 3 == 0) {
                    // Clamp [0,64]
                    value = Math.max(0, Math.min(64, value));
                    this.data.set(index, value);

                    // QOL: If min increases above max, max should increase as well
                    int max = this.data.get(index + 1);
                    if (value > max) {
                        this.data.set(index + 1, ++max);
                    }
                }
                // Set max count
                else if (index % 3 == 1) {
                    // Clamp [0,64]
                    value = Math.max(0, Math.min(64, value));
                    this.data.set(index, value);

                    // QOL: If max shrinks below min, min should shrink as well
                    int min = this.data.get(index - 1);
                    if (value < min) {
                        this.data.set(index - 1, --min);
                    }
                }
                // Set weight
                else {
                    // Clamp [1,100]
                    value = Math.max(1, Math.min(100, value));
                    this.data.set(index, value);
                }
                return true;
            }
            // Back
            if (pId == 6) {
                // Flag used in Screen class to determine layout
                this.showItemScreen = false;
                // Add the slots that display Pool level items
                this.updateItemSlots();
                return true;
            }

            // Reset Item
            if (pId == 7) {
                // Reset data
                // 0-26 (27 is the pool's icon which should never get here)
                int itemIndex = getActiveItemLocal();

                // Rolls for the pool are 0-2
                int dataIndex = getActivePoolStart() + 3;
                int index = dataIndex + itemIndex * 3;
                // Each item has its 3 ints
                for (int i = 0; i < 3; i++) {
                    this.data.set(index + i, 1);
                }

                // Reset ItemHandler
                this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
                    handler.extractItem(this.getActiveItem(), 65, false);
                });
                return true;
            }
            // LootItems
            // buttonCounter in screen#mouseClicked is always 8 when we get to loot items.
            // Subtract off to get the item's index.
            pId -= 8;
            int finalPId = pId;
            this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
                int i = this.getActiveItem();
                // The inserted item should be size 1, but the amount to remove is 65
                // as a reminder that we are just removing whatever exists at this location.
                handler.extractItem(i, 65, false);
                ItemStack toInsert = this.lootItems.get(finalPId).copy();
                toInsert.setCount(1);
                handler.insertItem(i, toInsert, false);
            });
        }
        return true;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                player, blockEntity.getBlockState().getBlock());
    }

    public boolean canScroll() {
        return this.lootItems.size() > ROWS * COLS;
    }

    public int getOffscreenRows() {
        return (this.lootItems.size() + COLS - 1) / COLS - ROWS;
    }
}