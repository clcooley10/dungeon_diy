package net.drdooley.dungeon_diy.Event;

import net.drdooley.dungeon_diy.DungeonDIY;
import net.drdooley.dungeon_diy.Item.DDIYItems;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

// 1. Dist.CLIENT tells NeoForge to completely ignore this class on dedicated servers
@EventBusSubscriber(modid = DungeonDIY.MOD_ID, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemProperties.register(
              DDIYItems.BROKEN_COMPASS.get(),
              ResourceLocation.withDefaultNamespace("angle"),
              (stack, level, entity, seed) -> {
                  if (level == null) return 0.0F;

                  // Check if the item has a valid Lodestone Tracker component
                  LodestoneTracker tracker = stack.get(DataComponents.LODESTONE_TRACKER);

                  if (tracker != null && tracker.target().isPresent()) {
                      GlobalPos targetGlobalPos = tracker.target().get();

                      // Make sure the compass target matches the current dimension the player is standing in
                      if (level.dimension() == targetGlobalPos.dimension()) {
                          BlockPos targetPos = targetGlobalPos.pos();

                          // Get the look vector of the entity holding the item, defaulting to 0 if none
                          double entityYRot = entity != null ? entity.getYRot() : 0.0;
                          // Calculate the mathematical angle from the player to the target block
                          double angleToTarget = Math.atan2(
                            (entity != null ? entity.getZ() : 0) - targetPos.getZ(),
                            (entity != null ? entity.getX() : 0) - targetPos.getX());

                          // Normalize the angle into Minecraft's 0.0 to 1.0 texture animation space
                          float rawAngle = (float) ((angleToTarget / (2 * Math.PI)) - (entityYRot / 360.0) + 0.25);
                          return (rawAngle % 1.0F + 1.0F) % 1.0F;
                      }
                  }

                  long time = level.getGameTime();
                  float speed = 0.02F;
                  float rawAngle = (time * speed) % 1.0F;
                  if (rawAngle < 0.0F) {
                      rawAngle += 1.0F;
                  }
                  return rawAngle;
              }
            );
        });
    }

}
