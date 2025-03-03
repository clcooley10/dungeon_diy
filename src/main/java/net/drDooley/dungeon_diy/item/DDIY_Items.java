package net.drDooley.dungeon_diy.item;

import net.drDooley.dungeon_diy.DDIY;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class DDIY_Items {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, DDIY.MODID);

    public static final RegistryObject<Item> OVERLOADED_EYE = ITEMS.register("overloaded_eye",
            () -> new OverloadedEye(new Item.Properties().rarity(Rarity.COMMON).tab(DDIY_CreativeTab.DDIY_CREATIVE)));


    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
