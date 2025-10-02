package net.drDooley.dungeon_diy.unused.pool;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EntityPoolsProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static Capability<EntityPools> ENTITY_POOLS = CapabilityManager.get(new CapabilityToken<EntityPools>() { });
    private EntityPools entityPools = null;
    private final LazyOptional<EntityPools> optional = LazyOptional.of(this::createPools);

    private EntityPools createPools() {
        if (this.entityPools == null) {
            this.entityPools = new EntityPools();
        }
        return this.entityPools;
    }


    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ENTITY_POOLS) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag pTag = new CompoundTag();
        createPools().saveNBTData(pTag);
        return pTag;
    }

    @Override
    public void deserializeNBT(CompoundTag pTag) {
        createPools().loadNBTData(pTag);
    }
}
