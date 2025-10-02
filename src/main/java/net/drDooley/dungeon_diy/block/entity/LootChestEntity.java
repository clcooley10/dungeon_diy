package net.drDooley.dungeon_diy.block.entity;

import com.google.gson.Gson;
import jdk.jfr.Event;
import net.drDooley.dungeon_diy.DDIY;
import net.drDooley.dungeon_diy.block.DDIY_Blocks;
import net.drDooley.dungeon_diy.block.LootChestBlock;
import net.drDooley.dungeon_diy.screen.LootChestActiveMenu;
import net.drDooley.dungeon_diy.screen.LootChestEditMenu;
import net.drDooley.dungeon_diy.screen.RequirementDoorActiveMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.ShulkerBoxScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.behavior.VillagerPanicTrigger;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.ObserverBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class LootChestEntity extends BlockEntity implements MenuProvider, DungeonLink {
    private ArrayList<ItemStack> lootItems = new ArrayList<>();
    private DungeonConduitEntity conduit;
    private BlockPos conduitPos = null;

    // (12 pools * 27 entries per pool + 1 item for the icon) + 27 entries for the active loot
    private final ItemStackHandler itemHandler = new ItemStackHandler(12 * 28 + 27) {
        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            setChanged();
        }
    };
    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    // Max size is 1010: (3 for pool config + 27 * 3 for item config * 12 pools) + activePool + activeItem
    protected final ContainerData data;
    public int[] dataArr = new int[1010];

    public LootChestEntity(BlockPos pPos, BlockState pBlockState) {
        super(DDIY_Blocks.LOOT_CHEST_ENTITY.get(), pPos, pBlockState);
        for (int i = 0; i < dataArr.length; i++) {
            if (i % 84 > 2) {
                dataArr[i] = 1;
            }
        }
        this.data = new ContainerData() {
            @Override
            public int get(int pIndex) { return LootChestEntity.this.dataArr[pIndex]; }

            @Override
            public void set(int pIndex, int pValue) {
                LootChestEntity.this.dataArr[pIndex] = pValue;
                setChanged();
            }

            @Override
            public int getCount() {
                return LootChestEntity.this.dataArr.length;
            }
        };
    }

    public void itemSync(List<ItemStack> stacks) {
        DDIY.LOGGER.info("[Client="+level.isClientSide()+"] Received sync packet. stacks: " + stacks);
        lootItems = new ArrayList<ItemStack>(stacks);
        setChanged();
    }

    public void setActive() {
        this.getBlockState().setValue(LootChestBlock.ACTIVE, true);
        if (level.isClientSide()) { return; }
        resetLoot();

        genLoot(genTable());
    }

    private void resetLoot() {
        DDIY.LOGGER.info("[Client=" + level.isClientSide + "] Reset Loot");
        ArrayList<BlockPos> sources = conduit.getLootSources();
        // Don't use a container, just use ArrayList, and exit early if toputback is empty
        SimpleContainer toPutBack = new SimpleContainer(27);
        for (int i = 12*28; i < itemHandler.getSlots(); i++) {
            toPutBack.addItem(itemHandler.getStackInSlot(i));
            itemHandler.setStackInSlot(i, new ItemStack(Items.AIR));
        }
        for (BlockPos pos : sources) {
            SimpleContainer remainders = new SimpleContainer(27);
            final SimpleContainer finalToPutBack = toPutBack;
            level.getBlockEntity(pos).getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
                for (int i = 0; i < finalToPutBack.getContainerSize(); i++) {
                    remainders.addItem(ItemHandlerHelper.insertItemStacked(handler, finalToPutBack.getItem(i), false));
                }
            });
            toPutBack.clearContent();
            toPutBack = remainders;
        }

        if (!toPutBack.isEmpty()) {
            DDIY.LOGGER.debug("The following items were destroyed because they could not find storage: " +  toPutBack);
        }
    }

    private ArrayList<ItemStack> genTable() {
        LootTable.Builder tableBuilder = LootTable.lootTable();
        // For each pool
        for (int i = 0; i < 12; i++) {
            int poolStart = 84*i;
            LootPool.Builder poolBuilder = LootPool.lootPool()
                    .setRolls(UniformGenerator.between(dataArr[poolStart], dataArr[poolStart + 1]))
                    .setBonusRolls(ConstantValue.exactly(dataArr[poolStart + 2]));
            // For each potential item in pool
            for (int j = 0; j < 27; j++) {
                int itemStart = poolStart + 3 + j * 3;
                // If Item is not AIR
                Item item = itemHandler.getStackInSlot(i*28 + j).getItem();
                if (item != Items.AIR) {
                    poolBuilder.add(LootItem.lootTableItem(item)
                            .setWeight(dataArr[itemStart + 2])
                            .apply(SetItemCountFunction.setCount(UniformGenerator.between(dataArr[itemStart], dataArr[itemStart + 1])))
                    );
                }
            }
            tableBuilder.withPool(poolBuilder);
        }
        LootTable table = tableBuilder.build();

        LootContext.Builder lootcontext$builder = (new LootContext.Builder((ServerLevel)this.level)).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(this.worldPosition)).withOptionalRandomSeed(RandomSource.create().nextLong());
        Player pPlayer = null;
        if (pPlayer != null) {
            lootcontext$builder.withLuck(pPlayer.getLuck()).withParameter(LootContextParams.THIS_ENTITY, pPlayer);
        }

        List<ItemStack> stacks = table.getRandomItems(lootcontext$builder.create(LootContextParamSets.CHEST));
        DDIY.LOGGER.info("Generated Loot Table with content: " + stacks);
        return new ArrayList<>(stacks);
    }

    private void genLoot(ArrayList<ItemStack> need) {
        // Going forward, found is what we'll actually put in the LootChest
        ArrayList<ItemStack> found = new ArrayList<>();
        for (BlockPos pos : conduit.getLootSources()) {
            ArrayList<ItemStack> finalFound = found;
            level.getBlockEntity(pos).getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
                for (int i = 0; i < handler.getSlots() && !need.isEmpty(); i++) {
                    ItemStack stack = handler.getStackInSlot(i);
                    for (int j = 0; j < need.size(); j++) {
                        if (need.get(j).is(stack.getItem())) {
                            ItemStack needStack = need.remove(j);
                            stack = handler.extractItem(i, needStack.getCount(), false);
                            finalFound.add(stack);
                            int have = stack.getCount();
                            int want = needStack.getCount();
                            if (have < want) {
                                needStack = new ItemStack(needStack.getItem(), want - have);
                                need.add(needStack);
                            }
                        }
                    }
                }
            });
            found = finalFound;
        }
        // If the length of collected items is greater than the chest size, attempt to condense it
        // Ex. If a double check was filled with 1 dirt in each slot, and we asked for 42 dirt, we'd currently have
        // an arrayList of len 42 because we add what we find in each slot one at a time. We want to deliver all 42
        // dirt, so we must condense it.

        // To more closely mimic vanilla, if the len is less than 28, just allow the spaces to be filled
        if (found.size() > 27) {
            for (int i = 0; i < found.size(); i++) {
                ItemStack stack = found.get(i);
                // Can it theoretically grow
                if (!stack.isEmpty() && stack.getCount() < stack.getMaxStackSize()) {
                    for (int j = i + 1; j < found.size(); j++) {
                        ItemStack other = found.get(j);
                        if (ItemHandlerHelper.canItemStacksStack(stack, other)) {
                            int spaceAvailable = stack.getMaxStackSize() - stack.getCount();
                            int canMove = Math.min(spaceAvailable, other.getCount());

                            // Condense
                            if (canMove > 0) {
                                stack.grow(canMove);
                                other.shrink(canMove);
                            }
                        }
                        // If we can't grow anymore, move to the next item
                        if (stack.getCount() >= stack.getMaxStackSize()) {
                            break;
                        }
                    }
                }
            }
        }
        // If our size is still greater than what will fit, we have to put stuff back.
        if (found.size() > 27) {
            ArrayList<ItemStack> toPutBack = (ArrayList<ItemStack>) found.subList(27, found.size());
            found = (ArrayList<ItemStack>) found.subList(0, 27);
            for (BlockPos pos : conduit.getLootSources()) {
                ArrayList<ItemStack> remainders = new ArrayList<>();
                ArrayList<ItemStack> finalToPutBack = toPutBack;
                level.getBlockEntity(pos).getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
                    for (ItemStack itemStack : finalToPutBack) {
                        remainders.add(ItemHandlerHelper.insertItemStacked(handler, itemStack, false));
                    }
                });
                toPutBack.clear();
                for (ItemStack stack : remainders) {
                    if (stack != ItemStack.EMPTY) {
                        toPutBack.add(stack);
                    }
                }
                if (toPutBack.isEmpty()) {
                    break;
                }
            }
            if (!toPutBack.isEmpty()) {
                DDIY.LOGGER.debug("The following items were destroyed because they could not find storage: " +  toPutBack);
            }
        }
        for (int i = 0; i < found.size() && i < itemHandler.getSlots(); i++) {
            itemHandler.insertItem(i + 12*28, found.get(i), false);
        }
    }

    public void getNsetLootItems() {
        if (level.isClientSide()) {
            return;
        }

        if (conduitPos != null && conduit == null) {
            registerConduit(conduitPos);
            if (conduit == null) {
                DDIY.LOGGER.warn("Couldn't find Conduit at " + conduitPos.toShortString() + ". Can't get linked chests. Try redoing the linking process.");
                return;
            }
        }

        this.lootItems.clear();
        for (BlockPos pos : conduit.getLootSources()) {
            level.getBlockEntity(pos).getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
                for (int i = 0; i < handler.getSlots(); i++) {
                    ItemStack stack = handler.getStackInSlot(i);
                    if (!stack.is(Items.AIR)) {
                        this.lootItems.add(stack.copy());
                    }
                }
            });
        }
        DDIY.LOGGER.info("lootItems after getNset: " + lootItems);
        setChanged();
    }

    public List<ItemStack> getLootItems() { return lootItems; }

    public void drops() {
        SimpleContainer toDrop = new SimpleContainer(27);
        for (int i = 12*28; i < itemHandler.getSlots(); i++) {
            toDrop.addItem(itemHandler.getStackInSlot(i));
        }
        Containers.dropContents(this.level, this.worldPosition, toDrop);
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);
        pTag.putIntArray("loot_chest.dataArr", dataArr);
        pTag.put("loot_chest.pool_inventory", itemHandler.serializeNBT());

        if (conduit != null) {
            CompoundTag conduitTag = new CompoundTag();
            BlockPos conduitPos = conduit.getBlockPos();
            conduitTag.putInt("x", conduitPos.getX());
            conduitTag.putInt("y", conduitPos.getY());
            conduitTag.putInt("z", conduitPos.getZ());
            pTag.put("loot_chest.conduit", conduitTag);
        }
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        dataArr = pTag.getIntArray("loot_chest.dataArr");
        itemHandler.deserializeNBT(pTag.getCompound("loot_chest.pool_inventory"));

        CompoundTag conduitTag = pTag.getCompound("loot_chest.conduit");
        conduitPos = new BlockPos(conduitTag.getInt("x"), conduitTag.getInt("y"), conduitTag.getInt("z"));
    }

    @Override
    public Component getDisplayName() {
        if (this.getBlockState().getValue(LootChestBlock.ACTIVE)) {
            return Component.translatable("gui.dungeon_diy.loot_chest.active");
        }
        return Component.translatable("gui.dungeon_diy.loot_chest.standby");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        // FIXME: Testing
        if (pPlayer.isCrouching()) {
            DDIY.LOGGER.info("Creating Active Menu");
            this.setActive();
            return new LootChestActiveMenu(pContainerId, pPlayerInventory, this);
        } else {
            DDIY.LOGGER.info("Creating Edit Menu");
            // Menu opening, use our conduit to gather the lootItems
            getNsetLootItems();
            return new LootChestEditMenu(pContainerId, pPlayerInventory, this, this.data);
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }
        return super.getCapability(cap);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag pTag = super.getUpdateTag();
        //NOTE: This could be more efficient by only adding the 12 indices we actually want to sync ahead of time.
        saveAdditional(pTag);
        return pTag;
    }

    @Override
    public void handleUpdateTag(CompoundTag pTag) {
        super.handleUpdateTag(pTag);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void registerConduit(BlockPos pos) {
        try {
            conduit = (DungeonConduitEntity) level.getBlockEntity(pos);
        } catch (NullPointerException ignored) { }
        DDIY.LOGGER.info("End of registerConduit");
    }
}
