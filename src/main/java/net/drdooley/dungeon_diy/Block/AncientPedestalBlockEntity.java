package net.drdooley.dungeon_diy.Block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.UUID;

public class AncientPedestalBlockEntity extends BlockEntity {
    @Nullable
    private UUID dungeonId;
    public final ItemStackHandler displayedStack = new ItemStackHandler(1) {
        @Override
        protected int getStackLimit(int slot, ItemStack stack) {
            return 1;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (!level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };

    public AncientPedestalBlockEntity(BlockPos pos, BlockState blockState) {
        super(DDIYBlocks.ANCIENT_PEDESTAL_BE.get(), pos, blockState);
    }

    @Nullable
    public UUID getDungeonId() {
        return dungeonId;
    }

    public void setDungeonId(UUID dungeonId) {
        this.dungeonId = dungeonId;
        setChanged();
    }

    public boolean isBound() {
        return dungeonId != null;
    }

    public void clearContents() {
        displayedStack.setStackInSlot(0, ItemStack.EMPTY);
    }

    public void drops() {
        SimpleContainer displayed = new SimpleContainer(displayedStack.getSlots());
        for (int i = 0; i < displayedStack.getSlots(); i++) {
            displayed.setItem(i, displayedStack.getStackInSlot(i));
        }
        Containers.dropContents(this.level, this.worldPosition, displayed);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (dungeonId != null) {
            tag.putUUID("dungeonID", this.dungeonId);
        }
        tag.put("displayed", displayedStack.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.dungeonId = tag.getUUID("dungeonID");
        displayedStack.deserializeNBT(registries, tag.getCompound("displayed"));
    }
}
