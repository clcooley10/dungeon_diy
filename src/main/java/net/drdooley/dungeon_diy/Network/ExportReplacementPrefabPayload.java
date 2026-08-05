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
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record ExportReplacementPrefabPayload(UUID dungeonId, BlockPos nodePos, String prefabName) implements CustomPacketPayload {


    public static final Type<ExportReplacementPrefabPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(DungeonDIY.MOD_ID, "export_replacement_prefab"));


    public static final StreamCodec<RegistryFriendlyByteBuf, ExportReplacementPrefabPayload> STREAM_CODEC =
      StreamCodec.composite(
        UUIDUtil.STREAM_CODEC,
        ExportReplacementPrefabPayload::dungeonId,

        BlockPos.STREAM_CODEC,
        ExportReplacementPrefabPayload::nodePos,

        ByteBufCodecs.STRING_UTF8,
        ExportReplacementPrefabPayload::prefabName,

        ExportReplacementPrefabPayload::new
      );


    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ExportReplacementPrefabPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            DungeonInstance dungeon = DungeonManager.getDungeon(player.serverLevel(), payload.dungeonId());
            if (dungeon == null) return;
            DungeonNode node = dungeon.getNodes().get(payload.nodePos());
            if (node == null) return;
            dungeon.addPrefab(payload.prefabName(), node.copyReplacements());
            dungeon.markDirty();
        });
    }
}
