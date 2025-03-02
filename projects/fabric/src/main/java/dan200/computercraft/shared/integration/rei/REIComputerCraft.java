// SPDX-FileCopyrightText: 2025 The CC: Tweaked Developers
//
// SPDX-License-Identifier: MPL-2.0

package dan200.computercraft.shared.integration.rei;

import dan200.computercraft.api.turtle.TurtleSide;
import dan200.computercraft.shared.ModRegistry;
import dan200.computercraft.shared.pocket.items.PocketComputerItem;
import dan200.computercraft.shared.turtle.items.TurtleItem;
import me.shedaniel.rei.api.common.entry.comparison.ItemComparatorRegistry;
import me.shedaniel.rei.api.common.plugins.REICommonPlugin;
import net.minecraft.world.item.component.DyedItemColor;

/**
 * Common integration for ComputerCraft.
 */
public class REIComputerCraft implements REICommonPlugin {
    @Override
    public void registerItemComparators(ItemComparatorRegistry registry) {
        registry.register((context, stack) -> {
            long hash = 1;

            var left = TurtleItem.getUpgradeWithData(stack, TurtleSide.LEFT);
            var right = TurtleItem.getUpgradeWithData(stack, TurtleSide.RIGHT);
            if (left != null) hash = hash * 31 + left.holder().key().location().hashCode();
            if (right != null) hash = hash * 31 + right.holder().key().location().hashCode();

            return hash;
        }, ModRegistry.Items.TURTLE_NORMAL.get(), ModRegistry.Items.TURTLE_ADVANCED.get());

        registry.register((context, stack) -> {
            var upgrade = PocketComputerItem.getUpgradeWithData(stack);
            return upgrade == null ? 1 : upgrade.holder().key().location().hashCode();
        }, ModRegistry.Items.POCKET_COMPUTER_NORMAL.get(), ModRegistry.Items.POCKET_COMPUTER_ADVANCED.get());

        registry.register((context, stack) -> DyedItemColor.getOrDefault(stack, -1), ModRegistry.Items.DISK.get());
    }
}
