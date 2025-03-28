/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2020. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */
package dan200.computercraft.shared.turtle.items;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.turtle.ITurtleAccess;
import dan200.computercraft.api.turtle.ITurtleUpgrade;
import dan200.computercraft.api.turtle.TurtleSide;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.turtle.blocks.ITurtleTile;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

public final class TurtleItemFactory {

    private TurtleItemFactory() {
    }

    @Nonnull
    public static ItemStack create(ITurtleTile turtle) {
        ITurtleAccess access = turtle.getAccess();

        return create(turtle.getComputerID(), turtle.getLabel(), turtle.getColour(), turtle.getFamily(), access.getUpgrade(TurtleSide.Left),
                      access.getUpgrade(TurtleSide.Right), access.getFuelLevel(), turtle.getOverlay());
    }

    @Nonnull
    public static ItemStack create(int id, String label, int colour, ComputerFamily family, ITurtleUpgrade leftUpgrade,
                                   ITurtleUpgrade rightUpgrade, int fuelLevel, ResourceLocation overlay) {
        switch (family) {
            case Normal:
                return ComputerCraft.Items.turtleExpanded.create(id, label, colour, leftUpgrade, rightUpgrade, fuelLevel, overlay);
            case Advanced:
                return ComputerCraft.Items.turtleAdvanced.create(id, label, colour, leftUpgrade, rightUpgrade, fuelLevel, overlay);
            default:
                return ItemStack.EMPTY;
        }
    }
}
