package net.drdooley.dungeon_diy.Item;

import com.mojang.datafixers.util.Pair;
import net.drdooley.dungeon_diy.DungeonDIY;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.Optional;

public class BrokenCompassItem extends Item {
    public BrokenCompassItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        // 1. Ensure this executes strictly on the logical server
        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) return;

        // If it already has a target set, don't recalculate it
        LodestoneTracker existingTracker = stack.get(DataComponents.LODESTONE_TRACKER);
        if (existingTracker != null && existingTracker.target().isPresent()) return;

        try {
            ServerLevel netherLevel = serverLevel.getServer().getLevel(Level.NETHER);
            if (netherLevel != null) {
                System.out.println("[SERVER] inside netherLevel != null");
                Registry<Structure> structureRegistry = netherLevel.registryAccess().registryOrThrow(Registries.STRUCTURE);
                TagKey<Structure> structureTag = TagKey.create(Registries.STRUCTURE,
                  ResourceLocation.fromNamespaceAndPath(DungeonDIY.MOD_ID, "ruined_altar_structure"));
                Optional<HolderSet.Named<Structure>> optionalStructureSet = structureRegistry.getTag(structureTag);

                System.out.println("[SERVER] structureRegistry=" + structureRegistry);
                System.out.println("[SERVER] optionalStructureSet=" + optionalStructureSet);
                if (!optionalStructureSet.isPresent()) return;
                System.out.println("[DDIY DEBUG] Successfully verified structure tag inside active registry!");

                HolderSet<Structure> holderSet = optionalStructureSet.get();

                BlockPos position = entity.blockPosition();
                BlockPos scaledPos = new BlockPos(
                  (int) (position.getX() / 8.0),
                  position.getY(),
                  (int) (position.getZ() / 8.0)
                );

                // 3. Safely query structural generation trees
                Pair<BlockPos, Holder<Structure>> resultPair = netherLevel.getChunkSource().getGenerator()
                  .findNearestMapStructure(
                    netherLevel,
                    holderSet,
                    scaledPos, 100, true
                  );

                if (resultPair != null) {
                    System.out.println("[SERVER] inside resultPair != null");
                    BlockPos nearestStructure = resultPair.getFirst();
                    GlobalPos globalPos = GlobalPos.of(Level.NETHER, nearestStructure);
                    LodestoneTracker tracker = new LodestoneTracker(Optional.of(globalPos), false);
                    stack.set(DataComponents.LODESTONE_TRACKER, tracker);

                    System.out.println("[SERVER SUCCESS] Attached Nether target directly at: " + nearestStructure);
                }
            }
        } catch (Exception e) {
            System.err.println("[SERVER ERROR] Structure lookup failed: " + e.getMessage());
        }
    }
}
