package net.drDooley.dungeon_diy.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.drDooley.dungeon_diy.DDIY;
import net.drDooley.dungeon_diy.item.DDIY_Items;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.io.Serial;

public class CopyRulebookRecipe implements CraftingRecipe {
    private final ResourceLocation id;
    private final ItemStack output;
    private final NonNullList<Ingredient> recipeItems;


    public CopyRulebookRecipe(ResourceLocation id, ItemStack output, NonNullList<Ingredient> recipeItems) {
        this.id = id;
        this.output = output;
        this.recipeItems = recipeItems;
    }

    @Override
    public boolean matches(CraftingContainer pContainer, Level pLevel) {
        boolean hasRulebook = false;
        boolean hasBook = false;

        for (int i = 0; i < pContainer.getContainerSize(); i++) {
            ItemStack stack = pContainer.getItem(i);
            if (stack.isEmpty()) continue;
            if (stack.is(DDIY_Items.DUNGEON_RULEBOOK.get())) hasRulebook = true;
            else if (stack.is(Items.BOOK)) hasBook = true;
            else return false;
        }
        return hasRulebook && hasBook;
    }

    @Override
    public ItemStack assemble(CraftingContainer pContainer) {
        ItemStack ret = output.copy();
        for (int i = 0; i < pContainer.getContainerSize(); i++) {
            ItemStack stack = pContainer.getItem(i);
            if (stack.is(DDIY_Items.DUNGEON_RULEBOOK.get())) {
                if (stack.hasTag()) {
                    ret.setTag(stack.getTag().copy());
                } else {
                    DDIY.LOGGER.error("Error copying NBT- source rulebook has no Tag.");
                }
            }
        }

        return ret;
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return pWidth * pHeight >= 2;
    }

    @Override
    public ItemStack getResultItem() {
        return output.copy();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return recipeItems;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    public static class Serializer implements RecipeSerializer<CopyRulebookRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID = new ResourceLocation(DDIY.MODID, "copy_rulebook");

        @Override
        public CopyRulebookRecipe fromJson(ResourceLocation pRecipeId, JsonObject pSerializedRecipe) {
            JsonArray ingredients = GsonHelper.getAsJsonArray(pSerializedRecipe, "ingredients");
            NonNullList<Ingredient> inputs = NonNullList.withSize(ingredients.size(), Ingredient.EMPTY);
            for (int i = 0; i < inputs.size(); i++) {
                inputs.set(i, Ingredient.fromJson(ingredients.get(i)));
            }

            ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(pSerializedRecipe, "result"));
            return new CopyRulebookRecipe(pRecipeId, output, inputs);
        }

        @Override
        public @Nullable CopyRulebookRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
            NonNullList<Ingredient> inputs = NonNullList.withSize(pBuffer.readInt(), Ingredient.EMPTY);
            for (int i = 0; i < inputs.size(); i++) {
                inputs.set(i, Ingredient.fromNetwork(pBuffer));
            }

            ItemStack output = pBuffer.readItem();
            return new CopyRulebookRecipe(pRecipeId, output, inputs);
        }

        @Override
        public void toNetwork(FriendlyByteBuf pBuffer, CopyRulebookRecipe pRecipe) {
            pBuffer.writeInt(pRecipe.getIngredients().size());

            for (Ingredient ingredient : pRecipe.getIngredients()) {
                ingredient.toNetwork(pBuffer);
            }

            pBuffer.writeItemStack(pRecipe.getResultItem(), false);
        }
    }
}
