package net.drdooley.dungeon_diy.Event;

import net.drdooley.dungeon_diy.DungeonDIY;
import net.drdooley.dungeon_diy.Item.DDIYItems;
import net.minecraft.core.*;
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
                  10, 15, 0.0f
                ));
            }
        }
        if (event.getType() == VillagerProfession.CARTOGRAPHER) {
            List<VillagerTrades.ItemListing> masterCartTrades = event.getTrades().get(5);
            masterCartTrades.add((entity, randomsource) -> new MerchantOffer(
              new ItemCost(Items.EMERALD, 32),
              Optional.of(new ItemCost(Items.COMPASS, 1)),
              new ItemStack(DDIYItems.BROKEN_COMPASS.get(), 1),
              10, 15, 0.0f
            ));
        }
    }
}
