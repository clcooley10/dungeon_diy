package net.drDooley.dungeon_diy.block.entity;

import net.drDooley.dungeon_diy.DDIY;
import net.drDooley.dungeon_diy.block.ChestTypes;
import net.drDooley.dungeon_diy.block.LootChestBlock;
import net.drDooley.dungeon_diy.screen.LootChestEditMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class LootChestEntity extends ChestBlockEntity {

    private final ChestTypes type;
    private int viewers = 0;
    private float angle, last;
    private final ContainerOpenersCounter openersCounter;
    private final ChestLidController lidController;
    private NonNullList<ItemStack> items;

    public LootChestEntity(BlockPos pos, BlockState state, ChestTypes type) {
        this(type.getBlockEntityType(), pos, state, type);
    }

    public LootChestEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state, ChestTypes type) {
        super(blockEntityType, pos, state);
        this.type = type;
        this.items = NonNullList.withSize(type.rows * type.columns, ItemStack.EMPTY);
        this.openersCounter = new ContainerOpenersCounter() {
            protected void onOpen(Level level, BlockPos pos, BlockState state) {
            }

            protected void onClose(Level level, BlockPos pos, BlockState state) {
            }

            protected void openerCountChanged(Level level, BlockPos pos, BlockState state, int oldViewerCount, int newViewerCount) {
                LootChestEntity.this.signalOpenCount(level, pos, state, oldViewerCount, newViewerCount);
                viewers = newViewerCount;
                lidController.shouldBeOpen(newViewerCount > 0);

            }

            protected boolean isOwnContainer(Player player) {
                if (!(player.containerMenu instanceof LootChestEditMenu)) {
                    return false;
                } else {
                    Container container = ((LootChestEditMenu)player.containerMenu).getContainer();
                    return container instanceof LootChestEntity || container instanceof CompoundContainer && ((CompoundContainer) container).contains(LootChestEntity.this);
                }
            }
        };
        this.lidController = new ChestLidController();
    }

    public boolean triggerEvent(int id, int type) {
        if (id == 1) {
            this.lidController.shouldBeOpen(type > 0);
            return true;
        } else {
            return super.triggerEvent(id, type);
        }
    }

    public void setActive() {
        this.getBlockState().setValue(LootChestBlock.ACTIVE, true);
    }

    public Boolean isActive() {
        return this.getBlockState().getValue(LootChestBlock.ACTIVE);
    }


    @Override
    public int getContainerSize() {
        return type.rows * type.columns;
    }

    @Override
    public Component getDefaultName() {
        return Component.translatable("gui.dungeon_diy." + this.type.name().toLowerCase());
    }
    
    @Override
    public void startOpen(Player player) {
        if (!this.remove && !player.isSpectator()) {
            this.openersCounter.incrementOpeners(player, this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    @Override
    public void stopOpen(Player player) {
        if (!this.remove && !player.isSpectator()) {
            this.openersCounter.decrementOpeners(player, this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player pPlayer) {
        // FIXME: Testing
        if (pPlayer.isCrouching()) {
            this.setActive();
            DDIY.LOGGER.info("Creating Active Menu");
            return LootChestEditMenu.createLootChestEditMenu(syncId, playerInventory, this);
        } else {
            DDIY.LOGGER.info("Creating Edit Menu");
            return LootChestEditMenu.createLootChestEditMenu(syncId, playerInventory, this);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(tag)) {
            ContainerHelper.loadAllItems(tag, this.items);
        }
        this.viewers = tag.getInt("viewers");
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        if (!this.trySaveLootTable(tag)) {
            ContainerHelper.saveAllItems(tag, this.items);
        }
        tag.putInt("viewers", viewers);
    }

    public int countViewers() {
        return viewers;
    }

    public static void lidAnimateTick(Level level, BlockPos blockPos, BlockState blockState, LootChestEntity entity) {
        entity.lidController.tickLid();
    }

    private static void playSound(Level level, BlockPos pos, BlockState state, SoundEvent soundEvent) {
        level.playSound(null, (double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D, soundEvent, SoundSource.BLOCKS, 0.5F, level.random.nextFloat() * 0.1F + 0.9F);
    }
}