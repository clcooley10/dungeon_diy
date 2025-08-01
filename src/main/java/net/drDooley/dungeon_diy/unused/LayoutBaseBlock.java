package net.drDooley.dungeon_diy.unused;

import net.drDooley.dungeon_diy.DDIY;
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

public class LayoutBaseBlock extends Block {
    public static final BooleanProperty NORTH = BooleanProperty.create("ddiy_door_north");
    public static final BooleanProperty EAST = BooleanProperty.create("ddiy_door_east");
    public static final BooleanProperty SOUTH = BooleanProperty.create("ddiy_door_south");
    public static final BooleanProperty WEST = BooleanProperty.create("ddiy_door_west");

    public LayoutBaseBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(NORTH, false)
                .setValue(EAST, false)
                .setValue(SOUTH, false)
                .setValue(WEST, false)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(NORTH, EAST, SOUTH, WEST);
    }

    // Change blockState value if the click was in certain places to indicate a door should be placed
    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (pPlayer.getMainHandItem() != ItemStack.EMPTY) return InteractionResult.PASS;

        double x_local = pHit.getLocation().x % 1;
        double y_local = pHit.getLocation().y % 1;
        double z_local = pHit.getLocation().z % 1;
        // Large values here mean different directions based on what quadrant you're in
        // make them all as if they were in pos X, pos Z territory
        if (x_local < 0) x_local += 1;
        if (z_local < 0) z_local += 1;

        x_local = Math.abs(x_local);
        y_local = Math.abs(y_local);
        z_local = Math.abs(z_local);
        //DDIY.LOGGER.info("x: {}, y: {}, z: {}", x_local, y_local, z_local);
        // North
        if ((x_local > 0.25 && x_local < 0.75 && y_local < 0.01 && z_local < 0.25) || (y_local > 0.25 && y_local < 0.75 && x_local < 0.01 && z_local < 0.25)) {
            pLevel.setBlockAndUpdate(pPos, pState.setValue(NORTH, !pState.getValue(NORTH)));
            return InteractionResult.SUCCESS;
        }
        // East
        else if ((z_local > 0.25 && z_local < 0.75 && y_local < 0.01 && x_local > 0.75) || (y_local > 0.25 && y_local < 0.75 && z_local < 0.01 && x_local > 0.75)) {
            pLevel.setBlockAndUpdate(pPos, pState.setValue(EAST, !pState.getValue(EAST)));
            return InteractionResult.SUCCESS;
        }
        // South
        else if ((x_local > 0.25 && x_local < 0.75 && y_local < 0.01 && z_local > 0.75) || (y_local > 0.25 && y_local < 0.75 && x_local < 0.01 && z_local > 0.75)) {
            pLevel.setBlockAndUpdate(pPos, pState.setValue(SOUTH, !pState.getValue(SOUTH)));
            return InteractionResult.SUCCESS;
        }
        // West
        else if ((z_local > 0.25 && z_local < 0.75 && y_local < 0.01 && x_local < 0.25) || (y_local > 0.25 && y_local < 0.75 && z_local < 0.01 && x_local < 0.25)) {
            pLevel.setBlockAndUpdate(pPos, pState.setValue(WEST, !pState.getValue(WEST)));
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
