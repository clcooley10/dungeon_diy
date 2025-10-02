package net.drDooley.dungeon_diy.block;

import net.drDooley.dungeon_diy.block.entity.DungeonManagerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.EnchantmentTableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class DungeonManagerBlock extends BaseEntityBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty HAS_BOOK = BlockStateProperties.HAS_BOOK;
    protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 12.0D, 16.0D);

    public DungeonManagerBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, false).setValue(HAS_BOOK, false));
    }

    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new DungeonManagerEntity(pPos, pState);
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        BlockEntity entity = pLevel.getBlockEntity(pPos);
        if (!(entity instanceof DungeonManagerEntity)) { return InteractionResult.PASS; }

        ItemStack held = pPlayer.getItemInHand(pHand);

        // Remove book (Adding is done through useOn of appropriate rulebook item.
        if (pPlayer.isCrouching()) {
            if (pState.getValue(HAS_BOOK)) {
                this.dropBook(pLevel, pPos);
                pState = pState.setValue(HAS_BOOK, false);
                pLevel.setBlock(pPos, pState, 2);
                pLevel.gameEvent(GameEvent.BLOCK_CHANGE, pPos, GameEvent.Context.of(pPlayer, pState));
                return InteractionResult.sidedSuccess(pLevel.isClientSide);
            } else {
                return InteractionResult.PASS;
            }
        } else {
            // Open GUI
            if (!pLevel.isClientSide) {
                NetworkHooks.openScreen(((ServerPlayer) pPlayer), (DungeonManagerEntity) entity, pPos);
            }
            return InteractionResult.sidedSuccess(pLevel.isClientSide);
        }
    }

    private void dropBook(Level pLevel, BlockPos pPos) {
        if (!pLevel.isClientSide) {
            BlockEntity blockentity = pLevel.getBlockEntity(pPos);
            if (blockentity instanceof DungeonManagerEntity manager) {
                ItemStack rulebook = manager.getBook();
                if (!rulebook.isEmpty()) {
                    pLevel.levelEvent(1010, pPos, 0);
                    manager.removeBook();
                    float f = 0.7F;
                    double d0 = (double)(pLevel.random.nextFloat() * 0.7F) + (double)0.15F;
                    double d1 = (double)(pLevel.random.nextFloat() * 0.7F) + (double)0.060000002F + 0.6D;
                    double d2 = (double)(pLevel.random.nextFloat() * 0.7F) + (double)0.15F;
                    ItemStack itemstack1 = rulebook.copy();
                    ItemEntity itementity = new ItemEntity(pLevel, (double)pPos.getX() + d0, (double)pPos.getY() + d1, (double)pPos.getZ() + d2, itemstack1);
                    itementity.setDefaultPickUpDelay();
                    pLevel.addFreshEntity(itementity);
                }
            }
        }
    }

    public void setBook(@Nullable Entity pEntity, LevelAccessor pLevel, BlockPos pPos, BlockState pState, ItemStack pStack) {
        BlockEntity blockentity = pLevel.getBlockEntity(pPos);
        if (blockentity instanceof DungeonManagerEntity manager) {
            manager.setBook(pStack.copy());
            pLevel.setBlock(pPos, pState.setValue(HAS_BOOK, true), 2);
            pLevel.gameEvent(GameEvent.BLOCK_CHANGE, pPos, GameEvent.Context.of(pEntity, pState));
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(POWERED, HAS_BOOK);
    }

    public static boolean tryPlaceBook(@Nullable Player pPlayer, Level pLevel, BlockPos pPos, BlockState pState, ItemStack pBook) {
        if (!pState.getValue(HAS_BOOK)) {
            if (!pLevel.isClientSide) {
                placeBook(pPlayer, pLevel, pPos, pState, pBook);
            }
            return true;
        }
        return false;
    }

    private static void placeBook(@Nullable Player pPlayer, Level pLevel, BlockPos pPos, BlockState pState, ItemStack pBook) {
        BlockEntity blockentity = pLevel.getBlockEntity(pPos);
        if (blockentity instanceof DungeonManagerEntity managerEntity) {
            managerEntity.setBook(pBook.split(1));
            resetBookState(pLevel, pPos, pState, true);
            pLevel.playSound((Player)null, pPos, SoundEvents.BOOK_PUT, SoundSource.BLOCKS, 1.0F, 1.0F);
            pLevel.gameEvent(pPlayer, GameEvent.BLOCK_CHANGE, pPos);
        }
    }

    public static void resetBookState(Level pLevel, BlockPos pPos, BlockState pState, boolean pHasBook) {
        pLevel.setBlock(pPos, pState.setValue(POWERED, Boolean.valueOf(false)).setValue(HAS_BOOK, Boolean.valueOf(pHasBook)), 3);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return pLevel.isClientSide ? createTickerHelper(pBlockEntityType, DDIY_Blocks.DUNGEON_MANAGER_ENTITY.get(), DungeonManagerEntity::bookAnimationTick) : null;
    }
}
