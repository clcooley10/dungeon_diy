package net.drdooley.dungeon_diy.Network;

import net.drdooley.dungeon_diy.DungeonDIY;
import net.drdooley.dungeon_diy.Screen.DungeonCodexMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncReplacementEntryWeightPayload(BlockPos nodePos, int replacementIndex, int weight) implements CustomPacketPayload {
    public static final Type<SyncReplacementEntryWeightPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(DungeonDIY.MOD_ID, "sync_replacement_entry_weight"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncReplacementEntryWeightPayload> STREAM_CODEC =
      StreamCodec.composite(
        BlockPos.STREAM_CODEC,
        SyncReplacementEntryWeightPayload::nodePos,

        ByteBufCodecs.INT,
        SyncReplacementEntryWeightPayload::replacementIndex,

        ByteBufCodecs.INT,
        SyncReplacementEntryWeightPayload::weight,

        SyncReplacementEntryWeightPayload::new
      );


    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }


    public static void handle(SyncReplacementEntryWeightPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (!(minecraft.player.containerMenu instanceof DungeonCodexMenu menu)) {
                return;
            }
            menu.updateReplacementWeight(payload.nodePos(), payload.replacementIndex(), payload.weight());
        });
    }
}
