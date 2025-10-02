package net.drDooley.dungeon_diy.item;

import net.drDooley.dungeon_diy.DDIY;
import net.drDooley.dungeon_diy.unused.OverloadedEye;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class DDIY_Items {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, DDIY.MODID);

    public static final RegistryObject<Item> DUNGEON_RULEBOOK = ITEMS.register("dungeon_rulebook",
            () -> new DungeonRulebook(new Item.Properties().rarity(Rarity.COMMON).tab(DDIY_CreativeTab.DDIY_CREATIVE)));


    public static final RegistryObject<Item> OVERLOADED_EYE = ITEMS.register("overloaded_eye",
            () -> new OverloadedEye(new Item.Properties().rarity(Rarity.COMMON).tab(DDIY_CreativeTab.DDIY_CREATIVE)));

    public static final RegistryObject<Item> REACTIVE_EYE = ITEMS.register("reactive_eye",
            () -> new ReactiveEye(new Item.Properties().rarity(Rarity.COMMON).stacksTo(1).tab(DDIY_CreativeTab.DDIY_CREATIVE)));

    public static final RegistryObject<Item> UNSTABLE_EYE = ITEMS.register("unstable_eye",
            () -> new UnstableEye(new Item.Properties().rarity(Rarity.UNCOMMON).stacksTo(1).tab(DDIY_CreativeTab.DDIY_CREATIVE)));


    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
