package net.drdooley.dungeon_diy.Network;

import net.drdooley.dungeon_diy.Dungeon.DungeonInstance;
import net.drdooley.dungeon_diy.Dungeon.DungeonManager;
import net.drdooley.dungeon_diy.Dungeon.DungeonNode;
import net.drdooley.dungeon_diy.Dungeon.ReplacementEntry;
import net.drdooley.dungeon_diy.DungeonDIY;
import net.drdooley.dungeon_diy.Screen.CodexPageEnum;
import net.drdooley.dungeon_diy.Screen.DungeonCodexMenu;
import net.drdooley.dungeon_diy.Screen.DungeonCodexScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record SyncImportReplacementPayload(BlockPos nodePos, List<ReplacementEntry> replacements) implements CustomPacketPayload {
    public static final Type<SyncImportReplacementPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(DungeonDIY.MOD_ID, "sync_import_replacement_prefab"));

    private static final StreamCodec<RegistryFriendlyByteBuf, List<ReplacementEntry>> REPLACEMENTS_CODEC = StreamCodec.of(
      (buf, entries) -> {
          buf.writeVarInt(entries.size());
          for (ReplacementEntry entry : entries) {
              ReplacementEntry.STREAM_CODEC.encode(buf, entry);
          }
      },
      buf -> {
          int size = buf.readVarInt();
          List<ReplacementEntry> entries = new ArrayList<>(size);
          for (int i = 0; i < size; i++) {
              entries.add(ReplacementEntry.STREAM_CODEC.decode(buf));
          }
          return entries;
      }
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncImportReplacementPayload> STREAM_CODEC =
      StreamCodec.composite(
        BlockPos.STREAM_CODEC,
        SyncImportReplacementPayload::nodePos,

        REPLACEMENTS_CODEC,
        SyncImportReplacementPayload::replacements,

        SyncImportReplacementPayload::new
      );


    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncImportReplacementPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (!(minecraft.player.containerMenu instanceof DungeonCodexMenu menu)) {
                return;
            }
            menu.updateNodeReplacements(payload.nodePos(), payload.replacements());
            menu.setSelectedReplacementIndex(0);
            menu.setActivePage(CodexPageEnum.NODE_VIEW_EDIT);

            if (minecraft.screen instanceof DungeonCodexScreen screen) {
                screen.setPage(CodexPageEnum.NODE_VIEW_EDIT);
            }
        });
    }
}
