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

public record ChangeReplacementEntryWeightPayload(UUID dungeonId, BlockPos nodePos, int replacementIndex, boolean increase) implements CustomPacketPayload {
    public static final Type<ChangeReplacementEntryWeightPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(DungeonDIY.MOD_ID, "change_replacement_weight"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ChangeReplacementEntryWeightPayload> STREAM_CODEC =
      StreamCodec.composite(
        UUIDUtil.STREAM_CODEC,
        ChangeReplacementEntryWeightPayload::dungeonId,

        BlockPos.STREAM_CODEC,
        ChangeReplacementEntryWeightPayload::nodePos,

        ByteBufCodecs.INT,
        ChangeReplacementEntryWeightPayload::replacementIndex,

        ByteBufCodecs.BOOL,
        ChangeReplacementEntryWeightPayload::increase,

        ChangeReplacementEntryWeightPayload::new
      );


    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ChangeReplacementEntryWeightPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            DungeonInstance dungeon = DungeonManager.getDungeon(player.serverLevel(), payload.dungeonId());
            if (dungeon == null) {
                return;
            }
            DungeonNode node = dungeon.getNodes().get(payload.nodePos());
            ReplacementEntry entry = node.getReplacements().get(payload.replacementIndex());
            if (payload.increase()) {
                entry.addWeight();
            }
            else {
                entry.removeWeight();
            }
            dungeon.markDirty();

            PacketDistributor.sendToPlayer(player, new SyncReplacementEntryWeightPayload(payload.nodePos(), payload.replacementIndex(), entry.getWeight()));
        });
    }
}
