/*
 * This file is part of Titanium
 * Copyright (C) 2024, Horizon Studio <contact@hrznstudio.com>.
 *
 * This code is licensed under GNU Lesser General Public License v3.0, the full license text can be found in LICENSE.txt
 */

package com.hrznstudio.titanium.recipe.shapelessenchant;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.Map;

public class ShapelessEnchantSerializer extends ShapelessRecipe.Serializer {
    private final Codec<ShapelessRecipe> codec = Codec.PASSTHROUGH.flatXmap(
        dynamic -> {
            try {
                return DataResult.success(fromJson(dynamic.cast(JsonOps.INSTANCE).getAsJsonObject()));
            } catch (Exception exception) {
                return DataResult.error(exception::getMessage);
            }
        },
        shapelessRecipe -> DataResult.error(() -> "Cannot encode")
    );

    @Override
    public Codec<ShapelessRecipe> codec() {
        return codec;
    }

    @Nonnull
    public ShapelessRecipe fromJson(JsonObject json) {
        String s = GsonHelper.getAsString(json, "group", "");
        NonNullList<Ingredient> ingredients = readIngredients(GsonHelper.getAsJsonArray(json, "ingredients"));
        if (ingredients.isEmpty()) {
            throw new JsonParseException("No ingredients for shapeless recipe");
        } else if (ingredients.size() > 9) {
            throw new JsonParseException("Too many ingredients for shapeless recipe the max is " + 9);
        } else {
            JsonObject jsonObject = GsonHelper.getAsJsonObject(json, "result");
            ItemStack itemstack = ItemStack.ITEM_WITH_COUNT_CODEC.parse(JsonOps.INSTANCE, jsonObject).getOrThrow(false, err -> {});
            if (jsonObject.has("enchantments")) {
                JsonElement enchantments = jsonObject.get("enchantments");
                Map<Enchantment, Integer> enchantmentLevelMap = Maps.newHashMap();
                if (enchantments.isJsonArray()) {
                    for (JsonElement jsonElement: enchantments.getAsJsonArray()) {
                        if (jsonElement.isJsonObject()) {
                            Pair<Enchantment, Integer> enchantmentLevelPair = parseJson(jsonElement.getAsJsonObject());
                            enchantmentLevelMap.put(enchantmentLevelPair.getKey(), enchantmentLevelPair.getValue());
                        }
                    }
                } else if (enchantments.isJsonObject()) {
                    Pair<Enchantment, Integer> enchantmentLevelPair = parseJson(enchantments.getAsJsonObject());
                    enchantmentLevelMap.put(enchantmentLevelPair.getKey(), enchantmentLevelPair.getValue());
                }
                EnchantmentHelper.setEnchantments(enchantmentLevelMap, itemstack);
            } else {
                throw new JsonParseException("No String or Array found for enchantments");
            }
            return new ShapelessRecipe(s, CraftingBookCategory.MISC, itemstack, ingredients);
        }
    }

    private static Pair<Enchantment, Integer> parseJson(JsonObject jsonObject) {
        String name = GsonHelper.getAsString(jsonObject, "name");
        Enchantment enchantment = BuiltInRegistries.ENCHANTMENT.get(new ResourceLocation(name));
        if (enchantment == null) {
            throw new JsonParseException("Failed to find enchantment named: " + name);
        }
        return Pair.of(enchantment, GsonHelper.getAsInt(jsonObject, "level", 1));
    }

    private static NonNullList<Ingredient> readIngredients(JsonArray ingredientArray) {
        NonNullList<Ingredient> ingredients = NonNullList.create();

        for(int i = 0; i < ingredientArray.size(); ++i) {
            Ingredient ingredient = Ingredient.fromJson(ingredientArray.get(i),false);
            if (!ingredient.isEmpty()) {
                ingredients.add(ingredient);
            }
        }

        return ingredients;
    }
}
