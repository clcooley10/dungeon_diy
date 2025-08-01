package net.drDooley.dungeon_diy.unused;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DungeonTeleporterBlock extends BaseEntityBlock {
    public static final BooleanProperty HAS_EYE = BooleanProperty.create("ddiy_has_eye");
    protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    public DungeonTeleporterBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.stateDefinition.any().setValue(HAS_EYE, false));
    }

    // If portals active, perform TP
//    @Override
//    public void stepOn(Level pLevel, BlockPos pPos, BlockState pState, Entity pEntity) {
//        super.stepOn(pLevel, pPos, pState, pEntity);
//    }

    // Add or Remove the overloaded eye from this entity
    // DimDungeon portalKeyhole
    @Override
    public @NotNull InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        ItemStack stack = pPlayer.getItemInHand(pHand);
        BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
        DungeonTeleporterEntity entity = (DungeonTeleporterEntity) blockEntity;

        if (blockEntity == null)  { return InteractionResult.PASS; }

        ItemStack cur_eye = entity.getEye();

        // Adding
        if (cur_eye.isEmpty()) {
            if (stack.getItem() instanceof OverloadedEye && !pLevel.isClientSide) {
                entity.setEye(stack.copy());
                stack.shrink(1);
            }
        }
        // Removing
        else {
            if (stack.isEmpty()) {
                pPlayer.setItemInHand(pHand, cur_eye);
            }
            else if (!pPlayer.addItem(cur_eye)) {
                pPlayer.drop(cur_eye, false);
            }
        entity.popEye();
        }

        pLevel.setBlockAndUpdate(pPos, pState.setValue(HAS_EYE, entity.hasEye()));

        return InteractionResult.SUCCESS;
    }

    // Furnace
    public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRandom) {
        if (pState.getValue(HAS_EYE)) {
            double d0 = (double)pPos.getX() + 0.5D;
            double d1 = (double)pPos.getY();
            double d2 = (double)pPos.getZ() + 0.5D;
            if (pRandom.nextDouble() < 0.01D) {
                pLevel.playLocalSound(d0, d1, d2, SoundEvents.RESPAWN_ANCHOR_DEPLETE, SoundSource.BLOCKS, 0.7F, 1.0F, false);
            }

            Direction direction = Direction.UP;
            Direction.Axis direction$axis = direction.getAxis();
            double d3 = 0.52D;
            double d4 = pRandom.nextDouble() * 0.6D - 0.3D;
            double d5 = direction$axis == Direction.Axis.X ? (double)direction.getStepX() * 0.52D : d4;
            double d6 = pRandom.nextDouble() * 6.0D / 16.0D;
            double d7 = direction$axis == Direction.Axis.Z ? (double)direction.getStepZ() * 0.52D : d4;
            pLevel.addParticle(ParticleTypes.SMOKE, d0 + d5, d1 + d6, d2 + d7, 0.0D, 0.0D, 0.0D);
            pLevel.addParticle(ParticleTypes.DRAGON_BREATH, d0 + d5, d1 + d6 + 0.2D, d2 + d7, 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(HAS_EYE);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) { return RenderShape.MODEL; }

    @Override
    public boolean useShapeForLightOcclusion(BlockState pState) { return true; }

    @Override
    public VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new DungeonTeleporterEntity(pPos, pState);
    }
}
