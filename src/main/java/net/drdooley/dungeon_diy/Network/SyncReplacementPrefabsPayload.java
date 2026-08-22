package net.drdooley.dungeon_diy.Network;

import net.drdooley.dungeon_diy.DungeonDIY;
import net.drdooley.dungeon_diy.Dungeon.ReplacementPrefab;
import net.drdooley.dungeon_diy.Screen.DungeonCodexMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public record SyncReplacementPrefabsPayload(
  List<ReplacementPrefab> prefabs
) implements CustomPacketPayload {

    public static final Type<SyncReplacementPrefabsPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(DungeonDIY.MOD_ID, "sync_replacement_prefabs"));

    private static final StreamCodec<RegistryFriendlyByteBuf, List<ReplacementPrefab>> PREFABS_CODEC = StreamCodec.of(
      (buf, prefabs) -> {
          buf.writeVarInt(prefabs.size());

          for (ReplacementPrefab prefab : prefabs) {
              prefab.writeNetworkData(buf);
          }
      },
      buf -> {
          int size = buf.readVarInt();
          List<ReplacementPrefab> prefabs = new ArrayList<>(size);

          for (int i = 0; i < size; i++) {
              prefabs.add(ReplacementPrefab.readNetworkData(buf));
          }

          return prefabs;
      }
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncReplacementPrefabsPayload> STREAM_CODEC = StreamCodec.composite(
      PREFABS_CODEC,
      SyncReplacementPrefabsPayload::prefabs,

      SyncReplacementPrefabsPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncReplacementPrefabsPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (!(minecraft.player.containerMenu instanceof DungeonCodexMenu menu)) {
                return;
            }
            menu.updateReplacementPrefabs(payload.prefabs());
        });
    }
}