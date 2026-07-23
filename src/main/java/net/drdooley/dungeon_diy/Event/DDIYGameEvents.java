package net.drdooley.dungeon_diy.Event;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.drdooley.dungeon_diy.DungeonDIY;
import net.drdooley.dungeon_diy.item.DDIYItems;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;

import java.util.List;
import java.util.Optional;

@EventBusSubscriber(modid = DungeonDIY.MOD_ID)
public class DDIYGameEvents {

    @SubscribeEvent
    public static void addCustomTrades(VillagerTradesEvent event) {
        if (event.getType() == VillagerProfession.LIBRARIAN) {
            List<VillagerTrades.ItemListing> masterTrades = event.getTrades().get(5);

            if (masterTrades != null) {
                // Get rid of candle trades, if villager rework is used, it will be custom book + enchanted book
                // if rework is not used, the only trade will be the custom mod book
                masterTrades.removeIf(trade -> trade.toString().contains("candle"));

                masterTrades.add((entity, randomsource) -> new MerchantOffer(
                  new ItemCost(Items.EMERALD, 32),
                  Optional.of(new ItemCost(DDIYItems.TATTERED_BOOK.get(), 1)),
                  new ItemStack(DDIYItems.ANCIENT_BOOK.get(), 1),
                  1, 15, 0.0f
                ));
            }
        }
        if (event.getType() == VillagerProfession.CARTOGRAPHER) {
            List<VillagerTrades.ItemListing> masterCartTrades = event.getTrades().get(5);

            masterCartTrades.add((entity, randomsource) -> new MerchantOffer(
              new ItemCost(Items.EMERALD, 32),
              Optional.of(new ItemCost(DDIYItems.TATTERED_BOOK.get(), 1)),
              new ItemStack(Items.PAPER, 1),
              1, 15, 0.0f
            ));
        }
    }
}


/*
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;

import java.util.List;
import java.util.Optional;

@Mod.EventBusSubscriber(modid = "your_mod_id", bus = Mod.EventBusSubscriber.Bus.GAME)
public class ModExplorerMapTrade {

    // Define a TagKey matching your custom structure config registry
    private static final TagKey<Structure> CUSTOM_STRUCTURE_TAG = TagKey.create(
            Registries.STRUCTURE,
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("your_mod_id", "your_custom_structure")
    );

    @SubscribeEvent
    public static void onVillagerTrades(VillagerTradesEvent event) {
        // Typically, map-sellers are assigned to the Cartographer profession
        if (event.getType() == VillagerProfession.CARTOGRAPHER) {
            // Level 2 is Apprentice, where explorer maps are typically offered
            List<VillagerTrades.ItemListing> apprenticeTrades = event.getTrades().get(2);

            if (apprenticeTrades != null) {
                // Add the explorer map trade offering
                apprenticeTrades.add(new CustomExplorerMapListing(
                        CUSTOM_STRUCTURE_TAG,
                        "filled_map.your_mod_id.custom_structure_name"
                ));
            }
        }
    }

    private static class CustomExplorerMapListing implements VillagerTrades.ItemListing {
        private final TagKey<Structure> destination;
        private final String displayNameKey;

        public CustomExplorerMapListing(TagKey<Structure> destination, String displayNameKey) {
            this.destination = destination;
            this.displayNameKey = displayNameKey;
        }

        @Override
        public MerchantOffer getOffer(Entity trader, RandomSource random) {
            // Verify if the system context is running on a live Server World
            if (!(trader.level() instanceof ServerLevel serverLevel)) {
                return null;
            }

            BlockPos villagerPos = trader.blockPosition();

            // Locate the nearest structure matching your targeted custom mod tag
            BlockPos targetPos = serverLevel.findNearestMapStructure(
                    this.destination,
                    villagerPos,
                    100, // Look within 100 chunks maximum
                    false // Skip chunks that are already explored? False helps find un-generated areas
            );

            if (targetPos != null) {
                // Create an empty filled map item stack container
                ItemStack mapStack = MapItem.create(
                        serverLevel,
                        targetPos.getX(),
                        targetPos.getZ(),
                        (byte) 2, // Scale layer factor (2 matches vanilla Explorer Maps zoom profile)
                        true, // Render tracking marker icons
                        true  // Render depth shading data
                );

                // Re-render and lock the tracking details to the physical item
                MapItem.renderBiomePreviewMap(serverLevel, mapStack);

                // Attach map target marker iconography (e.g., using RED_MARKER, TARGET_X, or custom types)
                MapItemSavedData.addTargetDecoration(
                        mapStack,
                        targetPos,
                        "+",
                        MapDecorationTypes.TARGET_X
                );

                // Give the map item instance a custom localized display name component
                mapStack.setHoverName(Component.translatable(this.displayNameKey));

                // Return the newly constructed merchant transaction card
                return new MerchantOffer(
                        new ItemCost(Items.EMERALD, 13), // Cost 1: 13 Emeralds
                        Optional.of(new ItemCost(Items.COMPASS, 1)), // Cost 2: 1 Compass
                        mapStack, // The finished customized map delivery output
                        12, // Maximum transaction usage cycles before restocking is forced
                        5,  // Base villager experience points rewarded on successful purchase
                        0.05F // Price modifier penalty scaling ratio
                );
            }

            // Return a safe fallback if no matching structure tag coordinates are found
            return new MerchantOffer(
                    new ItemCost(Items.EMERALD, 1),
                    new ItemStack(Items.MAP),
                    12, 1, 0.05F
            );
        }
    }
}
 */