package net.drdooley.dungeon_diy.Network;

import net.drdooley.dungeon_diy.Dungeon.DungeonInstance;
import net.drdooley.dungeon_diy.Dungeon.DungeonManager;
import net.drdooley.dungeon_diy.Dungeon.DungeonNode;
import net.drdooley.dungeon_diy.DungeonDIY;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.UUID;

public record RemoveReplacementEntryPayload(UUID dungeonId, BlockPos nodePos, int selectedIndex) implements CustomPacketPayload {
    public static final Type<RemoveReplacementEntryPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(DungeonDIY.MOD_ID, "remove_replacement_entry"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RemoveReplacementEntryPayload> STREAM_CODEC =
      StreamCodec.composite(
        UUIDUtil.STREAM_CODEC,
        RemoveReplacementEntryPayload::dungeonId,

        BlockPos.STREAM_CODEC,
        RemoveReplacementEntryPayload::nodePos,

        ByteBufCodecs.INT,
        RemoveReplacementEntryPayload::selectedIndex,

        RemoveReplacementEntryPayload::new
      );


    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RemoveReplacementEntryPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            DungeonInstance dungeon = DungeonManager.getDungeon(player.serverLevel(), payload.dungeonId());
            if (dungeon == null) return;
            DungeonNode node = dungeon.getNodes().get(payload.nodePos());
            if (node == null) return;
            node.removeReplacement(payload.selectedIndex());
            dungeon.markDirty();

            PacketDistributor.sendToPlayer(player, new SyncRemoveReplacementEntryPayload(payload.nodePos, payload.selectedIndex()));
        });
    }
}
