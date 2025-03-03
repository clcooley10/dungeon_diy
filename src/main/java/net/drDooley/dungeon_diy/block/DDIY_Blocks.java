package net.drDooley.dungeon_diy.block;

import net.drDooley.dungeon_diy.DDIY;
import net.drDooley.dungeon_diy.item.DDIY_CreativeTab;
import net.drDooley.dungeon_diy.item.DDIY_Items;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class DDIY_Blocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, DDIY.MODID);

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, DDIY.MODID);

    // Blocks
    public static final RegistryObject<Block> DRAFTING_TABLE_OLD = registerBlock("drafting_table_old",
            () -> new DraftingTableBlockOld(BlockBehaviour.Properties.of(Material.WOOD).strength(2.5F).sound(SoundType.WOOD).noOcclusion()), DDIY_CreativeTab.DDIY_CREATIVE);

    public static final RegistryObject<Block> DRAFTING_TABLE = registerBlock("drafting_table",
            () -> new DraftingTableBlock(BlockBehaviour.Properties.of(Material.STONE).noOcclusion()), DDIY_CreativeTab.DDIY_CREATIVE);
    public static final RegistryObject<Block> LAYOUT_BLOCK_BASE = registerBlock("layout_block_base",
            () -> new Block(BlockBehaviour.Properties.of(Material.STONE, DyeColor.WHITE).strength(1.8F)), null);
    public static final RegistryObject<Block> LAYOUT_BLOCK_WHITE = registerBlock("layout_block_white",
            () -> new LayoutBaseBlock(BlockBehaviour.Properties.of(Material.STONE, DyeColor.WHITE)), DDIY_CreativeTab.DDIY_CREATIVE);
    public static final RegistryObject<Block> LAYOUT_BLOCK_ORANGE = registerBlock("layout_block_orange",
            () -> new LayoutBaseBlock(BlockBehaviour.Properties.of(Material.STONE, DyeColor.ORANGE)), DDIY_CreativeTab.DDIY_CREATIVE);
    public static final RegistryObject<Block> LAYOUT_BLOCK_MAGENTA = registerBlock("layout_block_magenta",
            () -> new LayoutBaseBlock(BlockBehaviour.Properties.of(Material.STONE, DyeColor.MAGENTA)), DDIY_CreativeTab.DDIY_CREATIVE);
    public static final RegistryObject<Block> LAYOUT_BLOCK_LIGHT_BLUE = registerBlock("layout_block_light_blue",
            () -> new LayoutBaseBlock(BlockBehaviour.Properties.of(Material.STONE, DyeColor.LIGHT_BLUE)), DDIY_CreativeTab.DDIY_CREATIVE);
    public static final RegistryObject<Block> LAYOUT_BLOCK_YELLOW = registerBlock("layout_block_yellow",
            () -> new LayoutBaseBlock(BlockBehaviour.Properties.of(Material.STONE, DyeColor.YELLOW)), DDIY_CreativeTab.DDIY_CREATIVE);
    public static final RegistryObject<Block> LAYOUT_BLOCK_LIME = registerBlock("layout_block_lime",
            () -> new LayoutBaseBlock(BlockBehaviour.Properties.of(Material.STONE, DyeColor.LIME)), DDIY_CreativeTab.DDIY_CREATIVE);
    public static final RegistryObject<Block> LAYOUT_BLOCK_PINK = registerBlock("layout_block_pink",
            () -> new LayoutBaseBlock(BlockBehaviour.Properties.of(Material.STONE, DyeColor.PINK)), DDIY_CreativeTab.DDIY_CREATIVE);
    public static final RegistryObject<Block> LAYOUT_BLOCK_GRAY = registerBlock("layout_block_gray",
            () -> new LayoutBaseBlock(BlockBehaviour.Properties.of(Material.STONE, DyeColor.GRAY)), DDIY_CreativeTab.DDIY_CREATIVE);
    public static final RegistryObject<Block> LAYOUT_BLOCK_LIGHT_GRAY = registerBlock("layout_block_light_gray",
            () -> new LayoutBaseBlock(BlockBehaviour.Properties.of(Material.STONE, DyeColor.LIGHT_GRAY)), DDIY_CreativeTab.DDIY_CREATIVE);
    public static final RegistryObject<Block> LAYOUT_BLOCK_CYAN = registerBlock("layout_block_cyan",
            () -> new LayoutBaseBlock(BlockBehaviour.Properties.of(Material.STONE, DyeColor.CYAN)), DDIY_CreativeTab.DDIY_CREATIVE);
    public static final RegistryObject<Block> LAYOUT_BLOCK_PURPLE = registerBlock("layout_block_purple",
            () -> new LayoutBaseBlock(BlockBehaviour.Properties.of(Material.STONE, DyeColor.PURPLE)), DDIY_CreativeTab.DDIY_CREATIVE);
    public static final RegistryObject<Block> LAYOUT_BLOCK_BLUE = registerBlock("layout_block_blue",
            () -> new LayoutBaseBlock(BlockBehaviour.Properties.of(Material.STONE, DyeColor.BLUE)), DDIY_CreativeTab.DDIY_CREATIVE);
    public static final RegistryObject<Block> LAYOUT_BLOCK_BROWN = registerBlock("layout_block_brown",
            () -> new LayoutBaseBlock(BlockBehaviour.Properties.of(Material.STONE, DyeColor.BROWN)), DDIY_CreativeTab.DDIY_CREATIVE);
    public static final RegistryObject<Block> LAYOUT_BLOCK_GREEN = registerBlock("layout_block_green",
            () -> new LayoutBaseBlock(BlockBehaviour.Properties.of(Material.STONE, DyeColor.GREEN)), DDIY_CreativeTab.DDIY_CREATIVE);
    public static final RegistryObject<Block> LAYOUT_BLOCK_RED = registerBlock("layout_block_red",
            () -> new LayoutBaseBlock(BlockBehaviour.Properties.of(Material.STONE, DyeColor.RED)), DDIY_CreativeTab.DDIY_CREATIVE);
    public static final RegistryObject<Block> LAYOUT_BLOCK_BLACK = registerBlock("layout_block_black",
            () -> new LayoutBaseBlock(BlockBehaviour.Properties.of(Material.STONE, DyeColor.BLACK)), DDIY_CreativeTab.DDIY_CREATIVE);
    public static final RegistryObject<Block> LAYOUT_BLOCK_TELEPORTER = registerBlock("layout_block_teleporter",
            () -> new LayoutTeleporterBlock(BlockBehaviour.Properties.of(Material.STONE)), DDIY_CreativeTab.DDIY_CREATIVE);

    public static final RegistryObject<Block> DUNGEON_TELEPORTER = registerBlock("dungeon_teleporter",
            () -> new DungeonTeleporterBlock(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.COLOR_BLACK)
                    .strength(50.0f, 1200f).requiresCorrectToolForDrops()
                    .lightLevel((s) -> { return 10; }).noOcclusion()), DDIY_CreativeTab.DDIY_CREATIVE);

    public static final RegistryObject<Block> DUNGEON_TELEPORTER_OLD = registerBlock("dungeon_teleporter_old",
            () -> new DungeonTeleporterBlock_old(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.COLOR_BLACK)
                    .strength(50.0f, 1200f).requiresCorrectToolForDrops()
                    .lightLevel((s) -> { return 10; })), DDIY_CreativeTab.DDIY_CREATIVE);

    // BlockEntities
    public static final RegistryObject<BlockEntityType<DraftingTableEntityOld>> DRAFTING_TABLE_ENTITY_OLD = BLOCK_ENTITIES.register("drafting_table_entity_old",
            () -> BlockEntityType.Builder.of(DraftingTableEntityOld::new, DRAFTING_TABLE_OLD.get()).build(null));

    public static final RegistryObject<BlockEntityType<DungeonTeleporterEntity>> DUNGEON_TELEPORTER_ENTITY = BLOCK_ENTITIES.register("dungeon_teleporter_entity",
            () -> BlockEntityType.Builder.of(DungeonTeleporterEntity::new, DUNGEON_TELEPORTER.get()).build(null));

    public static final RegistryObject<BlockEntityType<DraftingTableEntity>> DRAFTING_TABLE_ENTITY = BLOCK_ENTITIES.register("drafting_table_entity",
            () -> BlockEntityType.Builder.of(DraftingTableEntity::new, DRAFTING_TABLE.get()).build(null));

    // Registration
    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block, CreativeModeTab tab) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn, tab);
        return toReturn;
    }
    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block, CreativeModeTab tab) {
        if (tab == null) {
            return DDIY_Items.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
        }
        return DDIY_Items.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(tab)));
    }
    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        BLOCK_ENTITIES.register(bus);
    }
}
