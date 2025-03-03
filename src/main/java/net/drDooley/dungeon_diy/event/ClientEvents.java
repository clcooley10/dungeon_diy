package net.drDooley.dungeon_diy.event;

import net.drDooley.dungeon_diy.DDIY;
import net.drDooley.dungeon_diy.block.DDIY_Blocks;
import net.drDooley.dungeon_diy.block.DungeonTeleporterBlockEntityRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class ClientEvents {
    @Mod.EventBusSubscriber(modid = DDIY.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModBusEvents {
        @SubscribeEvent
        public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(DDIY_Blocks.DUNGEON_TELEPORTER_ENTITY.get(), DungeonTeleporterBlockEntityRenderer::new);
        }
    }
}
