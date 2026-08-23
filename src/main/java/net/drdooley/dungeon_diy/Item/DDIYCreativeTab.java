package net.drdooley.dungeon_diy.Item;

import net.drdooley.dungeon_diy.Block.DDIYBlocks;
import net.drdooley.dungeon_diy.DungeonDIY;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class DDIYCreativeTab {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, DungeonDIY.MOD_ID);
    public static final Supplier<CreativeModeTab> DDIY_TAB = CREATIVE_MODE_TAB.register("ddiy_creative_tab",
      () -> CreativeModeTab.builder()
        .icon(() -> new ItemStack(DDIYItems.ANCIENT_BOOK.get()))
        .title(Component.translatable("creativetab.dungeon_diy.creative_tab"))
        .displayItems((itemDisplayParameters, output) -> {
            output.accept(DDIYItems.TATTERED_BOOK);
            output.accept(DDIYItems.ANCIENT_BOOK);
            output.accept(DDIYItems.DUNGEON_CODEX);
            output.accept(DDIYItems.BROKEN_COMPASS);
            output.accept(DDIYBlocks.ANCIENT_VAULT);
            output.accept(DDIYBlocks.RUINED_ALTAR);
            output.accept(DDIYBlocks.ANCIENT_PEDESTAL);
        }).build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
