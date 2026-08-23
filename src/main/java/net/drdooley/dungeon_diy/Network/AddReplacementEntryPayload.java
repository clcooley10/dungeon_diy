package net.drdooley.dungeon_diy.Network;

import net.drdooley.dungeon_diy.Dungeon.DungeonInstance;
import net.drdooley.dungeon_diy.Dungeon.DungeonManager;
import net.drdooley.dungeon_diy.Dungeon.DungeonNode;
import net.drdooley.dungeon_diy.Dungeon.ReplacementEntry;
import net.drdooley.dungeon_diy.DungeonDIY;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record AddReplacementEntryPayload(UUID dungeonId, BlockPos nodePos, ReplacementEntry newEntry) implements CustomPacketPayload {
    public static final Type<AddReplacementEntryPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(DungeonDIY.MOD_ID, "add_replacement_entry"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AddReplacementEntryPayload> STREAM_CODEC =
      StreamCodec.composite(
        UUIDUtil.STREAM_CODEC,
        AddReplacementEntryPayload::dungeonId,

        BlockPos.STREAM_CODEC,
        AddReplacementEntryPayload::nodePos,

        ReplacementEntry.STREAM_CODEC,
        AddReplacementEntryPayload::newEntry,

        AddReplacementEntryPayload::new
      );


    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(AddReplacementEntryPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            DungeonInstance dungeon = DungeonManager.getDungeon(player.serverLevel(), payload.dungeonId());
            if (dungeon == null) return;
            DungeonNode node = dungeon.getNodes().get(payload.nodePos());
            if (node == null) return;
            node.addReplacement(payload.newEntry());
            dungeon.markDirty();

            PacketDistributor.sendToPlayer(player, new SyncAddReplacementEntryPayload(payload.nodePos, payload.newEntry()));
        });
    }
}
