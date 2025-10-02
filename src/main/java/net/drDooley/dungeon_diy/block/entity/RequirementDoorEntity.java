package net.drDooley.dungeon_diy.block.entity;

import net.drDooley.dungeon_diy.DDIY;
import net.drDooley.dungeon_diy.block.DDIY_Blocks;
import net.drDooley.dungeon_diy.block.RequirementDoorBlock;
import net.drDooley.dungeon_diy.screen.RequirementDoorActiveMenu;
import net.drDooley.dungeon_diy.screen.RequirementDoorEditMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class RequirementDoorEntity extends BlockEntity implements MenuProvider, DungeonLink {
    private DungeonConduitEntity conduit;
    private final ItemStackHandler editItemHandler = new ItemStackHandler(10) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };
//    private final ItemStackHandler activeItemHandler = new ItemStackHandler(5) {
//        @Override
//        protected void onContentsChanged(int slot) {
//            setChanged();
//        }
//    };
    private LazyOptional<IItemHandler> lazyEditHandler = LazyOptional.empty();
    //private LazyOptional<IItemHandler> lazyActiveHandler = LazyOptional.empty();

    public RequirementDoorEntity(BlockPos pPos, BlockState pBlockState) {
        super(DDIY_Blocks.REQUIREMENT_DOOR_ENTITY.get(), pPos, pBlockState);
    }

    public Boolean isLocked() {
        return this.getBlockState().getValue(RequirementDoorBlock.LOCKED);
    }

    public void setActive() {
        this.getBlockState().setValue(RequirementDoorBlock.ACTIVE, true);

//        for (int i = 0; i < editItemHandler.getSlots(); i++) {
//            activeItemHandler.setStackInSlot(i, editItemHandler.getStackInSlot(i));
//        }
    }
    public Boolean isActive() {
        return this.getBlockState().getValue(RequirementDoorBlock.ACTIVE);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("gui.dungeon_diy.requirement_door");
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) { return lazyEditHandler.cast(); }
        //if (cap == ForgeCapabilities.ITEM_HANDLER ) { return lazyActiveHandler.cast(); }
        return super.getCapability(cap);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyEditHandler = LazyOptional.of(() -> editItemHandler);
        //lazyActiveHandler = LazyOptional.of(() -> activeItemHandler);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyEditHandler.invalidate();
        //lazyActiveHandler.invalidate();
    }

    // ContainerHelper#saveAllItems
    @Override
    protected void saveAdditional(CompoundTag pTag) {
        pTag.put("masterInventory", editItemHandler.serializeNBT());
        //pTag.put("requiredInventory", activeItemHandler.serializeNBT());
        /*
        ListTag items = new ListTag();
        for (int i = 0; i < requiredItems.size(); i++) {
            ItemStack stack = requiredItems.get(i);
            if (!stack.isEmpty()) {
                CompoundTag compoundTag = new CompoundTag();
                compoundTag.putByte("Slot", (byte) i);
                stack.save(compoundTag);
                items.add(compoundTag);
            }
        }
        pTag.put("requiredInventory", items);

         */
        super.saveAdditional(pTag);
    }

    // ContainerHelper#loadAllItems
    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        editItemHandler.deserializeNBT(pTag.getCompound("masterInventory"));
        //activeItemHandler.deserializeNBT(pTag.getCompound("requiredInventory"));
        /*
        requiredItems = NonNullList.withSize(5, ItemStack.EMPTY);
        ListTag items = pTag.getList("requiredInventory", ListTag.TAG_COMPOUND);
        for (int i = 0; i < items.size(); i++) {
            CompoundTag compoundTag = items.getCompound(i);
            int j = compoundTag.getByte("Slot") & 255;
            if (j >= 0 && j < requiredItems.size()) {
                requiredItems.set(j, ItemStack.of(compoundTag));
            }
        }

         */
    }

    public void dropInventory() {
        SimpleContainer inv = new SimpleContainer(editItemHandler.getSlots());
        for (int i = 0; i < editItemHandler.getSlots(); i++) {
            inv.setItem(i, editItemHandler.getStackInSlot(i));
        }
        Containers.dropContents(this.level, this.worldPosition, inv);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        // FIXME: Testing
        if (pPlayer.isCrouching()) {
            this.setActive();
            DDIY.LOGGER.info("Creating Active Menu");
            return new RequirementDoorActiveMenu(pContainerId, pPlayerInventory, this);
        } else {
            DDIY.LOGGER.info("Creating Edit Menu");
            return new RequirementDoorEditMenu(pContainerId, pPlayerInventory, this);
        }

//        if (this.isActive()) {
//            return new RequirementDoorActiveMenu(pContainerId, pPlayerInventory, this);
//        } else {
//            return new RequirementDoorEditMenu(pContainerId, pPlayerInventory, this);
//        }
    }

    @Override
    public void registerConduit(BlockPos pos) {
        conduit = (DungeonConduitEntity) level.getBlockEntity(pos);
    }

//    public static void tick(Level level, BlockPos pos, BlockState state, RequirementDoorEntity pEntity) {
//        if (level.isClientSide()) {
//            return;
//        }
//    }
}
