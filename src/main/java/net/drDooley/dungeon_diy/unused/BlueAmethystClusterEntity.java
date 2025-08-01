package net.drDooley.dungeon_diy.unused;

import net.drDooley.dungeon_diy.block.DDIY_Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public class BlueAmethystClusterEntity extends BlockEntity {
    private UUID parentUid;

    public BlueAmethystClusterEntity(BlockPos pPos, BlockState pBlockState) {
        super(DDIY_Blocks.BLUE_AMETHYST_CLUSTER_ENTITY.get(), pPos, pBlockState);
    }

    public void setParentUid(UUID uid) {
        this.parentUid = uid;
        setChanged();
    }

    public UUID getParentUid() {
        return parentUid;
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        if (pTag.hasUUID("ParentUID")) {
            parentUid = pTag.getUUID("ParentUID");
        }
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);
        if (parentUid != null) {
            pTag.putUUID("ParentUID", parentUid);
        }
    }
}
