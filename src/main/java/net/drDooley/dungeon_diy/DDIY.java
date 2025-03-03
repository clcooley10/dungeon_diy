package net.drDooley.dungeon_diy;

import com.mojang.logging.LogUtils;
import net.drDooley.dungeon_diy.block.DDIY_Blocks;
import net.drDooley.dungeon_diy.dimension.DDIY_Dimensions;
import net.drDooley.dungeon_diy.item.DDIY_Items;
import net.drDooley.dungeon_diy.networking.DDIY_Packets;
import net.drDooley.dungeon_diy.screen.DDIY_Menus;
import net.drDooley.dungeon_diy.screen.LayoutBlockScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
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
        });
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            MenuScreens.register(DDIY_Menus.LAYOUT_BLOCK_MENU.get(), LayoutBlockScreen::new);
        }
    }
}
