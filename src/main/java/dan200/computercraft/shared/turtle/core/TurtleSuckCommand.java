/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2020. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */
package dan200.computercraft.shared.turtle.core;

import dan200.computercraft.api.turtle.ITurtleAccess;
import dan200.computercraft.api.turtle.ITurtleCommand;
import dan200.computercraft.api.turtle.TurtleAnimation;
import dan200.computercraft.api.turtle.TurtleCommandResult;
import dan200.computercraft.api.turtle.event.TurtleInventoryEvent;
import dan200.computercraft.shared.util.InventoryUtil;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.List;

public class TurtleSuckCommand implements ITurtleCommand {

    private final InteractDirection m_direction;
    private final int m_quantity;

    public TurtleSuckCommand(InteractDirection direction, int quantity) {
        m_direction = direction;
        m_quantity = quantity;
    }

    @Nonnull
    @Override
    public TurtleCommandResult execute(@Nonnull ITurtleAccess turtle) {
        // Sucking nothing is easy
        if (m_quantity == 0) {
            turtle.playAnimation(TurtleAnimation.Wait);
            return TurtleCommandResult.success();
        }

        // Get world direction from direction
        EnumFacing direction = m_direction.toWorldDir(turtle);

        // Get inventory for thing in front
        World world = turtle.getWorld();
        BlockPos turtlePosition = turtle.getPosition();
        BlockPos blockPosition = turtlePosition.offset(direction);
        EnumFacing side = direction.getOpposite();

        IItemHandler inventory = InventoryUtil.getInventory(world, blockPosition, side);

        // Fire the event, exiting if it is cancelled.
        TurtlePlayer player = TurtlePlaceCommand.createPlayer(turtle, turtlePosition, direction);
        TurtleInventoryEvent.Suck event = new TurtleInventoryEvent.Suck(turtle, player, world, blockPosition, inventory);
        if (MinecraftForge.EVENT_BUS.post(event)) {
            return TurtleCommandResult.failure(event.getFailureMessage());
        }

        if (inventory != null) {
            // Take from inventory of thing in front
            ItemStack stack = InventoryUtil.takeItems(m_quantity, inventory);
            if (stack.isEmpty()) return TurtleCommandResult.failure("No items to take");

            // Try to place into the turtle
            ItemStack remainder = InventoryUtil.storeItems(stack, turtle.getItemHandler(), turtle.getSelectedSlot());
            if (!remainder.isEmpty()) {
                // Put the remainder back in the inventory
                InventoryUtil.storeItems(remainder, inventory);
            }

            // Return true if we consumed anything
            if (remainder != stack) {
                turtle.playAnimation(TurtleAnimation.Wait);
                return TurtleCommandResult.success();
            } else {
                return TurtleCommandResult.failure("No space for items");
            }
        } else {
            // Suck up loose items off the ground
            AxisAlignedBB aabb = new AxisAlignedBB(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ(),
                                                   blockPosition.getX() + 1.0, blockPosition.getY() + 1.0, blockPosition.getZ() + 1.0);
            List<EntityItem> list = world.getEntitiesWithinAABB(EntityItem.class, aabb, EntitySelectors.IS_ALIVE);
            if (list.isEmpty()) return TurtleCommandResult.failure("No items to take");

            for (EntityItem entity : list) {
                // Suck up the item
                ItemStack stack = entity.getItem().copy();

                ItemStack storeStack;
                ItemStack leaveStack;
                if (stack.getCount() > m_quantity) {
                    storeStack = stack.splitStack(m_quantity);
                    leaveStack = stack;
                } else {
                    storeStack = stack;
                    leaveStack = ItemStack.EMPTY;
                }

                ItemStack remainder = InventoryUtil.storeItems(storeStack, turtle.getItemHandler(), turtle.getSelectedSlot());

                if (remainder != storeStack) {
                    if (remainder.isEmpty() && leaveStack.isEmpty()) {
                        entity.setDead();
                    } else if (remainder.isEmpty()) {
                        entity.setItem(leaveStack);
                    } else if (leaveStack.isEmpty()) {
                        entity.setItem(remainder);
                    } else {
                        leaveStack.grow(remainder.getCount());
                        entity.setItem(leaveStack);
                    }

                    // Play fx
                    world.playBroadcastSound(1000, turtlePosition, 0); // BLOCK_DISPENSER_DISPENSE
                    turtle.playAnimation(TurtleAnimation.Wait);
                    return TurtleCommandResult.success();
                }
            }


            return TurtleCommandResult.failure("No space for items");
        }
    }
}
