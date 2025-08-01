package net.drDooley.dungeon_diy.block;

import net.drDooley.dungeon_diy.DDIY;
import net.drDooley.dungeon_diy.event.GongRungEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.MinecraftForge;

public class GongBlock extends Block {
    public GongBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        DDIY.LOGGER.info("Rung the Gong at " + pPos.toShortString());
        MinecraftForge.EVENT_BUS.post(new GongRungEvent(pPlayer, pHand, pPos, pHit));
        return InteractionResult.SUCCESS;
    }
}
