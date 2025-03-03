package net.drDooley.dungeon_diy.block;


import net.drDooley.dungeon_diy.DDIY;
import net.drDooley.dungeon_diy.networking.DDIY_Packets;
import net.drDooley.dungeon_diy.networking.ItemStackSync_S2C;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class DungeonTeleporterEntity extends BlockEntity {

    private ItemStack cur_key = ItemStack.EMPTY;
    public DungeonTeleporterEntity(BlockPos pPos, BlockState pBlockState) {
        super(DDIY_Blocks.DUNGEON_TELEPORTER_ENTITY.get(), pPos, pBlockState);
    }

    private void sendUpdate() {
        if (!level.isClientSide()) {
            DDIY_Packets.sendToClients(new ItemStackSync_S2C(this.cur_key, worldPosition));
        }
    }


    public void receiveUpdate(ItemStack stack) {
        cur_key = stack;
    }

    public ItemStack getEye() { return this.cur_key; }
    public boolean hasEye() { return !cur_key.isEmpty(); }
    public void setEye(ItemStack eye) {
        this.cur_key = eye;
        this.cur_key.setCount(1);
        this.setChanged();
        sendUpdate();
    }
    public void popEye() {
        this.cur_key = ItemStack.EMPTY;
        this.setChanged();
        sendUpdate();
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        if (nbt.contains("cur_key")) {
            setEye(ItemStack.of(nbt.getCompound("cur_key")));
        }
    }
    @Override
    protected void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        nbt.put("cur_key", cur_key.save(new CompoundTag()));
    }
}
