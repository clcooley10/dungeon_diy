package net.drDooley.dungeon_diy.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

public class LayoutTeleporterBlock extends Block {
    public static final BooleanProperty ENTRANCE = BooleanProperty.create("ddiy_entrance");

    public LayoutTeleporterBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.stateDefinition.any().setValue(ENTRANCE, true));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(ENTRANCE);
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (pPlayer.getMainHandItem() != ItemStack.EMPTY) return InteractionResult.PASS;

        pLevel.setBlockAndUpdate(pPos, pState.setValue(ENTRANCE, !pState.getValue(ENTRANCE)));
        return InteractionResult.SUCCESS;
    }
}
