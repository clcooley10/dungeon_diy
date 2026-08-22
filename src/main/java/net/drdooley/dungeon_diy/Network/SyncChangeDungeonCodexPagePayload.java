package net.drdooley.dungeon_diy.Network;

import net.drdooley.dungeon_diy.DungeonDIY;
import net.drdooley.dungeon_diy.Screen.DungeonCodexMenu;
import net.drdooley.dungeon_diy.Screen.CodexPageEnum;
import net.drdooley.dungeon_diy.Screen.DungeonCodexScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static net.drdooley.dungeon_diy.Screen.CodexPageEnum.CODEX_PAGE_CODEC;

public record SyncChangeDungeonCodexPagePayload(CodexPageEnum codexPageEnum) implements CustomPacketPayload {
    public static final Type<SyncChangeDungeonCodexPagePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(DungeonDIY.MOD_ID, "sync_change_dungeon_codex_page"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncChangeDungeonCodexPagePayload> STREAM_CODEC =
      StreamCodec.composite(
        CODEX_PAGE_CODEC,
        SyncChangeDungeonCodexPagePayload::codexPageEnum,

        SyncChangeDungeonCodexPagePayload::new
      );


    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }


    public static void handle(SyncChangeDungeonCodexPagePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player().containerMenu instanceof DungeonCodexMenu menu)) {
                return;
            }
            menu.setActivePage(payload.codexPageEnum());
            if (Minecraft.getInstance().screen instanceof DungeonCodexScreen screen) {
                screen.setPage(payload.codexPageEnum());
            }
        });
    }
}
