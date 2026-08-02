package net.drdooley.dungeon_diy.Dungeon;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class ReplacementEntry {
    private BlockState state;
    private int weight;

    public ReplacementEntry(BlockState state, int weight) {
        this.state = state;
        this.weight = weight;
    }

    public BlockState getState() {
        return state;
    }
    public int getWeight() {
        return weight;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.put("BlockState", NbtUtils.writeBlockState(state));
        tag.putInt("Weight", weight);
        return tag;
    }

    public static ReplacementEntry load(CompoundTag tag, HolderLookup.Provider registries) {
        HolderGetter<Block> blocks = registries.lookupOrThrow(Registries.BLOCK);
        BlockState state = NbtUtils.readBlockState(blocks, tag.getCompound("BlockState"));
        int weight = tag.getInt("Weight");
        return new ReplacementEntry(state, weight);
    }

    public void writeNetworkData(FriendlyByteBuf buf) {
        buf.writeUtf(BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString());
        buf.writeInt(weight);
    }


    public static ReplacementEntry readNetworkData(FriendlyByteBuf buf) {
        ResourceLocation id = ResourceLocation.parse(buf.readUtf());
        Block block = BuiltInRegistries.BLOCK.get(id);
        BlockState state = block.defaultBlockState();
        int weight = buf.readInt();
        return new ReplacementEntry(state, weight);
    }
}
