// Copyright Daniel Ratcliffe, 2011-2022. Do not distribute without permission.
//
// SPDX-License-Identifier: LicenseRef-CCPL

package dan200.computercraft.shared.computer.core;

import dan200.computercraft.shared.config.Config;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public enum ComputerFamily {
    NORMAL,
    ADVANCED,
    COMMAND;

    /**
     * Check whether computers with this family can be used by the provided player.
     * <p>
     * This method is not pure. On failure, the method may send a message to the player telling them why they cannot
     * interact with the computer.
     *
     * @param player The player trying to use a computer.
     * @return Whether this computer family can be used.
     */
    public boolean checkUsable(Player player) {
        return switch (this) {
            case NORMAL, ADVANCED -> true;
            case COMMAND -> checkCommandUsable(player);
        };
    }

    /**
     * Get the save folder for this computer type.
     * <p>
     * Command computers are saved under a different namespace, to prevent people who have obtained a normal computer
     * with the same ID as a command computer (e.g. via creative mode, or a bug), having access to its files.
     *
     * @return The save folder for a
     */
    public String getSaveFolder() {
        return switch (this) {
            case NORMAL, ADVANCED -> "computer";
            case COMMAND -> "command_computer";
        };
    }

    private static boolean checkCommandUsable(Player player) {
        var server = player.getServer();
        if (server == null || !server.isCommandBlockEnabled()) {
            player.displayClientMessage(Component.translatable("advMode.notEnabled"), true);
            return false;
        } else if (!canUseCommandBlock(player)) {
            player.displayClientMessage(Component.translatable("advMode.notAllowed"), true);
            return false;
        }

        return true;
    }

    private static boolean canUseCommandBlock(Player player) {
        return Config.commandRequireCreative ? player.canUseGameMasterBlocks() : player.hasPermissions(2);
    }
}
