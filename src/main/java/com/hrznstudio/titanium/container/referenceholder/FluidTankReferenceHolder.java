/*
 * This file is part of Titanium
 * Copyright (C) 2024, Horizon Studio <contact@hrznstudio.com>.
 *
 * This code is licensed under GNU Lesser General Public License v3.0, the full license text can be found in LICENSE.txt
 */

package com.hrznstudio.titanium.container.referenceholder;

import com.hrznstudio.titanium.component.fluid.FluidTankComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

public class FluidTankReferenceHolder implements ContainerData {
    private final FluidTankComponent<?> fluidTank;
    private int fluidAmount = -1;
    private int fluidId = -1;

    public FluidTankReferenceHolder(FluidTankComponent<?> fluidTank) {
        this.fluidTank = fluidTank;
    }

    @Override
    public int get(int index) {
        FluidStack fluidStack = this.fluidTank.getFluid();
        if (fluidStack.isEmpty()) {
            return -1;
        } else if (index == 0) {
            return BuiltInRegistries.FLUID.getId(fluidStack.getFluid());
        } else {
            return fluidStack.getAmount();
        }
    }

    @Override
    public void set(int index, int value) {
        if (index == 0) {
            fluidId = value;
        } else {
            fluidAmount = value;
        }

        if (fluidAmount >= 0 && fluidId >= 0) {
            fluidTank.setFluidStack(new FluidStack(BuiltInRegistries.FLUID.byId(fluidId), fluidAmount));
        } else {
            fluidTank.setFluidStack(FluidStack.EMPTY);
        }
    }

    @Override
    public int getCount() {
        return 2;
    }
}
