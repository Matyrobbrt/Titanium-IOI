/*
 * This file is part of Titanium
 * Copyright (C) 2024, Horizon Studio <contact@hrznstudio.com>.
 *
 * This code is licensed under GNU Lesser General Public License v3.0, the full license text can be found in LICENSE.txt
 */

package com.hrznstudio.titanium.module;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CreativeTabHelper {

    private HashMap<ResourceLocation, List<Holder<Item>>> item;

    public CreativeTabHelper() {
        this.item = new HashMap<>();
    }

    public void put(ResourceLocation resourceLocation, Holder<Item> item) {
        this.item.computeIfAbsent(resourceLocation, resourceLocation1 -> {
            List<Holder<Item>> items = new ArrayList<>();
            items.add(item);
            return items;
        });
    }
}
