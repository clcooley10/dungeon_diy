package net.drdooley.dungeon_diy.Block;

import net.drdooley.dungeon_diy.Dungeon.DungeonInstance;
import net.drdooley.dungeon_diy.Dungeon.DungeonManager;
import net.drdooley.dungeon_diy.Screen.AncientVaultMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.UUID;

public class AncientVaultBlockEntity extends BlockEntity implements MenuProvider {
    @Nullable
    private UUID dungeonId;

    public AncientVaultBlockEntity(BlockPos pos, BlockState blockState) {
        super(DDIYBlocks.ANCIENT_VAULT_BE.get(), pos, blockState);
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

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (dungeonId != null) {
            tag.putUUID("dungeonID", this.dungeonId);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.dungeonId = tag.getUUID("dungeonID");
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("menu.dungeon_diy.ancient_vault");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        DungeonInstance instance = DungeonManager.getDungeon((ServerLevel) player.level(), dungeonId);
        if (instance == null) return null;
        return new AncientVaultMenu(containerId, playerInventory, this);
    }
}
