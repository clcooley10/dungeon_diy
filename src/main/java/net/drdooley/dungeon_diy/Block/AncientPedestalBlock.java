package net.drdooley.dungeon_diy.Block;

import net.drdooley.dungeon_diy.Component.DDIYDataComponents;
import net.drdooley.dungeon_diy.Dungeon.DungeonInstance;
import net.drdooley.dungeon_diy.Dungeon.DungeonManager;
import net.drdooley.dungeon_diy.Item.AncientBookItem;
import net.drdooley.dungeon_diy.Item.DDIYItems;
import net.drdooley.dungeon_diy.Item.DungeonCodexItem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.UUID;

public class AncientPedestalBlock extends Block {
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 8, 16);

    public AncientPedestalBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            if (!(stack.getItem() instanceof DungeonCodexItem)) {
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }
            ServerLevel world = (ServerLevel) level;
            UUID dungeonID = stack.get(DDIYDataComponents.DUNGEON_ID);
            BlockEntity clickedBE = world.getBlockEntity(pos);
            if (!(clickedBE instanceof AncientPedestalBlockEntity) || dungeonID == null) {
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }
            AncientPedestalBlockEntity pedestalBE = (AncientPedestalBlockEntity) clickedBE;

            // Shift click -> Open menu to configure accepted inputs
            if (player.isShiftKeyDown()) {
                if (!pedestalBE.isBound()) {
                    return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
                }
                //
            }
            // Normal click -> link the dungeon
            else {
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
}
