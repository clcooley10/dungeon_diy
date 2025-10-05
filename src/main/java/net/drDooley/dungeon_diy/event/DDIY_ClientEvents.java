package net.drDooley.dungeon_diy.event;

import net.drDooley.dungeon_diy.DDIY;
import net.drDooley.dungeon_diy.block.DDIY_Blocks;
import net.drDooley.dungeon_diy.render.DungeonManagerRenderer;
import net.drDooley.dungeon_diy.unused.DungeonTeleporterBlockEntityRenderer;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DDIY.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DDIY_ClientEvents {

    @SubscribeEvent
    public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(DDIY_Blocks.DUNGEON_TELEPORTER_ENTITY.get(), DungeonTeleporterBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(DDIY_Blocks.DUNGEON_MANAGER_ENTITY.get(), DungeonManagerRenderer::new);
    }

    @SubscribeEvent
    public static void onStitch(TextureStitchEvent.Pre event) {
        if (!event.getAtlas().location().equals(Sheets.CHEST_SHEET)) { return; }

        event.addSprite(new ResourceLocation(DDIY.MODID, "entity/chest/loot_chest"));
    }
}
