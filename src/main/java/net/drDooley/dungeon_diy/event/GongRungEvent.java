package net.drDooley.dungeon_diy.event;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public class GongRungEvent extends PlayerInteractEvent.RightClickBlock {

    public GongRungEvent(Player player, InteractionHand hand, BlockPos pos, BlockHitResult hitVec) {
        super(player, hand, pos, hitVec);
    }
}
