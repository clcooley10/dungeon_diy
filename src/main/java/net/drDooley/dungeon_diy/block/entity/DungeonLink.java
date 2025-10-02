package net.drDooley.dungeon_diy.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface DungeonLink {
    void registerConduit(BlockPos pos);
}
