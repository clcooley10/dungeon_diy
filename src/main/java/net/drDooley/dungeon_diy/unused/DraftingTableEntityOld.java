package net.drDooley.dungeon_diy.unused;

import net.drDooley.dungeon_diy.block.DDIY_Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class DraftingTableEntityOld extends BlockEntity implements MenuProvider {
    public DraftingTableEntityOld(BlockPos pPos, BlockState pBlockState) {
        super(DDIY_Blocks.DRAFTING_TABLE_ENTITY_OLD.get(), pPos, pBlockState);
    }

    @Override
    public Component getDisplayName() {
        return null;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return null;
    }
}
