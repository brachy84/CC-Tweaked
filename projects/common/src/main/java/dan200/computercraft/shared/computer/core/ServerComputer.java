// Copyright Daniel Ratcliffe, 2011-2022. Do not distribute without permission.
//
// SPDX-License-Identifier: LicenseRef-CCPL

package dan200.computercraft.shared.computer.core;

import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.component.AdminComputer;
import dan200.computercraft.api.component.ComputerComponents;
import dan200.computercraft.api.filesystem.WritableMount;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.WorkMonitor;
import dan200.computercraft.core.computer.Computer;
import dan200.computercraft.core.computer.ComputerEnvironment;
import dan200.computercraft.core.computer.ComputerEvents;
import dan200.computercraft.core.computer.ComputerSide;
import dan200.computercraft.core.metrics.MetricsObserver;
import dan200.computercraft.impl.ApiFactories;
import dan200.computercraft.shared.computer.menu.ComputerMenu;
import dan200.computercraft.shared.computer.terminal.NetworkedTerminal;
import dan200.computercraft.shared.computer.terminal.TerminalState;
import dan200.computercraft.shared.config.Config;
import dan200.computercraft.shared.network.NetworkMessage;
import dan200.computercraft.shared.network.client.ClientNetworkContext;
import dan200.computercraft.shared.network.client.ComputerTerminalClientMessage;
import dan200.computercraft.shared.network.server.ServerNetworking;
import dan200.computercraft.shared.util.ComponentMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

public class ServerComputer implements ComputerEnvironment, ComputerEvents.Receiver {
    private final int instanceID;
    private final UUID instanceUUID = UUID.randomUUID();

    private ServerLevel level;
    private BlockPos position;

    private final ComputerFamily family;
    private final MetricsObserver metrics;
    private final Computer computer;

    private final NetworkedTerminal terminal;
    private final AtomicBoolean terminalChanged = new AtomicBoolean(false);

    private int ticksSincePing;

    public ServerComputer(
        ServerLevel level, BlockPos position, int computerID, @Nullable String label, ComputerFamily family, int terminalWidth, int terminalHeight,
        ComponentMap baseComponents
    ) {
        this.level = level;
        this.position = position;
        this.family = family;

        var context = ServerContext.get(level.getServer());
        instanceID = context.registry().getUnusedInstanceID();
        terminal = new NetworkedTerminal(terminalWidth, terminalHeight, family != ComputerFamily.NORMAL, this::markTerminalChanged);
        metrics = context.metrics().createMetricObserver(this);

        var componentBuilder = ComponentMap.builder();
        componentBuilder.add(ComponentMap.METRICS, metrics);
        if (family == ComputerFamily.COMMAND) {
            componentBuilder.add(ComputerComponents.ADMIN_COMPUTER, new AdminComputer() {
            });
        }
        componentBuilder.add(baseComponents);
        var components = componentBuilder.build();

        computer = new Computer(context.computerContext(), this, terminal, computerID);
        computer.setLabel(label);

        // Load in the externally registered APIs.
        for (var factory : ApiFactories.getAll()) {
            var system = new ComputerSystem(this, computer.getAPIEnvironment(), components);
            var api = factory.create(system);
            if (api == null) continue;

            system.activate();
            computer.addApi(api, system);
        }
    }

    public final ComputerFamily getFamily() {
        return family;
    }

    public final ServerLevel getLevel() {
        return level;
    }

    public final BlockPos getPosition() {
        return position;
    }

    public final void setPosition(ServerLevel level, BlockPos pos) {
        this.level = level;
        position = pos.immutable();
    }

    protected final void markTerminalChanged() {
        terminalChanged.set(true);
    }

    protected void tickServer() {
        ticksSincePing++;
        computer.tick();
        if (terminalChanged.getAndSet(false)) onTerminalChanged();
    }

    protected void onTerminalChanged() {
        sendToAllInteracting(c -> new ComputerTerminalClientMessage(c, getTerminalState()));
    }

