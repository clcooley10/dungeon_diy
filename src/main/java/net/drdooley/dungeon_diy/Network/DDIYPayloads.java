package net.drdooley.dungeon_diy.Network;

import net.drdooley.dungeon_diy.DungeonDIY;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class DDIYPayloads {

    public static void register(final RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(DungeonDIY.MOD_ID).versioned("1");

        registrar.playToServer(ChangeReplacementEntryWeightPayload.TYPE, ChangeReplacementEntryWeightPayload.STREAM_CODEC, ChangeReplacementEntryWeightPayload::handle);
        registrar.playToClient(SyncReplacementEntryWeightPayload.TYPE, SyncReplacementEntryWeightPayload.STREAM_CODEC, SyncReplacementEntryWeightPayload::handle);

        registrar.playToServer(ExportReplacementPrefabPayload.TYPE, ExportReplacementPrefabPayload.STREAM_CODEC, ExportReplacementPrefabPayload::handle);
        registrar.playToClient(SyncReplacementPrefabsPayload.TYPE, SyncReplacementPrefabsPayload.STREAM_CODEC, SyncReplacementPrefabsPayload::handle);

        registrar.playToServer(ChangeDungeonCodexPagePayload.TYPE, ChangeDungeonCodexPagePayload.STREAM_CODEC, ChangeDungeonCodexPagePayload::handle);
        registrar.playToClient(SyncChangeDungeonCodexPagePayload.TYPE, SyncChangeDungeonCodexPagePayload.STREAM_CODEC, SyncChangeDungeonCodexPagePayload::handle);

        registrar.playToServer(ImportReplacementPrefabPayload.TYPE, ImportReplacementPrefabPayload.STREAM_CODEC, ImportReplacementPrefabPayload::handle);
        registrar.playToClient(SyncImportReplacementPayload.TYPE, SyncImportReplacementPayload.STREAM_CODEC, SyncImportReplacementPayload::handle);

    }
}