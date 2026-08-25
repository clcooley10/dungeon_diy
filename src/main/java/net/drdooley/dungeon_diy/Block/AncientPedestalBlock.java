package net.drdooley.dungeon_diy.Block;

import com.mojang.serialization.MapCodec;
import net.drdooley.dungeon_diy.Component.DDIYDataComponents;
import net.drdooley.dungeon_diy.Dungeon.DungeonInstance;
import net.drdooley.dungeon_diy.Dungeon.DungeonManager;
import net.drdooley.dungeon_diy.DungeonDIY;
import net.drdooley.dungeon_diy.Item.DungeonCodexItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class AncientPedestalBlock extends BaseEntityBlock {
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 8, 16);
    public static final MapCodec<AncientPedestalBlock> CODEC = simpleCodec(AncientPedestalBlock::new);

    public AncientPedestalBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            ServerLevel world = (ServerLevel) level;
            if (!(stack.getItem() instanceof DungeonCodexItem)) {
                if (level.getBlockEntity(pos) instanceof AncientPedestalBlockEntity pedestalBE) {
                    if (pedestalBE.displayedStack.getStackInSlot(0).isEmpty() && !stack.isEmpty()) {
                        ItemStack stack_orig = stack.copyWithCount(1);
                        pedestalBE.displayedStack.insertItem(0, stack.copy(), false);
                        stack.shrink(1);
                        level.playSound(player, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1f, 2f);
                        if (pedestalBE.isBound()) {
                            DungeonInstance instance = DungeonManager.getDungeon(world, pedestalBE.getDungeonId());
                            NonNullList<ItemStack> acceptedStacks = instance.getAcceptedPedestalStacks();
                            if (acceptedStacks.stream().anyMatch(acceptedStack -> acceptedStack.is(stack_orig.getItem()))) {
                                instance.generate();
                            }
                        }
                    } else if (stack.isEmpty()) {
                        ItemStack stackOnPedestal = pedestalBE.displayedStack.extractItem(0, 1, false);
                        player.setItemInHand(InteractionHand.MAIN_HAND, stackOnPedestal);
                        pedestalBE.clearContents();
                        level.playSound(player, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1f, 1f);
                    }
                }
            } else {
                UUID dungeonID = stack.get(DDIYDataComponents.DUNGEON_ID);
                BlockEntity clickedBE = world.getBlockEntity(pos);
                if (!(clickedBE instanceof AncientPedestalBlockEntity pedestalBE) || dungeonID == null) {
                    return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
                }
                pedestalBE.setDungeonId(dungeonID);
                pedestalBE.setChanged();
            }
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AncientPedestalBlockEntity(pos, state);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (state.getBlock() != newState.getBlock()) {
            if (level.getBlockEntity(pos) instanceof AncientPedestalBlockEntity blockEntity) {
                blockEntity.drops();
                level.updateNeighbourForOutputSignal(pos, this);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