    public final TerminalState getTerminalState() {
        return TerminalState.create(terminal);
    }

    public final void keepAlive() {
        ticksSincePing = 0;
    }

    boolean hasTimedOut() {
        return ticksSincePing > 100;
    }

    /**
     * Get a bitmask returning which sides on the computer have changed, resetting the internal state.
     *
     * @return What sides on the computer have changed.
     */
    public final int pollRedstoneChanges() {
        return computer.pollRedstoneChanges();
    }

    public UUID register() {
        ServerContext.get(level.getServer()).registry().add(this);
        return instanceUUID;
    }

    void unload() {
        computer.unload();
    }

    public final void close() {
        unload();
        ServerContext.get(level.getServer()).registry().remove(this);
    }

    /**
     * Check whether this computer is usable by a player.
     *
     * @param player The player trying to use this computer.
     * @return Whether this computer can be used.
     */
    public final boolean checkUsable(Player player) {
        return ServerContext.get(level.getServer()).registry().get(instanceUUID) == this
            && getFamily().checkUsable(player);
    }

    private void sendToAllInteracting(Function<AbstractContainerMenu, NetworkMessage<ClientNetworkContext>> createPacket) {
        var server = level.getServer();

        for (var player : server.getPlayerList().getPlayers()) {
            if (player.containerMenu instanceof ComputerMenu && ((ComputerMenu) player.containerMenu).getComputer() == this) {
                ServerNetworking.sendToPlayer(createPacket.apply(player.containerMenu), player);
            }
        }
    }

    protected void onRemoved() {
    }

    public final int getInstanceID() {
        return instanceID;
    }

    public final UUID getInstanceUUID() {
        return instanceUUID;
    }

    public final int getID() {
        return computer.getID();
    }

    public final @Nullable String getLabel() {
        return computer.getLabel();
    }

    public final boolean isOn() {
        return computer.isOn();
    }

    public final ComputerState getState() {
        if (!computer.isOn()) return ComputerState.OFF;
        return computer.isBlinking() ? ComputerState.BLINKING : ComputerState.ON;
    }

    public final void turnOn() {
        computer.turnOn();
    }

    public final void shutdown() {
        computer.shutdown();
    }

    public final void reboot() {
        computer.reboot();
    }

    @Override
    public final void queueEvent(String event, @Nullable Object[] arguments) {
        computer.queueEvent(event, arguments);
    }

    public final void queueEvent(String event) {
        queueEvent(event, null);
    }

    public final int getRedstoneOutput(ComputerSide side) {
        return computer.isOn() ? computer.getRedstone().getExternalOutput(side) : 0;
    }

    public final void setRedstoneInput(ComputerSide side, int level, int bundledState) {
        computer.getRedstone().setInput(side, level, bundledState);
    }

    public final int getBundledRedstoneOutput(ComputerSide side) {
        return computer.isOn() ? computer.getRedstone().getExternalBundledOutput(side) : 0;
    }

    public final void setPeripheral(ComputerSide side, @Nullable IPeripheral peripheral) {
        computer.getEnvironment().setPeripheral(side, peripheral);
    }

    @Nullable
    public final IPeripheral getPeripheral(ComputerSide side) {
        return computer.getEnvironment().getPeripheral(side);
    }

    public final void setLabel(@Nullable String label) {
        computer.setLabel(label);
    }

    @Override
    public final double getTimeOfDay() {
        return (level.getDayTime() + 6000) % 24000 / 1000.0;
    }

    @Override
    public final int getDay() {
        return (int) ((level.getDayTime() + 6000) / 24000) + 1;
    }

    @Override
    public final MetricsObserver getMetrics() {
        return metrics;
    }

    public final WorkMonitor getMainThreadMonitor() {
        return computer.getMainThreadMonitor();
    }

    @Override
    public final WritableMount createRootMount() {
        return ComputerCraftAPI.createSaveDirMount(level.getServer(), "computer/" + computer.getID(), Config.computerSpaceLimit);
    }
}
