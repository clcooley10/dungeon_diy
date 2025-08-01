package net.drDooley.dungeon_diy.unused;

import net.drDooley.dungeon_diy.block.DDIY_Blocks;
import net.drDooley.dungeon_diy.item.DDIY_Items;
import net.drDooley.dungeon_diy.screen.DDIY_Menus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class LayoutBlockMenu extends AbstractContainerMenu {
    public static final int THEME_SLOT = 0;
    private final ContainerLevelAccess access;
    public final Container inputSlots;
    Runnable slotUpdateListener;
    public LayoutBlockMenu(int pContainerId, Inventory pPlayerInventory, FriendlyByteBuf pBuffer) {
        this(pContainerId, pPlayerInventory, ContainerLevelAccess.NULL);
    }
    public LayoutBlockMenu(int pContainerId, Inventory pPlayerInventory, final ContainerLevelAccess pAccess) {
        super(DDIY_Menus.LAYOUT_BLOCK_MENU.get(), pContainerId);
        this.access = pAccess;
        this.slotUpdateListener = () -> { };
        this.inputSlots = new SimpleContainer(1) {
            public void setChanged() {
                super.setChanged();
                LayoutBlockMenu.this.slotsChanged(this);
                LayoutBlockMenu.this.slotUpdateListener.run();
            }
        };
        this.addSlot(new Slot(this.inputSlots, THEME_SLOT, 40, 20) {
            public boolean mayPlace(ItemStack itemStack) {
                return itemStack.is(DDIY_Items.OVERLOADED_EYE.get());
            }
            public int getMaxStackSize() { return 1; }
        });
        // These are the player inventory slots
        int i;
        for(i = 0; i < 3; ++i) {
            for(int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(pPlayerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        // Player hotbar
        for(i = 0; i < 9; ++i) {
            this.addSlot(new Slot(pPlayerInventory, i, 8 + i * 18, 142));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = (Slot)this.slots.get(pIndex);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            // Moving slotted stacks out, back into inventory
            if (pIndex == THEME_SLOT) {
                if (!this.moveItemStackTo(itemstack1, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }
            }
            // Moving blueprint into the layout block
            else if (itemstack1.is(DDIY_Items.OVERLOADED_EYE.get())) {
                if (!this.moveItemStackTo(itemstack1, THEME_SLOT, THEME_SLOT+1, true)) {
                    return ItemStack.EMPTY;
                }
            }
            /*
            // Moving item into the right slot
            else if (itemstack1.is(DGItems.ROSETTA_STONE.get()) || itemstack1.is(GRIMOIRE)) {
                if (((Slot)this.slots.get(RIGHT_SLOT)).hasItem()) {
                    return ItemStack.EMPTY;
                }

                ItemStack itemstack2 = itemstack1.copy();
                itemstack2.setCount(1);
                itemstack1.shrink(1);
                ((Slot)this.slots.get(RIGHT_SLOT)).set(itemstack2);
            }
            // Moving item into the left slot
            else {
                if (((Slot)this.slots.get(LEFT_SLOT)).hasItem() || !((Slot)this.slots.get(LEFT_SLOT)).mayPlace(itemstack1)) {
                    return ItemStack.EMPTY;
                }

                ItemStack itemstack2 = itemstack1.copy();
                itemstack2.setCount(1);
                itemstack1.shrink(1);
                ((Slot)this.slots.get(LEFT_SLOT)).set(itemstack2);
            }

            */
            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(pPlayer, itemstack1);
        }
        return itemstack;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return stillValid(this.access, pPlayer, DDIY_Blocks.LAYOUT_BLOCK_BASE.get());
    }

    public void registerUpdateListener(Runnable pListener) {
        this.slotUpdateListener = pListener;
    }

    public void removed(Player pPlayer) {
        super.removed(pPlayer);
        this.access.execute((p_40313_, p_40314_) -> {
            this.clearContainer(pPlayer, this.inputSlots);
        });
    }
}
