package net.drdooley.dungeon_diy.Block;

import net.drdooley.dungeon_diy.DungeonDIY;
import net.drdooley.dungeon_diy.Item.DDIYItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class DDIYBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(DungeonDIY.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
      DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, DungeonDIY.MOD_ID);

    public static final DeferredBlock<Block> ANCIENT_VAULT = registerBlock("ancient_vault",
      () -> new AncientVaultBlock(BlockBehaviour.Properties.of().noOcclusion()));

    public static final DeferredBlock<Block> RUINED_ALTAR = registerBlock("ruined_altar",
      () -> new RuinedAltarBlock(BlockBehaviour.Properties.of().noOcclusion()));

    public static final DeferredBlock<Block> ANCIENT_PEDESTAL = registerBlock("ancient_pedestal",
      () -> new AncientPedestalBlock(BlockBehaviour.Properties.of().noOcclusion()));


    public static final Supplier<BlockEntityType<AncientVaultBlockEntity>> ANCIENT_VAULT_BE =
      BLOCK_ENTITIES.register("ancient_vault_be", () -> BlockEntityType.Builder.of(
        AncientVaultBlockEntity::new, ANCIENT_VAULT.get()).build(null));

    public static final Supplier<BlockEntityType<AncientPedestalBlockEntity>> ANCIENT_PEDESTAL_BE =
      BLOCK_ENTITIES.register("ancient_pedestal_be", () -> BlockEntityType.Builder.of(
        AncientPedestalBlockEntity::new, ANCIENT_PEDESTAL.get()).build(null));

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        DDIYItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        BLOCK_ENTITIES.register(bus);
    }
}
