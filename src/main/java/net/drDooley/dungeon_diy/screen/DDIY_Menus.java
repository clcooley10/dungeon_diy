package net.drDooley.dungeon_diy.screen;

import net.drDooley.dungeon_diy.DDIY;
import net.drDooley.dungeon_diy.unused.LayoutBlockMenu;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class DDIY_Menus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, DDIY.MODID);

    public static final RegistryObject<MenuType<DungeonConfigMenu>> DUNGEON_CONFIG_MENU =
            registerMenuType(DungeonConfigMenu::new, "dungeon_config_menu");


    public static final RegistryObject<MenuType<LayoutBlockMenu>> LAYOUT_BLOCK_MENU =
            registerMenuType(LayoutBlockMenu::new, "layout_block_menu");

    public static final RegistryObject<MenuType<LootChestEditMenu>> LOOT_CHEST_EDIT_MENU =
            registerMenuType(LootChestEditMenu::new, "loot_chest_edit_menu");
    public static final RegistryObject<MenuType<LootChestActiveMenu>> LOOT_CHEST_ACTIVE_MENU =
            registerMenuType(LootChestActiveMenu::new, "loot_chest_active_menu");

    public static final RegistryObject<MenuType<RequirementDoorEditMenu>> REQUIREMENT_DOOR_EDIT_MENU =
            registerMenuType(RequirementDoorEditMenu::new, "requirement_door_edit_menu");
    public static final RegistryObject<MenuType<RequirementDoorActiveMenu>> REQUIREMENT_DOOR_ACTIVE_MENU =
            registerMenuType(RequirementDoorActiveMenu::new, "requirement_door_active_menu");

    private static <T extends AbstractContainerMenu>RegistryObject<MenuType<T>> registerMenuType(IContainerFactory<T> factory, String name) {
        return MENUS.register(name, () -> IForgeMenuType.create(factory));
    }
    public static void register(IEventBus bus) {
        MENUS.register(bus);
    }

}
