package net.drdooley.dungeon_diy.Block;

import net.drdooley.dungeon_diy.Component.DDIYDataComponents;
import net.drdooley.dungeon_diy.Dungeon.DungeonInstance;
import net.drdooley.dungeon_diy.Dungeon.DungeonManager;
import net.drdooley.dungeon_diy.Item.AncientBookItem;
import net.drdooley.dungeon_diy.Item.DDIYItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class RuinedAltarBlock extends Block {
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 12, 16);

    public RuinedAltarBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide) {

            if (!(stack.getItem() instanceof AncientBookItem))
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

            ServerLevel world = (ServerLevel) level;
            DungeonInstance instance = DungeonManager.createDungeon(world);
            ItemStack codex = new ItemStack(DDIYItems.DUNGEON_CODEX.get());
            codex.set(DDIYDataComponents.DUNGEON_ID, instance.getId());

            ItemEntity itemEntity = new ItemEntity(world, pos.getX() + 0.5F, pos.getY() + 1, pos.getZ() + 0.5F, codex);
            itemEntity.setDeltaMovement(0, 0.3, 0);
            world.addFreshEntity(itemEntity);
            stack.shrink(1);
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
