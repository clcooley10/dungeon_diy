package net.drDooley.dungeon_diy.networking;

import net.drDooley.dungeon_diy.dungeon.DungeonClientCache;
import net.drDooley.dungeon_diy.dungeon.DungeonConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class DungeonConfigSync_S2C {
    private final UUID id;
    private final CompoundTag nbt;

    public DungeonConfigSync_S2C(UUID id, DungeonConfig config) {
        this.id = id;
        this.nbt = config.save();
    }

    public DungeonConfigSync_S2C(FriendlyByteBuf buf) {
        this.id = buf.readUUID();
        this.nbt = buf.readNbt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(id);
        buf.writeNbt(nbt);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            Minecraft.getInstance().execute(() -> {
                DungeonClientCache.setDungeon(id, DungeonConfig.load(nbt));
            });
        });
        context.get().setPacketHandled(true);
    }
}
