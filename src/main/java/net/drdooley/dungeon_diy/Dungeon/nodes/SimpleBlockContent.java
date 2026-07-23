package net.drdooley.dungeon_diy.Dungeon.nodes;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

public class SimpleBlockContent extends NodeContent {
    private final BlockState blockState;
    public SimpleBlockContent(int weight, BlockState blockState) {
        super(weight);
        this.blockState = blockState;
    }

    @Override
    public void place(ServerLevel level, BlockPos pos) {
        level.setBlock(pos, blockState, 3);
    }

    @Override
    public void remove(ServerLevel level, BlockPos pos) {
        level.removeBlock(pos, false);
    }
}
