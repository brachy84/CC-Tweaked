// Copyright Daniel Ratcliffe, 2011-2022. Do not distribute without permission.
//
// SPDX-License-Identifier: LicenseRef-CCPL

package dan200.computercraft.shared.turtle.items;

import dan200.computercraft.annotations.ForgeOverride;
import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.turtle.ITurtleUpgrade;
import dan200.computercraft.api.turtle.TurtleSide;
import dan200.computercraft.api.upgrades.UpgradeData;
import dan200.computercraft.impl.TurtleUpgrades;
import dan200.computercraft.shared.ModRegistry;
import dan200.computercraft.shared.turtle.TurtleOverlay;
import dan200.computercraft.shared.turtle.blocks.TurtleBlock;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import org.jspecify.annotations.Nullable;

public class TurtleItem extends BlockItem {
    public TurtleItem(TurtleBlock block, Properties settings) {
        super(block, settings);
    }

    @Override
    public Component getName(ItemStack stack) {
        var baseString = getDescriptionId(stack);
        var left = getUpgrade(stack, TurtleSide.LEFT);
        var right = getUpgrade(stack, TurtleSide.RIGHT);
        if (left != null && right != null) {
            return Component.translatable(baseString + ".upgraded_twice", right.getAdjective(), left.getAdjective());
        } else if (left != null) {
            return Component.translatable(baseString + ".upgraded", left.getAdjective());
        } else if (right != null) {
            return Component.translatable(baseString + ".upgraded", right.getAdjective());
        } else {
            return Component.translatable(baseString);
        }
    }

    @Nullable
    @ForgeOverride
    public String getCreatorModId(ItemStack stack) {
        // Determine our "creator mod" from the upgrades. We attempt to find the first non-vanilla/non-CC
        // upgrade (starting from the left).

        var left = getUpgradeWithData(stack, TurtleSide.LEFT);
        if (left != null) {
            var mod = TurtleUpgrades.instance().getOwner(left.holder());
            if (!mod.equals(ComputerCraftAPI.MOD_ID)) return mod;
        }

        var right = getUpgradeWithData(stack, TurtleSide.RIGHT);
        if (right != null) {
            var mod = TurtleUpgrades.instance().getOwner(right.holder());
            if (!mod.equals(ComputerCraftAPI.MOD_ID)) return mod;
        }

        return ComputerCraftAPI.MOD_ID;
    }

    public static @Nullable ITurtleUpgrade getUpgrade(ItemStack stack, TurtleSide side) {
        var upgrade = getUpgradeWithData(stack, side);
        return upgrade == null ? null : upgrade.upgrade();
    }

    public static @Nullable UpgradeData<ITurtleUpgrade> getUpgradeWithData(ItemStack stack, TurtleSide side) {
        return stack.get(side == TurtleSide.LEFT ? ModRegistry.DataComponents.LEFT_TURTLE_UPGRADE.get() : ModRegistry.DataComponents.RIGHT_TURTLE_UPGRADE.get());
    }

    public static @Nullable TurtleOverlay getOverlay(ItemStack stack) {
        var overlay = stack.get(ModRegistry.DataComponents.OVERLAY.get());
        return overlay == null ? null : overlay.value();
    }

    public static final CauldronInteraction CAULDRON_INTERACTION = (blockState, level, pos, player, hand, stack) -> {
        if (!stack.has(DataComponents.DYED_COLOR)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (!level.isClientSide) {
            stack.remove(DataComponents.DYED_COLOR);
            LayeredCauldronBlock.lowerFillLevel(blockState, level, pos);
        }

        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    };
}
