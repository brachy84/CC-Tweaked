// Copyright Daniel Ratcliffe, 2011-2022. Do not distribute without permission.
//
// SPDX-License-Identifier: LicenseRef-CCPL

package dan200.computercraft.shared.turtle.upgrades;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.turtle.*;
import dan200.computercraft.api.upgrades.UpgradeType;
import dan200.computercraft.shared.ModRegistry;
import dan200.computercraft.shared.peripheral.modem.ModemState;
import dan200.computercraft.shared.peripheral.modem.wireless.WirelessModemPeripheral;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class TurtleModem extends AbstractTurtleUpgrade {
    public static final MapCodec<TurtleModem> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(x -> x.getCraftingItem().getItem()),
        Codec.BOOL.fieldOf("advanced").forGetter(TurtleModem::advanced)
    ).apply(instance, (item, advanced) -> new TurtleModem(new ItemStack(item), advanced)));

    private static class Peripheral extends WirelessModemPeripheral {
        private final ITurtleAccess turtle;

        Peripheral(ITurtleAccess turtle, boolean advanced) {
            super(new ModemState(), advanced);
            this.turtle = turtle;
        }

        @Override
        public Level getLevel() {
            return turtle.getLevel();
        }

        @Override
        public Vec3 getPosition() {
            var turtlePos = turtle.getPosition();
            return new Vec3(
                turtlePos.getX(),
                turtlePos.getY(),
                turtlePos.getZ()
            );
        }

        @Override
        public boolean equals(@Nullable IPeripheral other) {
            return this == other || (other instanceof Peripheral modem && modem.turtle == turtle);
        }
    }

    private final boolean advanced;

    public TurtleModem(ItemStack stack, boolean advanced) {
        super(TurtleUpgradeType.PERIPHERAL, advanced ? WirelessModemPeripheral.ADVANCED_ADJECTIVE : WirelessModemPeripheral.NORMAL_ADJECTIVE, stack);
        this.advanced = advanced;
    }

    public boolean advanced() {
        return advanced;
    }

    @Override
    public IPeripheral createPeripheral(ITurtleAccess turtle, TurtleSide side) {
        return new Peripheral(turtle, advanced);
    }

    @Override
    public TurtleCommandResult useTool(ITurtleAccess turtle, TurtleSide side, TurtleVerb verb, Direction dir) {
        return TurtleCommandResult.failure();
    }

    @Override
    public void update(ITurtleAccess turtle, TurtleSide side) {
        // Advance the modem
        if (!turtle.getLevel().isClientSide) {
            var peripheral = turtle.getPeripheral(side);
            if (peripheral instanceof Peripheral modem) {
                var state = modem.getModemState();
                if (state.pollChanged()) {
                    turtle.setUpgradeData(side, DataComponentPatch.builder().set(ModRegistry.DataComponents.ON.get(), state.isOpen()).build());
                }
            }
        }
    }

    @Override
    public DataComponentPatch getPersistedData(DataComponentPatch upgradeData) {
        return DataComponentPatch.EMPTY;
    }

    @Override
    public UpgradeType<TurtleModem> getType() {
        return ModRegistry.TurtleUpgradeTypes.WIRELESS_MODEM.get();
    }
}
