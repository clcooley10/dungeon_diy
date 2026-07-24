package net.drdooley.dungeon_diy.WorldGen;

import net.drdooley.dungeon_diy.DungeonDIY;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Map;
import java.util.function.Supplier;

public class DDIYWorldGen {

    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES = DeferredRegister.create(Registries.STRUCTURE_TYPE, DungeonDIY.MOD_ID);
    public static final DeferredRegister<Structure> STRUCTURES = DeferredRegister.create(Registries.STRUCTURE, DungeonDIY.MOD_ID);

    //public static final Supplier<StructureType<JigsawStructure>> RUINED_ALTAR_STRUCTURE_TYPE = STRUCTURE_TYPES.register("ruined_altar_structure", () -> () -> JigsawStructure.CODEC);
    public static final DeferredHolder<StructureType<?>, StructureType<JigsawStructure>> RUINED_ALTAR_STRUCTURE = STRUCTURE_TYPES.register("ruined_altar_structure", () -> () -> JigsawStructure.CODEC);
}
