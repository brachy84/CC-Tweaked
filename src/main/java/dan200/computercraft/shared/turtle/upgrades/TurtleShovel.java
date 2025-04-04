/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2020. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */
package dan200.computercraft.shared.turtle.upgrades;

import dan200.computercraft.api.turtle.ITurtleAccess;
import dan200.computercraft.api.turtle.TurtleCommandResult;
import dan200.computercraft.api.turtle.TurtleSide;
import dan200.computercraft.api.turtle.TurtleVerb;
import dan200.computercraft.shared.turtle.core.TurtlePlaceCommand;
import dan200.computercraft.shared.turtle.core.TurtlePlayer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class TurtleShovel extends TurtleTool {

    public TurtleShovel(ResourceLocation id, int legacyId, String adjective, Item item) {
        super(id, legacyId, adjective, item);
    }

    public TurtleShovel(ResourceLocation id, int legacyId, Item item) {
        super(id, legacyId, item);
    }

    public TurtleShovel(ResourceLocation id, ItemStack craftItem, ItemStack toolItem) {
        super(id, craftItem, toolItem);
    }

    @Override
    protected boolean canBreakBlock(IBlockState state, World world, BlockPos pos, TurtlePlayer player) {
        if (!super.canBreakBlock(state, world, pos, player)) return false;

        Material material = state.getMaterial();
        return material == Material.GROUND ||
            material == Material.SAND ||
            material == Material.SNOW ||
            material == Material.CLAY ||
            material == Material.CRAFTED_SNOW ||
            material == Material.GRASS ||
            material == Material.PLANTS ||
            material == Material.CACTUS ||
            material == Material.GOURD ||
            material == Material.LEAVES ||
            material == Material.VINE;
    }

    @Nonnull
    @Override
    public TurtleCommandResult useTool(@Nonnull ITurtleAccess turtle, @Nonnull TurtleSide side, @Nonnull TurtleVerb verb,
                                       @Nonnull EnumFacing direction) {
        if (verb == TurtleVerb.Dig) {
            ItemStack shovel = item.copy();
            ItemStack remainder = TurtlePlaceCommand.deploy(shovel, turtle, direction, null, null);
            if (remainder != shovel) {
                return TurtleCommandResult.success();
            }
        }
        return super.useTool(turtle, side, verb, direction);
    }
}
