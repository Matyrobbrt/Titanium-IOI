/*
 * This file is part of Titanium
 * Copyright (C) 2024, Horizon Studio <contact@hrznstudio.com>.
 *
 * This code is licensed under GNU Lesser General Public License v3.0, the full license text can be found in LICENSE.txt
 */

package com.hrznstudio.titanium._impl.test.recipe;

import com.hrznstudio.titanium.recipe.serializer.GenericSerializer;
import com.hrznstudio.titanium.recipe.serializer.SerializableRecipe;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;

public class TestSerializableRecipe extends SerializableRecipe {

    public static Holder<RecipeSerializer<?>> SERIALIZER;
    public static Holder<RecipeType<?>> RECIPE_TYPE;
    public static final List<TestSerializableRecipe> RECIPES = new ArrayList<>();

    static {
        new TestSerializableRecipe(Ingredient.of(new ItemStack(Items.OAK_SAPLING)), new ItemStack(Items.STICK, 3), Blocks.STONE);
        new TestSerializableRecipe(Ingredient.of(new ItemStack(Blocks.DIRT)), new ItemStack(Items.DIAMOND, 1), Blocks.DIRT);
        ItemStack pick = new ItemStack(Items.DIAMOND_PICKAXE, 1);
        pick.setDamageValue(100);
        new TestSerializableRecipe(Ingredient.of(new ItemStack(Blocks.STONE)), pick, Blocks.DIRT);
    }

    public static final Codec<TestSerializableRecipe> CODEC = RecordCodecBuilder.create(in -> in.group(
        Ingredient.CODEC_NONEMPTY.fieldOf("input").forGetter(i -> i.input),
        ItemStack.ITEM_WITH_COUNT_CODEC.fieldOf("output").forGetter(i -> i.output),
        BuiltInRegistries.BLOCK.byNameCodec().fieldOf("block").forGetter(i -> i.block)
    ).apply(in, TestSerializableRecipe::new));

    public Ingredient input;
    public ItemStack output;
    public Block block;

    public TestSerializableRecipe(Ingredient input, ItemStack output, Block block) {
        this.input = input;
        this.output = output;
        this.block = block;
        RECIPES.add(this);
    }

    public TestSerializableRecipe() {
    }

    @Override
    public boolean matches(Container inv, Level worldIn) {
        return this.input.test(inv.getItem(0));
    }

    @Override
    public ItemStack assemble(Container p_44001_, RegistryAccess p_267165_) {
        return this.output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess p_267052_) {
        return this.output;
    }

    @Override
    public ItemStack getToastSymbol() {
        return this.output;
    }

    @Override
    public GenericSerializer<? extends SerializableRecipe> getSerializer() {
        return (GenericSerializer<? extends SerializableRecipe>) SERIALIZER.value();
    }

    @Override
    public RecipeType<?> getType() {
        return RECIPE_TYPE.value();
    }

    public boolean isValid(ItemStack input, Block block) {
        return this.input.test(input) && this.block == block;
    }
}
