package net.drDooley.dungeon_diy;

import com.mojang.logging.LogUtils;
import net.drDooley.dungeon_diy.block.DDIY_Blocks;
import net.drDooley.dungeon_diy.client.render.LootChestRenderer;
import net.drDooley.dungeon_diy.dimension.DDIY_Dimensions;
import net.drDooley.dungeon_diy.item.DDIY_Items;
import net.drDooley.dungeon_diy.networking.DDIY_Packets;
import net.drDooley.dungeon_diy.screen.*;
import net.drDooley.dungeon_diy.unused.LayoutBlockScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(DDIY.MODID)
public class DDIY {
    public static final String MODID = "dungeon_diy";
    public static final Logger LOGGER = LogUtils.getLogger();

    public DDIY(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        modEventBus.addListener(this::commonSetup);

        DDIY_Blocks.register(modEventBus);
        DDIY_Dimensions.register();
        DDIY_Items.register(modEventBus);
        DDIY_Menus.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);
        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            DDIY_Packets.register();

            BrewingRecipeRegistry.addRecipe(Ingredient.of(Items.DIAMOND_AXE), Ingredient.of(DDIY_Items.OVERLOADED_EYE.get()), new ItemStack(Items.NETHERITE_AXE));
        });
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            MenuScreens.register(DDIY_Menus.DUNGEON_CONFIG_MENU.get(), DungeonConfigScreen::new);

            MenuScreens.register(DDIY_Menus.LAYOUT_BLOCK_MENU.get(), LayoutBlockScreen::new);
            MenuScreens.register(DDIY_Menus.LOOT_CHEST_EDIT_MENU.get(), LootChestEditScreen::new);
            MenuScreens.register(DDIY_Menus.LOOT_CHEST_ACTIVE_MENU.get(), LootChestActiveScreen::new);
            MenuScreens.register(DDIY_Menus.REQUIREMENT_DOOR_EDIT_MENU.get(), RequirementDoorEditScreen::new);
            MenuScreens.register(DDIY_Menus.REQUIREMENT_DOOR_ACTIVE_MENU.get(), RequirementDoorActiveScreen::new);

            //BlockEntityRenderers.register(DDIY_Blocks.LOOT_CHEST_ENTITY.get(), LootChestRenderer::new);
        }
    }
}
