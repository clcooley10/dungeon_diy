package net.drDooley.dungeon_diy.unused;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BlueAmethystClusterBlock extends AmethystClusterBlock implements EntityBlock {
    public BlueAmethystClusterBlock(int pSize, int pOffset, Properties pProperties) {
        super(pSize, pOffset, pProperties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new BlueAmethystClusterEntity(pPos, pState);
    }
}
