package net.drdooley.dungeon_diy.Item;

import net.drdooley.dungeon_diy.DungeonDIY;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class DDIYItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(DungeonDIY.MOD_ID);

    public static final DeferredItem<Item> TATTERED_BOOK = ITEMS.registerSimpleItem("tattered_book");
    public static final DeferredItem<Item> ANCIENT_BOOK = ITEMS.registerSimpleItem("ancient_book");
    public static final DeferredItem<Item> DUNGEON_CODEX = ITEMS.registerSimpleItem("dungeon_codex");
    public static final DeferredItem<Item> BROKEN_COMPASS = ITEMS.register("broken_compass",
      () -> new BrokenCompassItem(new Item.Properties().stacksTo(1)));

    /* should work if above doesnt
    public static final DeferredItem<Item> TATTERED_BOOK = ITEMS.register("tattered_book",
      () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> ANCIENT_BOOK = ITEMS.register("ancient_book",
      () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> DUNGEON_CODEX = ITEMS.register("dungeon_codex",
      () -> new Item(new Item.Properties()));
     */

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
