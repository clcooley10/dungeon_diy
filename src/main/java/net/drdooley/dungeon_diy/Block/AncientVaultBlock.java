package net.drdooley.dungeon_diy.Block;

import com.mojang.serialization.MapCodec;
import net.drdooley.dungeon_diy.Component.DDIYDataComponents;
import net.drdooley.dungeon_diy.Dungeon.DungeonInstance;
import net.drdooley.dungeon_diy.Dungeon.DungeonManager;
import net.drdooley.dungeon_diy.Item.AncientBookItem;
import net.drdooley.dungeon_diy.Item.DDIYItems;
import net.drdooley.dungeon_diy.Item.DungeonCodexItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class AncientVaultBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final MapCodec<AncientVaultBlock> CODEC = simpleCodec(AncientVaultBlock::new);
    private static final VoxelShape SHAPE_NS = Block.box(0, 0, 1, 16, 16, 15);
    private static final VoxelShape SHAPE_EW = Block.box(1, 0, 0, 15, 16, 16);

    public AncientVaultBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof AncientVaultBlockEntity vaultBE && !level.isClientSide) {
            DungeonInstance instance = DungeonManager.getDungeon((ServerLevel) level, vaultBE.getDungeonId());
            ((ServerPlayer) player).openMenu(new SimpleMenuProvider(vaultBE, Component.translatable("gui.dungeon_diy.ancient_vault_title")), buf -> {
                buf.writeBlockPos(pos);
                buf.writeInt(instance.getVaultInventory().getHandler().getSlots());
            });
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            if (!(stack.getItem() instanceof DungeonCodexItem)) {
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }
            ServerLevel world = (ServerLevel) level;
            UUID dungeonID = stack.get(DDIYDataComponents.DUNGEON_ID);
            if (!(world.getBlockEntity(pos) instanceof AncientVaultBlockEntity vaultBlockEntity) || dungeonID == null) {
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }

            vaultBlockEntity.setDungeonId(dungeonID);
            vaultBlockEntity.setChanged();
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
    private VoxelShape getShapeFromState(BlockState state) {
        return switch (state.getValue(FACING)) {
            case WEST, EAST -> SHAPE_EW;
            default -> SHAPE_NS;
        };
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShapeFromState(state);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShapeFromState(state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AncientVaultBlockEntity(pos, state);
    }
}
