package net.drDooley.dungeon_diy.recipe;

import net.drDooley.dungeon_diy.DDIY;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class DDIY_Recipes {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, DDIY.MODID);

    public static final RegistryObject<RecipeSerializer<CopyRulebookRecipe>> COPY_RULEBOOK =
            RECIPE_SERIALIZERS.register("copy_rulebook", () -> CopyRulebookRecipe.Serializer.INSTANCE);

    public static void register(IEventBus bus) {
        RECIPE_SERIALIZERS.register(bus);
    }
}
