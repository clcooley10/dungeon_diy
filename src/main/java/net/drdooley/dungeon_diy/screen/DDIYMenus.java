package net.drdooley.dungeon_diy.screen;

import net.drdooley.dungeon_diy.DungeonDIY;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class DDIYMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, DungeonDIY.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<AncientVaultMenu>> ANCIENT_VAULT_MENU =
      registerMenuType("ancient_vault_menu", AncientVaultMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<DungeonCodexMenu>> DUNGEON_CODEX_MENU =
      registerMenuType("dungeon_codex_menu", DungeonCodexMenu::new);

    private static <T extends AbstractContainerMenu>DeferredHolder<MenuType<?>, MenuType<T>> registerMenuType(String name, IContainerFactory<T> factory) {
        return MENUS.register(name, () -> IMenuTypeExtension.create(factory));
    }

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
