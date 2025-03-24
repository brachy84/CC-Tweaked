/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2020. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */
package dan200.computercraft.shared.pocket.peripherals;

import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.pocket.AbstractPocketUpgrade;
import dan200.computercraft.api.pocket.IPocketAccess;
import dan200.computercraft.shared.peripheral.PeripheralType;
import dan200.computercraft.shared.peripheral.common.PeripheralItemFactory;
import dan200.computercraft.shared.peripheral.modem.ModemState;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PocketModem extends AbstractPocketUpgrade {

    private final boolean advanced;

    public PocketModem(boolean advanced) {
        super(advanced ? new ResourceLocation("computercraft", "advanced_modem") : new ResourceLocation("computercraft", "wireless_modem"),
              PeripheralItemFactory.create(advanced ? PeripheralType.AdvancedModem : PeripheralType.WirelessModem, null, 1));
        this.advanced = advanced;
    }

    @Nullable
    @Override
    public IPeripheral createPeripheral(@Nonnull IPocketAccess access) {
        return new PocketModemPeripheral(advanced);
    }

    @Override
    public void update(@Nonnull IPocketAccess access, @Nullable IPeripheral peripheral) {
        if (!(peripheral instanceof PocketModemPeripheral modem)) return;

        Entity entity = access.getValidEntity();

        if (entity != null) modem.setLocation(entity.getEntityWorld(), entity.getPositionEyes(1));

        ModemState state = modem.getModemState();
        if (state.pollChanged()) access.setLight(state.isOpen() ? 0xBA0000 : -1);
    }
}
