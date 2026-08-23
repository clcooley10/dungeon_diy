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

public record SyncRemoveReplacementEntryPayload(BlockPos nodePos, int replacementIndex) implements CustomPacketPayload {
    public static final Type<SyncRemoveReplacementEntryPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(DungeonDIY.MOD_ID, "sync_remove_replacement_entry"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncRemoveReplacementEntryPayload> STREAM_CODEC =
      StreamCodec.composite(
        BlockPos.STREAM_CODEC,
        SyncRemoveReplacementEntryPayload::nodePos,

        ByteBufCodecs.INT,
        SyncRemoveReplacementEntryPayload::replacementIndex,

        SyncRemoveReplacementEntryPayload::new
      );


    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }


    public static void handle(SyncRemoveReplacementEntryPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (!(minecraft.player.containerMenu instanceof DungeonCodexMenu menu)) {
                return;
            }
            menu.removeReplacementEntryFromNode(payload.nodePos(), payload.replacementIndex());
        });
    }
}
