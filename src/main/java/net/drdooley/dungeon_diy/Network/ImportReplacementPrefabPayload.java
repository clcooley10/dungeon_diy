package net.drdooley.dungeon_diy.Network;

import net.drdooley.dungeon_diy.Dungeon.*;
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

public record ImportReplacementPrefabPayload(UUID dungeonId, BlockPos nodePos, int replacementPrefabIndex) implements CustomPacketPayload {
    public static final Type<ImportReplacementPrefabPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(DungeonDIY.MOD_ID, "import_replacement_prefab"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ImportReplacementPrefabPayload> STREAM_CODEC =
      StreamCodec.composite(
        UUIDUtil.STREAM_CODEC,
        ImportReplacementPrefabPayload::dungeonId,

        BlockPos.STREAM_CODEC,
        ImportReplacementPrefabPayload::nodePos,

        ByteBufCodecs.INT,
        ImportReplacementPrefabPayload::replacementPrefabIndex,

        ImportReplacementPrefabPayload::new
      );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ImportReplacementPrefabPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            DungeonInstance dungeon = DungeonManager.getDungeon(player.serverLevel(), payload.dungeonId());
            if (dungeon == null) {
                return;
            }
            DungeonNode node = dungeon.getNodes().get(payload.nodePos());
            if (node == null) {
                return;
            }
            if (payload.replacementPrefabIndex() < 0 || payload.replacementPrefabIndex() >= dungeon.getReplPrefabs().size()) {
                return;
            }
            ReplacementPrefab prefab = dungeon.getReplPrefab(payload.replacementPrefabIndex());
            if (prefab == null) {
                return;
            }
            node.setReplacements(new ArrayList<>(prefab.entries));
            dungeon.markDirty();

            PacketDistributor.sendToPlayer(player, new SyncImportReplacementPayload(payload.nodePos(), node.copyReplacements()));
        });
    }
}
