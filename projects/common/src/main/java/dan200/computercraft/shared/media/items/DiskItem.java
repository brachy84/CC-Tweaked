// SPDX-FileCopyrightText: 2025 The CC: Tweaked Developers
//
// SPDX-License-Identifier: MPL-2.0

package dan200.computercraft.shared.media.items;

import dan200.computercraft.annotations.ForgeOverride;
import dan200.computercraft.shared.peripheral.diskdrive.DiskDriveBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;

/**
 * An item that can be shift-right-clicked into a {@link DiskDriveBlock}.
 */
public class DiskItem extends Item {
    public DiskItem(Properties settings) {
        super(settings);
    }

    @ForgeOverride
    public boolean doesSneakBypassUse(ItemStack stack, LevelReader world, BlockPos pos, Player player) {
        return true;
    }
}
