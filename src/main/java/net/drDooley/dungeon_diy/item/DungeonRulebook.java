package net.drDooley.dungeon_diy.item;

import net.drDooley.dungeon_diy.block.DDIY_Blocks;
import net.drDooley.dungeon_diy.block.DungeonManagerBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class DungeonRulebook extends Item {
    public DungeonRulebook(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        Level level = pContext.getLevel();
        BlockPos blockPos = pContext.getClickedPos();
        BlockState blockState = level.getBlockState(blockPos);

        // We clicked on a Manager block that does not currently have a book inserted
        if (blockState.is(DDIY_Blocks.DUNGEON_MANAGER.get()) && !blockState.getValue(DungeonManagerBlock.HAS_BOOK)) {
            ItemStack stack = pContext.getItemInHand();
            if (!level.isClientSide) {
                ((DungeonManagerBlock) DDIY_Blocks.DUNGEON_MANAGER.get()).setBook(pContext.getPlayer(), level, blockPos, blockState, stack);
                level.levelEvent((Player) null, 1010, blockPos, Item.getId(this));
                stack.shrink(1);
                Player player = pContext.getPlayer();
                if (player != null) {
                    //player.awardStat(Stats.DUNGEONS_MANAGED);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        } else {
            return InteractionResult.PASS;
        }
    }
}
