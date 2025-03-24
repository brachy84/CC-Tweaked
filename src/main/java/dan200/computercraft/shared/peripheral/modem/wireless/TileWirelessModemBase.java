/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2020. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */
package dan200.computercraft.shared.peripheral.modem.wireless;

import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralTile;
import dan200.computercraft.shared.common.TileGeneric;
import dan200.computercraft.shared.peripheral.modem.ModemPeripheral;
import dan200.computercraft.shared.peripheral.modem.ModemState;
import dan200.computercraft.shared.util.TickScheduler;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public abstract class TileWirelessModemBase extends TileGeneric implements IPeripheralTile {

    protected TileWirelessModemBase(boolean advanced) {
        this.advanced = advanced;
        modem = new Peripheral(this); // Needs to be initialised after advanced
    }

    private static class Peripheral extends WirelessModemPeripheral {

        private final TileWirelessModemBase entity;

        Peripheral(TileWirelessModemBase entity) {
            super(new ModemState(() -> TickScheduler.schedule(entity)), entity.advanced);
            this.entity = entity;
        }

        @Nonnull
        @Override
        public World getWorld() {
            return entity.getWorld();
        }

        @Nonnull
        @Override
        public Vec3d getPosition() {
            BlockPos pos = entity.getPos().offset(entity.modemDirection);
            return new Vec3d(pos.getX(), pos.getY(), pos.getZ());
        }

        @Override
        public boolean equals(IPeripheral other) {
            return this == other;
        }
    }

    private final boolean advanced;
    private boolean hasModemDirection = false;
    private EnumFacing modemDirection = EnumFacing.DOWN;
    private final ModemPeripheral modem;
    private boolean destroyed = false;

    private boolean on = false;

    @Override
    public void onLoad() {
        super.onLoad();
        updateDirection();
        world.scheduleUpdate(getPos(), getBlockType(), 0);
    }

    @Override
    public void destroy() {
        if (!destroyed) {
            modem.destroy();
            destroyed = true;
        }
    }

    @Override
    public void updateContainingBlockInfo() {
        hasModemDirection = false;
        super.updateContainingBlockInfo();
        world.scheduleUpdate(getPos(), getBlockType(), 0);
    }

    @Override
    public void updateTick() {
        updateDirection();

        if (modem.getModemState().pollChanged()) {
            boolean newOn = modem.getModemState().isOpen();
            if (newOn != on) {
                on = newOn;
                updateBlock();
            }
        }
    }

    private void updateDirection() {
        if (!hasModemDirection) {
            hasModemDirection = true;
            modemDirection = getDirection();
        }
    }

    protected abstract EnumFacing getDirection();

    @Override
    public void onNeighbourChange(@Nonnull BlockPos neighbour) {
        EnumFacing dir = getDirection();
        if (neighbour.equals(getPos().offset(dir)) && !getWorld().isSideSolid(neighbour, dir.getOpposite())) {
            // Drop everything and remove block
            getBlock().dropBlockAsItem(getWorld(), getPos(), getBlockState(), 0);
            getWorld().setBlockToAir(getPos());
        }
    }

    @Override
    protected void writeDescription(@Nonnull NBTTagCompound nbt) {
        super.writeDescription(nbt);
        nbt.setBoolean("on", on);
    }

    @Override
    public final void readDescription(@Nonnull NBTTagCompound nbt) {
        super.readDescription(nbt);
        on = nbt.getBoolean("on");
        updateBlock();
    }

    public boolean isOn() {
        return on;
    }

    @Override
    public IPeripheral getPeripheral(@Nonnull EnumFacing side) {
        return !destroyed && side == getDirection() ? modem : null;
    }
}
