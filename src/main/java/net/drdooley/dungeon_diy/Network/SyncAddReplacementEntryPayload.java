package net.drdooley.dungeon_diy.Network;

import net.drdooley.dungeon_diy.Dungeon.ReplacementEntry;
import net.drdooley.dungeon_diy.DungeonDIY;
import net.drdooley.dungeon_diy.Screen.CodexPageEnum;
import net.drdooley.dungeon_diy.Screen.DungeonCodexMenu;
import net.drdooley.dungeon_diy.Screen.DungeonCodexScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncAddReplacementEntryPayload(BlockPos nodePos, ReplacementEntry newEntry) implements CustomPacketPayload {
    public static final Type<SyncAddReplacementEntryPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(DungeonDIY.MOD_ID, "sync_add_replacement_entry"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncAddReplacementEntryPayload> STREAM_CODEC =
      StreamCodec.composite(
        BlockPos.STREAM_CODEC,
        SyncAddReplacementEntryPayload::nodePos,

        ReplacementEntry.STREAM_CODEC,
        SyncAddReplacementEntryPayload::newEntry,

        SyncAddReplacementEntryPayload::new
      );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncAddReplacementEntryPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (!(minecraft.player.containerMenu instanceof DungeonCodexMenu menu)) {
                return;
            }
            menu.addReplacementEntryToNode(payload.nodePos(), payload.newEntry);

            menu.setActivePage(CodexPageEnum.NODE_VIEW_EDIT);
            if (minecraft.screen instanceof DungeonCodexScreen screen) {
                screen.setPage(CodexPageEnum.NODE_VIEW_EDIT);
            }
        });
    }
}
