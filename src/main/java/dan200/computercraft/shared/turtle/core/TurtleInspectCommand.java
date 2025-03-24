/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2020. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */
package dan200.computercraft.shared.turtle.core;

import com.google.common.collect.ImmutableMap;
import dan200.computercraft.api.turtle.ITurtleAccess;
import dan200.computercraft.api.turtle.ITurtleCommand;
import dan200.computercraft.api.turtle.TurtleCommandResult;
import dan200.computercraft.api.turtle.event.TurtleBlockEvent;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class TurtleInspectCommand implements ITurtleCommand {

    private final InteractDirection direction;

    public TurtleInspectCommand(InteractDirection direction) {
        this.direction = direction;
    }

    @Nonnull
    @Override
    public TurtleCommandResult execute(@Nonnull ITurtleAccess turtle) {
        // Get world direction from direction
        EnumFacing direction = this.direction.toWorldDir(turtle);

        // Check if thing in front is air or not
        World world = turtle.getWorld();
        BlockPos oldPosition = turtle.getPosition();
        BlockPos newPosition = oldPosition.offset(direction);

        IBlockState state = world.getBlockState(newPosition);
        if (state.getBlock().isAir(state, world, newPosition)) {
            return TurtleCommandResult.failure("No block to inspect");
        }

        Block block = state.getBlock();

        Map<String, Object> table = new HashMap<>(3);
        table.put("name", Block.REGISTRY.getNameForObject(block).toString());
        table.put("metadata", block.getMetaFromState(state));

        Map<Object, Object> stateTable = new HashMap<>();
        for (ImmutableMap.Entry<IProperty<?>, ? extends Comparable<?>> entry : state.getActualState(world,
                                                                                                    newPosition).getProperties().entrySet()) {
            IProperty<?> property = entry.getKey();
            stateTable.put(property.getName(), getPropertyValue(property, entry.getValue()));
        }
        table.put("state", stateTable);

        // Fire the event, exiting if it is cancelled
        TurtlePlayer turtlePlayer = TurtlePlaceCommand.createPlayer(turtle, oldPosition, direction);
        TurtleBlockEvent.Inspect event = new TurtleBlockEvent.Inspect(turtle, turtlePlayer, world, newPosition, state, table);
        if (MinecraftForge.EVENT_BUS.post(event)) return TurtleCommandResult.failure(event.getFailureMessage());

        return TurtleCommandResult.success(new Object[]{table});

    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object getPropertyValue(IProperty property, Comparable value) {
        if (value instanceof String || value instanceof Number || value instanceof Boolean) return value;
        return property.getName(value);
    }
}
