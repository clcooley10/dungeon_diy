package net.drdooley.dungeon_diy.Network;

import net.drdooley.dungeon_diy.DungeonDIY;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class DDIYPayloads {

    public static void register(final RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(DungeonDIY.MOD_ID).versioned("1");
        registrar.playToServer(ChangeReplacementEntryWeightPayload.TYPE, ChangeReplacementEntryWeightPayload.STREAM_CODEC, ChangeReplacementEntryWeightPayload::handle);
        registrar.playToClient(SyncReplacementEntryWeightPayload.TYPE, SyncReplacementEntryWeightPayload.STREAM_CODEC, SyncReplacementEntryWeightPayload::handle);
    }
}