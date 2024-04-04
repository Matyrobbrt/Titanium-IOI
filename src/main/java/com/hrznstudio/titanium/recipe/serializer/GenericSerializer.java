/*
 * This file is part of Titanium
 * Copyright (C) 2024, Horizon Studio <contact@hrznstudio.com>.
 *
 * This code is licensed under GNU Lesser General Public License v3.0, the full license text can be found in LICENSE.txt
 */

package com.hrznstudio.titanium.recipe.serializer;

import com.hrznstudio.titanium.Titanium;
import com.hrznstudio.titanium.network.CompoundSerializableDataHandler;
import com.mojang.serialization.Codec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.reflect.Field;
import java.util.function.Supplier;

public class GenericSerializer<T extends Recipe<?>> implements RecipeSerializer<T> {
    private final Class<T> recipeClass;
    private final Supplier<RecipeType<?>> recipeTypeSupplier;
    private final Codec<T> codec;

    public GenericSerializer(Class<T> recipeClass, Supplier<RecipeType<?>> recipeTypeSupplier, Codec<T> codec) {
        this.recipeClass = recipeClass;
        this.recipeTypeSupplier = recipeTypeSupplier;
        this.codec = codec;
    }

    // Reading from a packet buffer
    @Override
    @ParametersAreNonnullByDefault
    public T fromNetwork(FriendlyByteBuf buffer) {
        try {
            T recipe = recipeClass.getConstructor().newInstance();
            for (Field field : recipeClass.getFields()) {
                if (CompoundSerializableDataHandler.acceptField(field, field.getType())) {
                    CompoundSerializableDataHandler.readField(field, field.getType(), buffer, recipe);
                }
            }
            return recipe;
        } catch (Exception e) {
            Titanium.LOGGER.catching(e);
        }
        return null;
    }

    // Writes a recipe to a buffer
    @Override
    @ParametersAreNonnullByDefault
    public void toNetwork(FriendlyByteBuf buffer, T recipe) {
        try {
            for (Field field : recipeClass.getFields()) {
                if (CompoundSerializableDataHandler.acceptField(field, field.getType())) {
                    CompoundSerializableDataHandler.writeField(field, field.getType(), buffer, recipe);
                }
            }
        } catch (Exception e) {
            Titanium.LOGGER.catching(e);
        }
    }

    @Override
    public Codec<T> codec() {
        return codec;
    }
}
