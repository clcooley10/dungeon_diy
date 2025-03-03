package net.drDooley.dungeon_diy.util;

import net.drDooley.dungeon_diy.dimension.DDIY_Dimensions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

// Trying to maintain good organization following DimDungeons
// Many methods are taken from there too
public class DDIY_Utils {
    public static boolean isDungeonDim(Level worldIn) {
        if (worldIn == null) {
            return false;
        }
        return worldIn.dimension().location().getPath().equals("ddiy_dim");
    }

    // TODO: This may or may not be accurate for me--> this is used by the personal key activating logic
    public static ServerLevel getDungeonDim(MinecraftServer server) {
        return server.getLevel(DDIY_Dimensions.DDIY_DIM);
    }

    // returns the limit of the dungeon space not in blocks, but in dungeon widths (which is BLOCKS_APART_PER_DUNGEON)
    // Size should absolutely not be an issue since the dungeons are 1 per player.
    public static long getLimitOfDungeonDim(MinecraftServer server) {
        ServerLevel world = getDungeonDim(server);
        double size = world.getWorldBorder().getSize() / 2;

        return Math.round(size);
    }

}
