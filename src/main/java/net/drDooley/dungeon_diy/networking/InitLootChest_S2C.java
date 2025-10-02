package net.drDooley.dungeon_diy.networking;

import net.drDooley.dungeon_diy.block.entity.LootChestEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class InitLootChest_S2C {
    private final BlockPos pos;
    private final List<ItemStack> syncItems;

    public InitLootChest_S2C(BlockPos pos, List<ItemStack> syncItems) {
        this.pos = pos;
        this.syncItems = syncItems;
    }

    public InitLootChest_S2C(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.syncItems = buf.readList(FriendlyByteBuf::readItem);
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeCollection(syncItems, FriendlyByteBuf::writeItem);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().level.getBlockEntity(pos) instanceof LootChestEntity blockEntity) {
                blockEntity.itemSync(this.syncItems);
            }
        });
        return true;
    }
}
