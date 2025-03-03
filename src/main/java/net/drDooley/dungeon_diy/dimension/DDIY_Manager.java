package net.drDooley.dungeon_diy.dimension;

import net.drDooley.dungeon_diy.DDIY;
import net.drDooley.dungeon_diy.util.DDIY_Utils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// McJTY tutorial and DimDungeons
public class DDIY_Manager extends SavedData {
    public static final int BLOCKS_APART_PER_DUNGEON = 16 * 16;
    public class PlayerData {
        UUID uuid;
        String playerName;

        PlayerData(Player player) {
            uuid = player.getUUID();
            playerName = player.getName().getString();
        }

        PlayerData(UUID id, String name) {
            uuid = id;
            playerName = name;
        }
    };

    private ConcurrentHashMap<ChunkPos, PlayerData> playerMap = new ConcurrentHashMap<>();
    private static String DATA = "ddiy_data";

    public static DDIY_Manager get(Level level) {
        if (level.isClientSide) {
            throw new RuntimeException("Manager doesn't exist on client");
        }
        if (!DDIY_Utils.isDungeonDim(level)) {
            throw new RuntimeException("Manager doesn't exist in dimensions other than 'ddiy_dim'");
        }

        DimensionDataStorage storage = ((ServerLevel) level).getDataStorage();
        return storage.computeIfAbsent(DDIY_Manager::new, DDIY_Manager::new, DATA);
    }

    public DDIY_Manager() { }

    public DDIY_Manager(CompoundTag nbt) {
        ListTag allPlayers = nbt.getList("player_data", nbt.getId());

        for (net.minecraft.nbt.Tag t : allPlayers) {
            CompoundTag playerTag = (CompoundTag) t;
            ChunkPos pos = new ChunkPos(playerTag.getInt("x"), playerTag.getInt("z"));
            UUID id = UUID.fromString(playerTag.getString("uuid"));
            String name = playerTag.getString("name");
            PlayerData player = new PlayerData(id, name);

            playerMap.put(pos, player);
        }
    }

    public PlayerData getOwnerAtPos(ChunkPos pos) {
        return playerMap.getOrDefault(pos, null);
    }

    // ripped from dimdungeon :/
    // returns a new or existing plot for this player
    // note that the return value is not a ChunkPos, but a [dest_x, dest_z] pair
    // that is consistent with other key types
    public ChunkPos getPosForOwner(LivingEntity player) {
        Iterator<ChunkPos> iter = playerMap.keySet().iterator();
        ChunkPos cpos;

        // sanity check. The parameter to this function should always be a Player except
        // when debugging.
        if (player == null) {
            DDIY.LOGGER.info("DUNGEON DIY ERROR: Player not found when registering dungeon");
            return null;
        }

        while (iter.hasNext()) {
            cpos = iter.next();
            PlayerData nextOwner = playerMap.get(cpos);

            // this player has an existing plot
            if (nextOwner.uuid.equals(player.getUUID())) {
                //DimDungeons.logMessageInfo("DIMENSIONAL DUNGEONS: Found existing build plot for player " + player.getName().getString() + " at (" + cpos.x + ", " + cpos.z + ")");
                return cpos;
            }
        }

        // pick the next available plot and register it now
        cpos = getNewChunkPos(playerMap.size() + 1, player.getServer());
        //DimDungeons.logMessageInfo("DIMENSIONAL DUNGEONS: Assigning player " + player.getName().getString() + " the build plot at (" + cpos.x + ", " + cpos.z + ")");
        PlayerData newOwner = new PlayerData(player.getUUID(), player.getName().getString());
        playerMap.computeIfAbsent(cpos, cp -> newOwner);
        setDirty();
        return cpos;
    }

    protected ChunkPos getNewChunkPos(int numOtherPlayers, MinecraftServer server) {
        // where is this key going?
        long generation_limit = DDIY_Utils.getLimitOfDungeonDim(server);
        int plotsPerLimit = (int) (generation_limit / BLOCKS_APART_PER_DUNGEON);

        // go as far as possible on the z-axis, then the x-axis, staying in the positive
        // x/z quadrant
        // Above comment is wrong? This is going across the x-axis first, right?
        int destZ = numOtherPlayers / plotsPerLimit;
        int destX = numOtherPlayers % plotsPerLimit;

        // warning: not actually a ChunkPos, but it is consistent with other keys use of
        // dest_x and dest_z
        return new ChunkPos(destX, destZ);
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
        ListTag allPlayers = new ListTag();
        playerMap.forEach(((chunkPos, playerData) -> {
            CompoundTag playerTag = new CompoundTag();
            playerTag.putInt("x", chunkPos.x);
            playerTag.putInt("z", chunkPos.z);
            playerTag.putString("uuid", playerData.uuid.toString());
            playerTag.putString("name", playerData.playerName);
            allPlayers.add(playerTag);
        }));
        nbt.put("player_data", allPlayers);
        return nbt;
    }
}
