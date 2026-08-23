package net.drdooley.dungeon_diy.Network;

import net.drdooley.dungeon_diy.DungeonDIY;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class DDIYPayloads {

    public static void register(final RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(DungeonDIY.MOD_ID).versioned("1");

        // Change pages
        registrar.playToServer(ChangeDungeonCodexPagePayload.TYPE, ChangeDungeonCodexPagePayload.STREAM_CODEC, ChangeDungeonCodexPagePayload::handle);
        registrar.playToClient(SyncChangeDungeonCodexPagePayload.TYPE, SyncChangeDungeonCodexPagePayload.STREAM_CODEC, SyncChangeDungeonCodexPagePayload::handle);
        // Replacement Entry
        registrar.playToServer(ChangeReplacementEntryWeightPayload.TYPE, ChangeReplacementEntryWeightPayload.STREAM_CODEC, ChangeReplacementEntryWeightPayload::handle);
        registrar.playToClient(SyncReplacementEntryWeightPayload.TYPE, SyncReplacementEntryWeightPayload.STREAM_CODEC, SyncReplacementEntryWeightPayload::handle);
        //    Add
        registrar.playToServer(AddReplacementEntryPayload.TYPE, AddReplacementEntryPayload.STREAM_CODEC, AddReplacementEntryPayload::handle);
        registrar.playToClient(SyncAddReplacementEntryPayload.TYPE, SyncAddReplacementEntryPayload.STREAM_CODEC, SyncAddReplacementEntryPayload::handle);
        //    Remove
        registrar.playToServer(RemoveReplacementEntryPayload.TYPE, RemoveReplacementEntryPayload.STREAM_CODEC, RemoveReplacementEntryPayload::handle);
        registrar.playToClient(SyncRemoveReplacementEntryPayload.TYPE, SyncRemoveReplacementEntryPayload.STREAM_CODEC, SyncRemoveReplacementEntryPayload::handle);
        //    Import
        registrar.playToServer(ImportReplacementPrefabPayload.TYPE, ImportReplacementPrefabPayload.STREAM_CODEC, ImportReplacementPrefabPayload::handle);
        registrar.playToClient(SyncImportReplacementPayload.TYPE, SyncImportReplacementPayload.STREAM_CODEC, SyncImportReplacementPayload::handle);
        //    Export
        registrar.playToServer(ExportReplacementPrefabPayload.TYPE, ExportReplacementPrefabPayload.STREAM_CODEC, ExportReplacementPrefabPayload::handle);
        registrar.playToClient(SyncReplacementPrefabsPayload.TYPE, SyncReplacementPrefabsPayload.STREAM_CODEC, SyncReplacementPrefabsPayload::handle);

    }
}