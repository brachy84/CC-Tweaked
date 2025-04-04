/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2020. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */
package dan200.computercraft.shared.computer.items;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.computer.blocks.TileComputer;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public final class ComputerItemFactory {

    private ComputerItemFactory() {
    }

    @Nonnull
    public static ItemStack create(TileComputer tile) {
        return create(tile.getComputerID(), tile.getLabel(), tile.getFamily());
    }

    @Nonnull
    public static ItemStack create(int id, String label, ComputerFamily family) {
        switch (family) {
            case Normal:
            case Advanced:
                return ComputerCraft.Items.computer.create(id, label, family);
            case Command:
                return ComputerCraft.Items.commandComputer.create(id, label, family);
            default:
                return ItemStack.EMPTY;
        }
    }
}
