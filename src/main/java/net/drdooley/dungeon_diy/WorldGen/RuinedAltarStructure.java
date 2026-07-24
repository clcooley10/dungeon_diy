package net.drdooley.dungeon_diy.WorldGen;

import net.drdooley.dungeon_diy.DungeonDIY;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pools.DimensionPadding;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;

import java.util.Optional;

public class RuinedAltarStructure extends Structure {
    private final Holder<StructureTemplatePool> startPool;
    private final Optional<ResourceLocation> startJigsawName;
    private final int size;
    private final Optional<Heightmap.Types> projectStartToHeightmap;
    private final int maxDistanceFromCenter;
    private final DimensionPadding dimensionPadding;
    private final LiquidSettings liquidSettings;

    public RuinedAltarStructure(StructureSettings config, Holder<StructureTemplatePool> startPool, Optional<ResourceLocation> startJigsawName, int size, HeightProvider startHeight, Optional<Heightmap.Types> projectStartToHeightmap, int maxDistanceFromCenter, DimensionPadding dimensionPadding, LiquidSettings liquidSettings) {
        super(config);
        this.startPool = startPool;
        this.startJigsawName = startJigsawName;
        this.size = size;
        this.projectStartToHeightmap = projectStartToHeightmap;
        this.maxDistanceFromCenter = maxDistanceFromCenter;
        this.dimensionPadding = dimensionPadding;
        this.liquidSettings = liquidSettings;
    }

    @Override
    public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();
        int x = chunkPos.getMiddleBlockX();
        int z = chunkPos.getMiddleBlockZ();
        int structure_y = 33;
        int offset = 6;

        // Fetch the column accessor for the chunk
        var randomState = context.randomState();
        var heightAccessor = context.heightAccessor();
        var generator = context.chunkGenerator();

        // Sample points to ensure the structure lands fully inside an open lava lake
        boolean bottomNorthIsLava  = generator.getBaseColumn(x, z - offset, heightAccessor, randomState).getBlock(structure_y - 1).is(Blocks.LAVA);
        boolean bottomSouthIsLava  = generator.getBaseColumn(x, z + offset, heightAccessor, randomState).getBlock(structure_y - 1).is(Blocks.LAVA);
        boolean bottomEastIsLava   = generator.getBaseColumn(x + offset, z, heightAccessor, randomState).getBlock(structure_y - 1).is(Blocks.LAVA);
        boolean bottomWestIsLava   = generator.getBaseColumn(x - offset, z, heightAccessor, randomState).getBlock(structure_y - 1).is(Blocks.LAVA);
        boolean topNorthIsLava  = generator.getBaseColumn(x, z - offset, heightAccessor, randomState).getBlock(structure_y + 3).is(Blocks.AIR);
        boolean topSouthIsLava  = generator.getBaseColumn(x, z + offset, heightAccessor, randomState).getBlock(structure_y + 3).is(Blocks.AIR);
        boolean topEastIsLava   = generator.getBaseColumn(x + offset, z, heightAccessor, randomState).getBlock(structure_y + 3).is(Blocks.AIR);
        boolean topWestIsLava   = generator.getBaseColumn(x - offset, z, heightAccessor, randomState).getBlock(structure_y + 3).is(Blocks.AIR);

        // Abort generation if any of the boundary points strike solid land or air instead of fluid
        if (!bottomNorthIsLava || !bottomSouthIsLava || !bottomEastIsLava || !bottomWestIsLava ||
          !topNorthIsLava || !topSouthIsLava || !topEastIsLava || !topWestIsLava) {
            return Optional.empty();
        }

        BlockPos blockPos = new BlockPos(x, structure_y, z);

        Optional<GenerationStub> structurePiecesGenerator =
          JigsawPlacement.addPieces(
            context, // Used for JigsawPlacement to get all the proper behaviors done.
            this.startPool, // The starting pool to use to create the structure layout from
            this.startJigsawName, // Can be used to only spawn from one Jigsaw block. But we don't need to worry about this.
            this.size, // How deep a branch of pieces can go away from center piece. (5 means branches cannot be longer than 5 pieces from center piece)
            blockPos, // Where to spawn the structure.
            false, // "useExpansionHack" This is for legacy villages to generate properly. You should keep this false always.
            Optional.empty(), // Adds the terrain height's y value to the passed in blockpos's y value. (This uses WORLD_SURFACE_WG heightmap which stops at top water too)
            // Here at projectStartToHeightmap, start_height's y value is -1 which means the structure spawn -1 blocks below terrain height if start_height and project_start_to_heightmap is defined in structure JSON.
            // Set projectStartToHeightmap to be empty optional for structure to be place only at the passed in blockpos's Y value instead.
            // Definitely keep this an empty optional when placing structures in the nether as otherwise, heightmap placing will put the structure on the Bedrock roof.
            this.maxDistanceFromCenter, // Maximum limit for how far pieces can spawn from center. You cannot set the horizontal part bigger than 128 or else pieces gets cutoff. Vertical is limited to dimension height.
            PoolAliasLookup.EMPTY, // Optional thing that allows swapping a template pool with another per structure json instance. We don't need this but see vanilla JigsawStructure class for how to wire it up if you want it.
            this.dimensionPadding, // Optional thing to prevent generating too close to the bottom or top of the dimension.
            this.liquidSettings); // Optional thing to control whether the structure will be waterlogged when replacing pre-existing water in the world.

        // 4. Return the pieces generator that is now set up so that the game runs it when it needs to create the layout of structure pieces.
        return structurePiecesGenerator;
    }

    @Override
    public StructureType<?> type() {
        return DDIYWorldGen.RUINED_ALTAR_STRUCTURE.get();
    }
}
