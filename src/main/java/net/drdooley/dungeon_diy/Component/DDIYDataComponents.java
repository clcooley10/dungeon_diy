package net.drdooley.dungeon_diy.Component;

import net.drdooley.dungeon_diy.DungeonDIY;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.UUID;
import java.util.function.UnaryOperator;

public class DDIYDataComponents {
    public static final DeferredRegister<DataComponentType<?>> COMPONENTS =
      DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, DungeonDIY.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<UUID>> DUNGEON_ID = register("dungeon_id",
      builder -> builder.persistent(UUIDUtil.CODEC));

    private static <T>DeferredHolder<DataComponentType<?>, DataComponentType<T>> register(String name, UnaryOperator<DataComponentType.Builder<T>> builder) {
        return COMPONENTS.register(name, () -> builder.apply(DataComponentType.builder()).build());
    }

    public static void register(IEventBus eventBus) {
        COMPONENTS.register(eventBus);
    }
}
