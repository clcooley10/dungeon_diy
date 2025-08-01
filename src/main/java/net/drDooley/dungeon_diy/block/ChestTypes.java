package net.drDooley.dungeon_diy.block;

import net.drDooley.dungeon_diy.DDIY;
import net.drDooley.dungeon_diy.block.entity.LootChestEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public enum ChestTypes {
    LOOT_CHEST,
    //TRAPPED_LOOT_CHEST,
    ;

    public final int columns, rows;
    public final boolean isBasic;

    ChestTypes(){
        this.columns = 9;
        this.rows = 3;
        this.isBasic = true;
    }

    ChestTypes(int rows, int columns){
        this.columns = columns;
        this.rows = rows;
        this.isBasic = false;
    }


    //returns chest type inventory size
    public int size(){
        return this.columns * this.rows;
    }

    public ResourceLocation getLoc(){
        return new ResourceLocation(DDIY.MODID, this.name().toLowerCase());
    }

    public BlockEntityType<? extends LootChestEntity> getBlockEntityType() {
        return switch (this) {
            case LOOT_CHEST -> DDIY_Blocks.LOOT_CHEST_ENTITY.get();
            //case TRAPPED_LOOT_CHEST -> DDIY_Blocks.TRAPPED_LOOT_CHEST_ENTITY.get();
        };
    }

    public LootChestEntity getBlockEntity(BlockPos pos, BlockState state) {
        return new LootChestEntity(pos, state, this);
    }


    public ResourceLocation getTextureId(){
        return new ResourceLocation(DDIY.MODID, "entity/chest/" + this.name().toLowerCase());
    }
}