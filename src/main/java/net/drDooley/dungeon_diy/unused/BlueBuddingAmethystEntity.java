package net.drDooley.dungeon_diy.unused;

import net.drDooley.dungeon_diy.block.DDIY_Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public class BlueBuddingAmethystEntity extends BlockEntity {
    private UUID dungeonID;
    public BlueBuddingAmethystEntity(BlockPos pPos, BlockState pBlockState) {
        super(DDIY_Blocks.BLUE_BUDDING_AMETHYST_ENTITY.get(), pPos, pBlockState);
        this.dungeonID = UUID.randomUUID();
    }

    public UUID getUid() {
        return dungeonID;
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        if (pTag.hasUUID("UID")) {
            dungeonID = pTag.getUUID("UID");
        }
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);
        pTag.putUUID("UID", dungeonID);
    }
}
