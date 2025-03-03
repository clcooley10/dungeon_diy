package net.drDooley.dungeon_diy.dimension;

import net.drDooley.dungeon_diy.DDIY;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

public class DDIY_Dimensions {
    public static final ResourceKey<Level> DDIY_DIM = ResourceKey.create(Registry.DIMENSION_REGISTRY,
            new ResourceLocation(DDIY.MODID, "ddiy_dim"));
    public static final ResourceKey<DimensionType> DDIY_DIM_TYPE =
            ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY, DDIY_DIM.registry());

    public static void register() {
        System.out.println("Registering Dimensions for " + DDIY.MODID);
    }
}
