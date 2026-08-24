package net.drdooley.dungeon_diy.Network;

import net.drdooley.dungeon_diy.Dungeon.DungeonInstance;
import net.drdooley.dungeon_diy.Dungeon.DungeonManager;
import net.drdooley.dungeon_diy.DungeonDIY;
import net.drdooley.dungeon_diy.Screen.CodexPageEnum;
import net.drdooley.dungeon_diy.Screen.DungeonCodexMenu;
import net.minecraft.core.NonNullList;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;
import java.util.UUID;

import static net.drdooley.dungeon_diy.Screen.CodexPageEnum.CODEX_PAGE_CODEC;

public record SavePedestalSettingsPayload(UUID dungeonId, List<ItemStack> acceptedItems) implements CustomPacketPayload {
    public static final Type<SavePedestalSettingsPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(DungeonDIY.MOD_ID, "save_pedestal_settings_payload"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SavePedestalSettingsPayload> STREAM_CODEC =
      StreamCodec.composite(
        UUIDUtil.STREAM_CODEC,
        SavePedestalSettingsPayload::dungeonId,

        ItemStack.OPTIONAL_LIST_STREAM_CODEC,
        SavePedestalSettingsPayload::acceptedItems,

        SavePedestalSettingsPayload::new
      );


    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SavePedestalSettingsPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            DungeonInstance dungeon = DungeonManager.getDungeon(player.serverLevel(), payload.dungeonId());
            if (dungeon == null) {
                return;
            }
            dungeon.setAcceptedPedestalStacks(NonNullList.copyOf(payload.acceptedItems()));
            if (player.containerMenu instanceof DungeonCodexMenu menu) {
                menu.setActivePage(CodexPageEnum.NODE_VIEW_EDIT);
                PacketDistributor.sendToPlayer(player, new SyncChangeDungeonCodexPagePayload(CodexPageEnum.NODE_VIEW_EDIT));
            }
        });
    }
}
