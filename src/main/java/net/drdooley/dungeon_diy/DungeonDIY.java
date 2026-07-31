package net.drdooley.dungeon_diy;

import net.drdooley.dungeon_diy.Block.DDIYBlocks;
import net.drdooley.dungeon_diy.Component.DDIYDataComponents;
import net.drdooley.dungeon_diy.Item.DDIYCreativeTab;
import net.drdooley.dungeon_diy.Item.DDIYItems;
import net.drdooley.dungeon_diy.WorldGen.DDIYWorldGen;
import net.drdooley.dungeon_diy.screen.AncientVaultScreen;
import net.drdooley.dungeon_diy.screen.DDIYMenus;
import net.drdooley.dungeon_diy.screen.DungeonCodexScreen;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(DungeonDIY.MOD_ID)
public class DungeonDIY {
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "dungeon_diy";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public DungeonDIY(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        NeoForge.EVENT_BUS.register(this);

        DDIYCreativeTab.register(modEventBus);

        DDIYItems.register(modEventBus);
        DDIYBlocks.register(modEventBus);
        DDIYWorldGen.STRUCTURE_TYPES.register(modEventBus);
        DDIYDataComponents.register(modEventBus);
        DDIYMenus.register(modEventBus);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);
        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
//        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
//            event.accept(DDIYItems.TATTERED_BOOK);
//            event.accept(DDIYItems.ANCIENT_BOOK);
//            event.accept(DDIYItems.DUNGEON_CODEX);
//            event.accept(DDIYBlocks.ANCIENT_VAULT);
//            event.accept(DDIYBlocks.RUINED_ALTAR);
//        }
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void registerScreens(RegisterMenuScreensEvent event) {
            event.register(DDIYMenus.ANCIENT_VAULT_MENU.get(), AncientVaultScreen::new);
            event.register(DDIYMenus.DUNGEON_CODEX_MENU.get(), DungeonCodexScreen::new);
        }
    }
}
