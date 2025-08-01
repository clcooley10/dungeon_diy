package net.drDooley.dungeon_diy.block;

import net.drDooley.dungeon_diy.block.entity.LootChestEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiPredicate;

public class LootChestBlock extends ChestBlock {
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    public final ChestTypes type;

    public LootChestBlock(Properties properties, ChestTypes type) {
        super(properties, type::getBlockEntityType);
        this.type = type;
        MinecraftForge.EVENT_BUS.register(this);
        this.registerDefaultState(this.defaultBlockState().setValue(ACTIVE, false));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return this.type.getBlockEntity(pos, state);
    }

    public ChestTypes getType() { return type; }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction direction = context.getHorizontalDirection().getOpposite();
        FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
        return this.defaultBlockState().setValue(FACING, direction).setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
    }

    public DoubleBlockCombiner.NeighborCombineResult<? extends ChestBlockEntity> combine(BlockState blockState, Level level, BlockPos blockPos, boolean ignoreBlockedChest) {
        BiPredicate<LevelAccessor, BlockPos> biPredicate;

        if (ignoreBlockedChest) {
            biPredicate = (levelAccessor, blockPos1) -> {
                return false;
            };
        } else {
            biPredicate = LootChestBlock::isChestBlockedAt;
        }

        return DoubleBlockCombiner.combineWithNeigbour(this.blockEntityType.get(), LootChestBlock::getBlockType, LootChestBlock::getConnectedDirection, FACING, blockState, level, blockPos, biPredicate);
    }

    public static boolean isChestBlockedAt(LevelAccessor levelAccessor, BlockPos blockPos) {
        return isBlockedChestByBlock(levelAccessor, blockPos) || isCatSittingOnChest(levelAccessor, blockPos);
    }

    private static boolean isBlockedChestByBlock(BlockGetter blockGetter, BlockPos blockPos) {
        BlockPos above = blockPos.above();

        return blockGetter.getBlockState(above).isRedstoneConductor(blockGetter, above);
    }

    private static boolean isCatSittingOnChest(LevelAccessor levelAccessor, BlockPos blockPos) {
        List<Cat> list = levelAccessor.getEntitiesOfClass(Cat.class, new AABB((double) blockPos.getX(), (double) (blockPos.getY() + 1), (double) blockPos.getZ(), (double) (blockPos.getX() + 1), (double) (blockPos.getY() + 2), (double) (blockPos.getZ() + 1)));

        if (!list.isEmpty()) {
            for (Cat cat : list) {
                if (cat.isInSittingPose()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? createTickerHelper(type, this.type.getBlockEntityType(), LootChestEntity::lidAnimateTick) : null;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(ACTIVE);
    }
}
