package net.drdooley.dungeon_diy.Network;

import net.drdooley.dungeon_diy.DungeonDIY;
import net.drdooley.dungeon_diy.Screen.DungeonCodexMenu;
import net.drdooley.dungeon_diy.Screen.CodexPageEnum;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static net.drdooley.dungeon_diy.Screen.CodexPageEnum.CODEX_PAGE_CODEC;

public record ChangeDungeonCodexPagePayload(CodexPageEnum codexPage) implements CustomPacketPayload {
    public static final Type<ChangeDungeonCodexPagePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(DungeonDIY.MOD_ID, "change_dungeon_codex_page"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ChangeDungeonCodexPagePayload> STREAM_CODEC =
      StreamCodec.composite(
        CODEX_PAGE_CODEC,
        ChangeDungeonCodexPagePayload::codexPage,

        ChangeDungeonCodexPagePayload::new
      );


    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ChangeDungeonCodexPagePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            if (player.containerMenu instanceof DungeonCodexMenu menu) {
                menu.setActivePage(payload.codexPage());
                PacketDistributor.sendToPlayer(player, new SyncChangeDungeonCodexPagePayload(payload.codexPage()));
            }
        });
    }
}
