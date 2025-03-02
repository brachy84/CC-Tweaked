// SPDX-FileCopyrightText: 2018 The CC: Tweaked Developers
//
// SPDX-License-Identifier: MPL-2.0

package dan200.computercraft.shared.peripheral.modem.wired;

import dan200.computercraft.shared.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public class WiredModemFullBlock extends Block implements EntityBlock {
    public static final BooleanProperty MODEM_ON = BooleanProperty.create("modem");
    public static final BooleanProperty PERIPHERAL_ON = BooleanProperty.create("peripheral");

    public WiredModemFullBlock(Properties settings) {
        super(settings);
        registerDefaultState(getStateDefinition().any()
            .setValue(MODEM_ON, false)
            .setValue(PERIPHERAL_ON, false)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(MODEM_ON, PERIPHERAL_ON);
    }

    @Override
    protected final InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        return world.getBlockEntity(pos) instanceof WiredModemFullBlockEntity modem ? modem.use(player) : InteractionResult.PASS;
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticker, BlockPos pos, Direction direction, BlockPos otherPos, BlockState neighborState, RandomSource randomSource) {
        if (state.getValue(PERIPHERAL_ON) && level.getBlockEntity(pos) instanceof WiredModemFullBlockEntity modem) {
            modem.queueRefreshPeripheral(direction);
        }

        return super.updateShape(state, level, ticker, pos, direction, otherPos, neighborState, randomSource);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, @Nullable Orientation orientation, boolean isMoving) {
        if (state.getValue(PERIPHERAL_ON) && level.getBlockEntity(pos) instanceof WiredModemFullBlockEntity modem) {
            modem.neighborChanged();
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource rand) {
        if (world.getBlockEntity(pos) instanceof WiredModemFullBlockEntity modem) modem.blockTick();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return ModRegistry.BlockEntities.WIRED_MODEM_FULL.get().create(pos, state);
    }
}
