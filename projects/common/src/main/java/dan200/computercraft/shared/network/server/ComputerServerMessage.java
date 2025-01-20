// SPDX-FileCopyrightText: 2018 The CC: Tweaked Developers
//
// SPDX-License-Identifier: MPL-2.0

package dan200.computercraft.shared.network.server;

import dan200.computercraft.shared.computer.menu.ComputerMenu;
import dan200.computercraft.shared.network.NetworkMessage;
import net.minecraft.world.entity.player.Player;

/**
 * A packet, which performs an action on the currently open {@link ComputerMenu}.
 */
public abstract class ComputerServerMessage implements NetworkMessage<ServerNetworkContext> {
    private final int containerId;

    ComputerServerMessage(int id) {
        containerId = id;
    }

    int containerId() {
        return containerId;
    }

    @Override
    public void handle(ServerNetworkContext context) {
        Player player = context.getSender();
        if (player.containerMenu.containerId == containerId && player.containerMenu instanceof ComputerMenu menu) {
            handle(context, menu);
        }
    }

    protected abstract void handle(ServerNetworkContext context, ComputerMenu container);
}
