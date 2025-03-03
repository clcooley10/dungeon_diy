package net.drDooley.dungeon_diy.networking;

import net.drDooley.dungeon_diy.block.DungeonTeleporterEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ItemStackSync_S2C {
    private final ItemStack itemStack;
    private final BlockPos pos;

    public ItemStackSync_S2C(ItemStack itemStack, BlockPos pos) {
        this.itemStack = itemStack;
        this.pos = pos;
    }

    public ItemStackSync_S2C(FriendlyByteBuf buf) {
        this.itemStack = buf.readItem();
        this.pos = buf.readBlockPos();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeItemStack(itemStack, true);
        buf.writeBlockPos(pos);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().level.getBlockEntity(pos) instanceof DungeonTeleporterEntity blockEntity) {
                blockEntity.receiveUpdate(this.itemStack);
            }
        });
        return true;
    }
}
