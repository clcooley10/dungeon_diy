package net.drDooley.dungeon_diy.unused;

import net.drDooley.dungeon_diy.block.DDIY_Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

public class BlueBuddingAmethystBlock extends BuddingAmethystBlock implements EntityBlock {
    private static final Direction[] DIRECTIONS = Direction.values();

    public BlueBuddingAmethystBlock(Properties pProperties) {
        super(pProperties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new BlueBuddingAmethystEntity(pPos, pState);
    }

    @Override
    public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
        if (pRandom.nextInt(5) == 0) {
            Direction direction = DIRECTIONS[pRandom.nextInt(DIRECTIONS.length)];
            BlockPos blockpos = pPos.relative(direction);
            BlockState blockstate = pLevel.getBlockState(blockpos);
            Block block = null;
            if (canClusterGrowAtState(blockstate)) {
                block = DDIY_Blocks.BLUE_SMALL_AMETHYST_BUD.get();
            } else if (blockstate.is(DDIY_Blocks.BLUE_SMALL_AMETHYST_BUD.get()) && blockstate.getValue(AmethystClusterBlock.FACING) == direction) {
                block = DDIY_Blocks.BLUE_MEDIUM_AMETHYST_BUD.get();
            } else if (blockstate.is(DDIY_Blocks.BLUE_MEDIUM_AMETHYST_BUD.get()) && blockstate.getValue(AmethystClusterBlock.FACING) == direction) {
                block = DDIY_Blocks.BLUE_LARGE_AMETHYST_BUD.get();
            } else if (blockstate.is(DDIY_Blocks.BLUE_LARGE_AMETHYST_BUD.get()) && blockstate.getValue(AmethystClusterBlock.FACING) == direction) {
                block = DDIY_Blocks.BLUE_AMETHYST_CLUSTER.get();
            }

            if (block != null) {
                BlockState blockstate1 = block.defaultBlockState().setValue(AmethystClusterBlock.FACING, direction).setValue(AmethystClusterBlock.WATERLOGGED, Boolean.valueOf(blockstate.getFluidState().getType() == Fluids.WATER));
                pLevel.setBlockAndUpdate(blockpos, blockstate1);
                BlockEntity buddingBE = pLevel.getBlockEntity(pPos);
                BlockEntity clusterBE = pLevel.getBlockEntity(blockpos);
                if (buddingBE instanceof BlueBuddingAmethystEntity bud && clusterBE instanceof BlueAmethystClusterEntity cluster) {
                    cluster.setParentUid(bud.getUid());
                }
            }
        }
    }
}
